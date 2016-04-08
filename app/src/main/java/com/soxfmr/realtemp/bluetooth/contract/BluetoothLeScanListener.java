package com.soxfmr.realtemp.bluetooth.contract;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public interface BluetoothLeScanListener {

    void onStartScan();

    void onStopScan();

    void onResult(BluetoothDevice device);

}
