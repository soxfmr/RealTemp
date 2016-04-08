package com.soxfmr.realtemp.bluetooth.contract;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public interface BluetoothLeSession {

    void disconnect();

    void setBluetoothGatt(BluetoothGatt gatt);

    BluetoothGatt getBluetoothGatt();

    /**
     * Load the services list to the current session
     * @param serviceList
     */
    void loadService(List<BluetoothGattService> serviceList);

    /**
     * Retrieve the services list from current session
     * @return
     */
    List<BluetoothGattService> getBluetoothGattServiceList();

    /**
     * Retrieve the characteristics list from current session
     * @return
     */
    List<BluetoothGattCharacteristic> getBluetoothGattCharacteristicList();

    /**
     * Retrieve the descriptors list from current session
     * @return
     */
    List<BluetoothGattDescriptor> getBluetoothGattDescriptorList();

    /**
     * Update the value of the characteristic to the remove server asynchronously
     * @param characteristic
     */
    void write(BluetoothGattCharacteristic characteristic);

    /**
     * Update the value of the characteristic to the remove server asynchronously
     * @param characteristic
     * @param value
     */
    void write(BluetoothGattCharacteristic characteristic, byte[] value);

    /**
     * Update the value of the descriptor to the remove server asynchronously
     * @param descriptor
     */
    void write(BluetoothGattDescriptor descriptor);

    /**
     * Update the value of the descriptor to the remove server asynchronously
     * @param descriptor
     * @param value
     */
    void write(BluetoothGattDescriptor descriptor, byte[] value);

    /**
     * Retrieve the latest value of the characteristic
     * @param characteristic
     */
    void read(BluetoothGattCharacteristic characteristic);

    /**
     * Retrieve the latest value of the descriptor
     * @param descriptor
     */
    void read(BluetoothGattDescriptor descriptor);

    /**
     * Toggle the notify for the characteristic
     * @param characteristic
     * @param enable
     * @return
     */
    boolean enableNotification(BluetoothGattCharacteristic characteristic, boolean enable);

}
