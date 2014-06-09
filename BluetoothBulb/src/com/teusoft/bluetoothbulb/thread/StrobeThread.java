package com.teusoft.bluetoothbulb.thread;

import com.teusoft.bluetoothbulb.business.Command;
import com.teusoft.bluetoothbulb.service.BluetoothLeService;

/**
 * Created by DungLV on 9/6/2014.
 */
public class StrobeThread extends Thread {
    private boolean isStart;
    private BluetoothLeService mBluetoothLeService;

    public StrobeThread(BluetoothLeService mBluetoothLeService) {
        this.mBluetoothLeService = mBluetoothLeService;
    }

    @Override
    public void run() {
        while (!isStart) {
            try {
                Command.powerOn(mBluetoothLeService);
                Thread.sleep(100);
                Command.powerOff(mBluetoothLeService);
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Request stop strobe thread
     */
    public void stopStrobeThread() {
        isStart = true;
    }
}
