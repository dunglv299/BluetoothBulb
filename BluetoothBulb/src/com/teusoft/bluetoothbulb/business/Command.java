package com.teusoft.bluetoothbulb.business;

import com.teusoft.bluetoothbulb.service.BluetoothLeService;
import com.teusoft.bluetoothbulb.utils.Utils;

/**
 * Created by DungLV on 9/6/2014.
 */
public class Command {
    /**
     * Power on bulb
     *
     * @param mBluetoothLeService
     */
    public static void powerOn(BluetoothLeService mBluetoothLeService) {
        byte[] value = new byte[]{(byte) 0XCC, (byte) 0X23, (byte) 0X33};
        mBluetoothLeService.writeCharacteristic(value);
    }

    /**
     * Power off bulb
     *
     * @param mBluetoothLeService
     */
    public static void powerOff(BluetoothLeService mBluetoothLeService) {
        byte[] value = new byte[]{(byte) 0XCC, (byte) 0X24, (byte) 0X33};
        mBluetoothLeService.writeCharacteristic(value);
    }

    /**
     * Send hexa color to bulb
     */
    public static void sendColor(BluetoothLeService mBluetoothLeService, String hex) {
        byte[] rgbColor = Utils.hexStringToByteArray(hex);
        byte[] value = new byte[]{(byte) 0X56, rgbColor[0], rgbColor[1], rgbColor[2], (byte) 0x29,
                (byte) 0xF0, (byte) 0XAA};
        mBluetoothLeService.writeCharacteristic(value);
    }
}
