package com.soxfmr.realtemp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.soxfmr.realtemp.bluetooth.contract.BluetoothBondStatusListener;
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

    private BluetoothStatusListener mBluetoothStatusListener;
    private BluetoothBondStatusListener mBluetoothBondStatusListener;

    private BluetoothLeManager() {}

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

    public void setBluetoothStatusListener(BluetoothStatusListener l) {
        mBluetoothStatusListener = l;
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
