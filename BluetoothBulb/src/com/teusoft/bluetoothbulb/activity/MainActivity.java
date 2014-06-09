/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teusoft.bluetoothbulb.activity;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.teusoft.bluetoothbulb.R;
import com.teusoft.bluetoothbulb.business.Command;
import com.teusoft.bluetoothbulb.service.BluetoothLeService;
import com.teusoft.bluetoothbulb.thread.StrobeThread;

import java.util.ArrayList;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class MainActivity extends Activity implements View.OnClickListener, ColorPicker.OnColorChangedListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ToggleButton mTogglePower;
    private ToggleButton mStrobeBtn;
    byte[] value;
    ColorPicker picker;
    private StrobeThread mStrobeThread;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(com.teusoft.bluetoothbulb.R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //TODO start control in here
                Log.e("ACTION_DATA_AVAILABLE", "ACTION_DATA_AVAILABLE");
            }
        }
    };

    private void clearUI() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public void initView() {
        // Sets up UI references.
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mTogglePower = (ToggleButton) findViewById(R.id.power_btn);
        mTogglePower.setOnClickListener(this);
        mStrobeBtn = (ToggleButton) findViewById(R.id.strobe_btn);
        mStrobeBtn.setOnClickListener(this);
        // Init color picker
        picker = (ColorPicker) findViewById(R.id.picker);
        picker.setOnColorChangedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            menu.findItem(R.id.menu_send).setVisible(true);

        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            menu.findItem(R.id.menu_send).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.power_btn:
                // Power button
                if (mTogglePower.isChecked()) {
                    Command.powerOff(mBluetoothLeService);
                } else {
                    Command.powerOn(mBluetoothLeService);
                }
                break;
            // Strobe button
            case R.id.strobe_btn:
                if (mStrobeBtn.isChecked()) {
                    stopStrobe();
                } else {
                    startStrobe();
                }
                break;
        }
    }


    @Override
    public void onColorChanged(int color) {
        String hexColor = String.format("#%06X", (0xFFFFFF & color));
        Log.e("hexColor", hexColor);
        if (mBluetoothLeService != null) {
            Command.sendColor(mBluetoothLeService, hexColor);
        }
    }

    /**
     * Start strobe light
     */
    private void startStrobe() {
        if (mStrobeThread == null) {
            mStrobeThread = new StrobeThread(mBluetoothLeService);
            mStrobeThread.start();
        }
    }

    /**
     * Stop strobe light
     */
    private void stopStrobe() {
        if (mStrobeThread != null) {
            mStrobeThread.stopStrobeThread();
            mStrobeThread = null;
        }
    }
}
