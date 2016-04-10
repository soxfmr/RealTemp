package com.soxfmr.realtemp.utils;

import android.bluetooth.BluetoothDevice;

import java.util.UUID;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public class GattHelper {
    public static final UUID BASE_SERVICE_NOTIFY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID BASE_SERVICE_HEART_RATE_MEASURE = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    public static final String BOND_STATUS_BONDED = "已配对";
    public static final String BOND_STATUS_NONE = "未配对";
    public static final String BOND_STATUS_UNKNOWN = "未知状态";

    public static String getStatusText(int status) {
        switch (status) {
            case BluetoothDevice.BOND_BONDED:
                return BOND_STATUS_BONDED;
            case BluetoothDevice.BOND_NONE:
                return BOND_STATUS_NONE;
            default:break;
        }

        return BOND_STATUS_UNKNOWN;
    }
}
