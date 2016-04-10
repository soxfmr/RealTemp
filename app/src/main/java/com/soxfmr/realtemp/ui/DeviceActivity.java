package com.soxfmr.realtemp.ui;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.soxfmr.realtemp.R;
import com.soxfmr.realtemp.adapter.BluetoothDeviceAdapter;
import com.soxfmr.realtemp.bluetooth.BluetoothLeManager;
import com.soxfmr.realtemp.bluetooth.SessionManager;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothBondStatusListener;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeScanListener;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSession;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSessionListener;

import java.util.ArrayList;
import java.util.List;

public class DeviceActivity extends AppCompatActivity {
    public static final String TAG = DeviceActivity.class.getName();

    private ListView mDeviceListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<BluetoothDevice> mBluetoothDeviceList;
    private BluetoothDeviceAdapter mBluetoothDeviceAdapter;

    private BluetoothDevice mBluetoothDevice;
    private BluetoothLeManager mBluetoothLeManager;

    private long scanningTimeout = 10000;
    private long connectTimeout = 10000;
    private boolean bAutoConnect = true;

    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        if (! getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.device_bluetooth_le_not_support,
                    Toast.LENGTH_LONG).show();
            finish();
        }

        init();
    }

    private void init() {
        setupDeviceListView();
        setupSwipeRefreshLayout();

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary));
        }

        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothLeManager = BluetoothLeManager.getInstance();
        mBluetoothLeManager.setContext(this);
        mBluetoothLeManager.setBluetoothAdapter(manager.getAdapter());

        mBluetoothLeManager.setBluetoothBondStatusListener(new BluetoothBondStatusListener() {
            @Override
            public void onBonded(BluetoothDevice device) {
                mBluetoothDeviceAdapter.notifyDataSetChanged();

                if (mBluetoothDevice != null &&
                        mBluetoothDevice.getAddress().equals(device.getAddress())) {
                    createSession(device);
                }
            }

            @Override
            public void onUnBond(BluetoothDevice device) {
                mBluetoothDeviceAdapter.notifyDataSetChanged();
            }
        });

        mBluetoothLeManager.setBluetoothLeScanListener(new BluetoothLeScanListener() {
            @Override
            public void onStartScan() {
                mBluetoothDeviceList.clear();
                mBluetoothDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onStopScan() {
                mSwipeRefreshLayout.setRefreshing(false);

                Toast.makeText(DeviceActivity.this, R.string.device_bluetooth_scan_finish,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResult(BluetoothDevice device) {
                mBluetoothDeviceList.add(device);
                mBluetoothDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupDeviceListView() {
        mDeviceListView = (ListView) findViewById(R.id.device_listview_device);

        mBluetoothDeviceList = new ArrayList<>();
        mBluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, mBluetoothDeviceList);

        mDeviceListView.setAdapter(mBluetoothDeviceAdapter);
        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = mBluetoothDeviceList.get(position);

                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    mBluetoothLeManager.bond(device);
                    // latest device to be bond
                    mBluetoothDevice = device;
                    return;
                }

                createSession(device);
            }
        });

        mDeviceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final BluetoothDevice device = mBluetoothDeviceList.get(position);
                // No in the bonded status
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    return false;
                }
                // Show the query dialog
                new AlertDialog.Builder(DeviceActivity.this)
                        .setTitle(R.string.device_bluetooth_un_bond)
                        .setPositiveButton(R.string.common_dialog_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBluetoothLeManager.unbound(device);
                            }
                        })
                        .setNegativeButton(R.string.common_dialog_cancel, null)
                        .show();

                return true;
            }
        });
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.device_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (! mBluetoothLeManager.isEnable()) {
                    mBluetoothLeManager.enable();
                    mSwipeRefreshLayout.setRefreshing(false);
                    return;
                }
                mBluetoothLeManager.startScan(scanningTimeout);
            }
        });
    }

    private void createSession(BluetoothDevice device) {
        // Progress dialog
        final ProgressDialog dialog = new ProgressDialog(DeviceActivity.this);
        dialog.setMessage(getString(R.string.device_bluetooth_try_to_create_session));

        // Start the Bluetooth session
        SessionManager sessionManager = mBluetoothLeManager.getSessionManager();
        sessionManager.setBluetoothLeSessionListener(new BluetoothLeSessionListener() {
            @Override
            public void onConnect(BluetoothLeSession session) {
                dialog.dismiss();
                // Session created, move on to MainActivity
                Intent intent = new Intent(DeviceActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onDisconnect(boolean unexpected) {}

            @Override
            public void onReceive(BluetoothGattCharacteristic characteristic) {}
        });
        sessionManager.create(device, bAutoConnect);
        dialog.show();

        // Connect timeout
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();

                Toast.makeText(DeviceActivity.this, R.string.device_bluetooth_connect_timeout,
                        Toast.LENGTH_SHORT).show();
            }
        }, connectTimeout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBluetoothLeManager.register();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothLeManager.unregister();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mBluetoothLeManager.isScanning()) {
            mBluetoothLeManager.stopScan();
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }
}
