package com.soxfmr.realtemp.utils;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import java.util.UUID;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public class GattHelper {
    private static final String BASE_SERVICE_FORMAT = "0000%s-0000-1000-8000-00805f9b34fb";

    public static final String BASE_SERVICE_NOTIFY = "2902";
    public static final String BASE_SERVICE_HEART_RATE_MEASURE = "ffe1";

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

    public static UUID buildServiceUUID(String vendor) {
        if (TextUtils.isEmpty(vendor) || vendor.length() != 4) {
            return null;
        }

        return UUID.fromString(String.format(BASE_SERVICE_FORMAT, vendor));
    }

}
