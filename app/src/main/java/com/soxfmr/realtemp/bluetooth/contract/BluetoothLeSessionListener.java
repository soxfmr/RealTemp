package com.soxfmr.realtemp.bluetooth.contract;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public interface BluetoothLeSessionListener {

    void onConnect(BluetoothLeSession session);

    void onDisconnect(BluetoothLeSession session);

}
