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
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.soxfmr.realtemp.bluetooth.contract.BluetoothBondStatusListener;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeScanListener;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSession;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSessionListener;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothStatusListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    private boolean mScanning = false;

    private BluetoothLeManager() {
        mSessionManager = new SessionManager(mContext);
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

    public SessionManager getSessionManager() {
        return mSessionManager;
    }

    public boolean isScanning() {
        return mScanning;
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

    public boolean isEnable() {
        if (mBluetoothAdapter == null)
            return false;

        return mBluetoothAdapter.isEnabled();
    }

    public void enable() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mContext.startActivity(intent);
    }

    public void disable() {
        mBluetoothAdapter.disable();
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

        mScanning = true;

        if (mBluetoothLeScanListener != null) {
            mBluetoothLeScanListener.onStartScan();
        }
    }

    public void stopScan() {
        if (mBluetoothAdapter == null)
            return;

        mBluetoothAdapter.stopLeScan(mLeScanCallback);

        mScanning = false;

        if (mBluetoothLeScanListener != null) {
            mBluetoothLeScanListener.onStopScan();
        }
    }

    public boolean bond(BluetoothDevice device) {
        return device.createBond();
    }

    public boolean unbound(BluetoothDevice device) {
        boolean bRet = false;
        try {
            Method method = BluetoothDevice.class.getMethod("removeBond", null);
            bRet = (boolean) method.invoke(device);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return bRet;
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        if (mContext != null) {
            mContext.registerReceiver(mBluetoothStatusReceiver, intentFilter);

            if (DEG) Log.d(TAG, "Register the receiver for the Bluetooth status.");
        }
    }

    public void unregister() {
        if (mContext != null) {
            mContext.unregisterReceiver(mBluetoothStatusReceiver);

            if (DEG) Log.d(TAG, "Unregister the receiver for the Bluetooth status.");
        }
    }

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

                            if (DEG) Log.d(TAG, String.format("Bluetooth device bond status change: %s, MAC: %s",
                                    device.getName(), device.getAddress()));
                            break;
                        case BluetoothDevice.BOND_NONE:
                            mBluetoothBondStatusListener.onUnBond(device);

                            if (DEG) Log.d(TAG, String.format("Bluetooth device bond status change: %s, MAC: %s",
                                    device.getName(), device.getAddress()));
                            break;
                        default:break;
                    }
                }
                return;
            }
        }
    };
}
