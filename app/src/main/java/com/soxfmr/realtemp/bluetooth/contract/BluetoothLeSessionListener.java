package com.soxfmr.realtemp.bluetooth.contract;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public interface BluetoothLeSessionListener {

    void onConnect(BluetoothLeSession session);

    void onDisconnect(boolean unexpected);

    void onReceive(BluetoothGattCharacteristic characteristic);

    void onReceive(BluetoothGattDescriptor descriptor);

}
