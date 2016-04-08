package com.soxfmr.realtemp.utils;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public class GattHelper {

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
