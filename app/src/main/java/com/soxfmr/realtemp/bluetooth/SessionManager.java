package com.soxfmr.realtemp.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;

import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSession;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSessionListener;

import static com.soxfmr.realtemp.utils.DebugUtils.DEG;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public class SessionManager {
    public static final String TAG = SessionManager.class.getName();

    private Context mContext;
    private BluetoothLeSession mSession;

    private boolean bAutoConnect;

    private BluetoothLeSessionListener mBluetoothLeSessionListener;

    public SessionManager(Context context) {
        mContext = context;
    }

    public void create(BluetoothDevice device, boolean autoConnect) {
        if (mContext == null || device == null) {
            return;
        }

        bAutoConnect = autoConnect;

        device.connectGatt(mContext, bAutoConnect, mBluetoothGattCallback);
    }

    public BluetoothLeSessionListener getBluetoothLeSessionListener() {
        return mBluetoothLeSessionListener;
    }

    public void setBluetoothLeSessionListener(BluetoothLeSessionListener l) {
        this.mBluetoothLeSessionListener = l;
    }

    public BluetoothLeSession getSession() {
        return mSession;
    }

    public void setSession(BluetoothLeSession session) {
        this.mSession = session;
    }

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (status) {
                case BluetoothGatt.STATE_CONNECTED:
                    if (mSession == null) {
                        mSession = new BluetoothLeSessionImpl();
                    }

                    mSession.setBluetoothGatt(gatt);
                    gatt.discoverServices();

                    if (mBluetoothLeSessionListener != null) {
                        mBluetoothLeSessionListener.onConnect(mSession);
                    }

                    if (DEG) Log.d(TAG, "Session created for " + gatt.getDevice().getName());
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    boolean unexpected = ! mSession.isClosed();
                    // No closed by the user, then recovery the session if the flag of auto connection is true
                    if (unexpected && bAutoConnect) {
                        gatt.connect();
                    }
                    // Reset the device instance
                    mSession.setBluetoothGatt(null);

                    if (mBluetoothLeSessionListener != null) {
                        mBluetoothLeSessionListener.onDisconnect(unexpected);
                    }

                    if (DEG) Log.d(TAG, "Session destroyed for " + gatt.getDevice().getName());
                    break;
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (mSession != null && !mSession.isClosed()) {
                mSession.loadService(gatt.getServices());
            }

            if (DEG) Log.d(TAG, "Services found on remote server, loading to the session.");
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (mBluetoothLeSessionListener != null) {
                mBluetoothLeSessionListener.onReceive(characteristic);
            }

            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (mBluetoothLeSessionListener != null) {
                mBluetoothLeSessionListener.onReceive(descriptor);
            }

            super.onDescriptorRead(gatt, descriptor, status);
        }
    };
}
