package com.soxfmr.realtemp.bluetooth.contract;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public interface BluetoothBondStatusListener {

    void onBonded(BluetoothDevice device);

    void onUnBond(BluetoothDevice device);

}
