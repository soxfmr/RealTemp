package com.soxfmr.realtemp.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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
    private BluetoothGatt mBluetoothGatt;

    private boolean bAutoConnect;

    private BluetoothLeSessionListener mBluetoothLeSessionListener;

    public SessionManager() {
        this(null);
    }

    public SessionManager(Context context) {
        mContext = context;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void create(BluetoothDevice device, boolean autoConnect) {
        if (mContext == null || device == null) {
            return;
        }

        bAutoConnect = autoConnect;
        mBluetoothGatt = device.connectGatt(mContext, bAutoConnect, mBluetoothGattCallback);
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
            switch (newState) {
                case BluetoothGatt.STATE_CONNECTED:
                    gatt.discoverServices();
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    boolean unexpected = true;
                    if (mSession != null) {
                        unexpected = ! mSession.isClosed();
                        // No closed by the user, then recovery the session if the flag of auto connection is true
                        if (unexpected && bAutoConnect) {
                            gatt.connect();
                        }
                    }

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
            mSession = new BluetoothLeSessionImpl(gatt);
            mSession.loadService(gatt.getServices());

            if (mBluetoothLeSessionListener != null) {
                mBluetoothLeSessionListener.onConnect(mSession);
            }

            if (DEG) Log.d(TAG, "Services found on remote server, loading to the session.");
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS && mBluetoothLeSessionListener != null) {
                mBluetoothLeSessionListener.onReceive(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if (mBluetoothLeSessionListener != null) {
                mBluetoothLeSessionListener.onReceive(characteristic);
            }

            if (DEG) Log.d(TAG, "Characteristic value changed.");
        }
    };
}
