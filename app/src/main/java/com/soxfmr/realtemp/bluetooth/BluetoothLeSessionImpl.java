package com.soxfmr.realtemp.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSession;
import com.soxfmr.realtemp.utils.GattHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public class BluetoothLeSessionImpl implements BluetoothLeSession {

    private BluetoothGatt mBluetoothGatt;

    private List<BluetoothGattService> mServiceList;
    private List<BluetoothGattCharacteristic> mCharacteristicList;
    private List<BluetoothGattDescriptor> mDescriptorList;

    private BluetoothLeSessionImpl(BluetoothGatt bluetoothGatt) {
        mBluetoothGatt = bluetoothGatt;

        mServiceList = new ArrayList<>();
        mCharacteristicList = new ArrayList<>();
        mDescriptorList = new ArrayList<>();
    }

    private void loadService() {
        if (mServiceList == null || mServiceList.size() == 0)
            return;

        mCharacteristicList.clear();
        mDescriptorList.clear();

        for (BluetoothGattService service : mServiceList) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            // Extract all of characteristics and descriptors
            for (BluetoothGattCharacteristic characteristic : characteristics) {

                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    mDescriptorList.add(descriptor);
                }

                mCharacteristicList.add(characteristic);
            }
        }
    }

    @Override
    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    @Override
    public void loadService(List<BluetoothGattService> serviceList) {
        mServiceList = serviceList;

        loadService();
    }

    @Override
    public List<BluetoothGattService> getBluetoothGattServiceList() {
        return mServiceList;
    }

    @Override
    public List<BluetoothGattCharacteristic> getBluetoothGattCharacteristicList() {
        return mCharacteristicList;
    }

    @Override
    public List<BluetoothGattDescriptor> getBluetoothGattDescriptorList() {
        return mDescriptorList;
    }

    @Override
    public void write(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null || characteristic == null)
            return;

        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    @Override
    public void write(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (mBluetoothGatt == null || characteristic == null)
            return;

        characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    @Override
    public void write(BluetoothGattDescriptor descriptor) {
        if (mBluetoothGatt == null || descriptor == null)
            return;

        mBluetoothGatt.writeDescriptor(descriptor);
    }

    @Override
    public void write(BluetoothGattDescriptor descriptor, byte[] value) {
        if (mBluetoothGatt == null || descriptor == null)
            return;

        descriptor.setValue(value);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    @Override
    public void read(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null || characteristic == null)
            return;

        mBluetoothGatt.readCharacteristic(characteristic);
    }

    @Override
    public void read(BluetoothGattDescriptor descriptor) {
        if (mBluetoothGatt == null || descriptor == null)
            return;

        mBluetoothGatt.readDescriptor(descriptor);
    }

    @Override
    public boolean enableNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        boolean bRet = false;

        if (mBluetoothGatt == null || characteristic == null)
            return bRet;

        bRet = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (bRet) {
            UUID uuid = GattHelper.buildServiceUUID(GattHelper.BASE_SERVICE_NOTIFY);
            // We should reset the CCCD for the notify function
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
            if (descriptor != null) {
                descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }

        return bRet;
    }
}
