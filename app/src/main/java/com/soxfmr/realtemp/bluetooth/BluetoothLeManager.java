package com.soxfmr.realtemp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.soxfmr.realtemp.bluetooth.contract.BluetoothBondStatusListener;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeScanListener;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSession;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSessionListener;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothStatusListener;

import static com.soxfmr.realtemp.utils.DebugUtils.DEG;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public class BluetoothLeManager {
    public static final String TAG = BluetoothLeManager.class.getName();

    private static final BluetoothLeManager mBluetoothLeManager = new BluetoothLeManager();

    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;

    private SessionManager mSessionManager;

    private BluetoothStatusListener mBluetoothStatusListener;
    private BluetoothBondStatusListener mBluetoothBondStatusListener;
    private BluetoothLeScanListener mBluetoothLeScanListener;
    private BluetoothLeSessionListener mBluetoothLeSessionListener;

    private BluetoothLeManager() {
        mSessionManager = new SessionManager();
    }

    public static BluetoothLeManager getInstance() {
        return mBluetoothLeManager;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void setBluetoothAdapter(BluetoothAdapter adapter) {
        mBluetoothAdapter = adapter;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /**
     * The listener for the status of the Bluetooth device
     * @param l
     */
    public void setBluetoothStatusListener(BluetoothStatusListener l) {
        mBluetoothStatusListener = l;
    }

    /**
     * The listener for the bond status of the peripheral
     * @param l
     */
    public void setBluetoothBondStatusListener(BluetoothBondStatusListener l) {
        mBluetoothBondStatusListener = l;
    }

    /**
     * The  listener of the scanning status
     * @param l
     */
    public void setBluetoothLeScanListener(BluetoothLeScanListener l) {
        this.mBluetoothLeScanListener = l;
    }

    public void setBluetoothLeSessionListener(BluetoothLeSessionListener l) {
        this.mBluetoothLeSessionListener = l;
    }

    public void startScan(long timeout) {
        if (mBluetoothAdapter == null)
            return;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, timeout);

        mBluetoothAdapter.startLeScan(mLeScanCallback);

        if (mBluetoothLeScanListener != null) {
            mBluetoothLeScanListener.onStartScan();
        }
    }

    public void stopScan() {
        if (mBluetoothAdapter == null)
            return;

        mBluetoothAdapter.stopLeScan(mLeScanCallback);

        if (mBluetoothLeScanListener != null) {
            mBluetoothLeScanListener.onStopScan();
        }
    }

    public void connect(BluetoothDevice device) {
        if (mContext == null || device == null)
            return;

        device.connectGatt(mContext, false, mBluetoothGattCallback);
    }

    public void init() {
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        if (mContext != null) {
            mContext.registerReceiver(mBluetoothStatusReceiver, intentFilter);

            if (DEG) Log.d(TAG, "Register the receiver for the Bluetooth status.");
        }
    }

    public void release() {
        if (mContext != null) {
            mContext.unregisterReceiver(mBluetoothStatusReceiver);

            if (DEG) Log.d(TAG, "Unregister the receiver for the Bluetooth status.");
        }
    }

    public BluetoothLeSession getCurrentSession() {
        return mSessionManager.getSession();
    }

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothLeSession session;
            switch (status) {
                case BluetoothGatt.STATE_CONNECTED:
                    session = new BluetoothLeSessionImpl(gatt);
                    // Store the current session
                    mSessionManager.setSession(session);

                    if (mBluetoothLeSessionListener != null) {
                        mBluetoothLeSessionListener.onConnect(session);
                    }

                    gatt.discoverServices();
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    session = getCurrentSession();
                    // Release the session
                    mSessionManager.release();

                    if (mBluetoothLeSessionListener != null) {
                        mBluetoothLeSessionListener.onDisconnect(session);
                    }

                    break;
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothLeSession session = getCurrentSession();
            if (session != null) {
                session.setBluetoothGatt(gatt);
                session.loadService(gatt.getServices());
            }

            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }
    };

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            // Device found
            if (mBluetoothLeScanListener != null) {
                mBluetoothLeScanListener.onResult(device);
            }

            if (DEG) Log.d(TAG, String.format("Bluetooth device found: %s, MAC: %s",
                    device.getName(), device.getAddress()));
        }
    };


    private final BroadcastReceiver mBluetoothStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Bluetooth device status change
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (mBluetoothStatusListener != null) {
                    int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    switch (status) {
                        case BluetoothAdapter.STATE_ON:
                            mBluetoothStatusListener.onEnable();
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            mBluetoothStatusListener.onDisable();
                            break;
                        default:break;
                    }
                }
                return;
            }

            // Bond status changed
            if (action.equals(android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                if (mBluetoothBondStatusListener != null) {
                    int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                    // Retrieve the device information
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    switch (status) {
                        case BluetoothDevice.BOND_BONDED:
                            mBluetoothBondStatusListener.onBonded(device);
                            break;
                        case BluetoothDevice.BOND_NONE:
                            mBluetoothBondStatusListener.onUnBond(device);
                            break;
                        default:break;
                    }
                }
                return;
            }
        }
    };
}
