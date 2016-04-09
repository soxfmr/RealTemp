package com.soxfmr.realtemp.ui;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.soxfmr.realtemp.R;
import com.soxfmr.realtemp.adapter.BluetoothDeviceAdapter;
import com.soxfmr.realtemp.bluetooth.BluetoothLeManager;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothBondStatusListener;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeScanListener;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothStatusListener;

import java.util.ArrayList;
import java.util.List;

public class DeviceActivity extends AppCompatActivity {
    public static final String TAG = DeviceActivity.class.getName();

    private ListView mDeviceListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<BluetoothDevice> mBluetoothDeviceList;
    private BluetoothDeviceAdapter mBluetoothDeviceAdapter;

    private BluetoothLeManager mBluetoothLeManager;

    private long scanningTimeout = 10000;

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

        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary));

        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothLeManager = BluetoothLeManager.getInstance();
        mBluetoothLeManager.setContext(getApplicationContext());
        mBluetoothLeManager.setBluetoothAdapter(manager.getAdapter());

        mBluetoothLeManager.setBluetoothStatusListener(new BluetoothStatusListener() {
            @Override
            public void onEnable() {

            }

            @Override
            public void onDisable() {

            }
        });

        mBluetoothLeManager.setBluetoothBondStatusListener(new BluetoothBondStatusListener() {
            @Override
            public void onBonded(BluetoothDevice device) {
                mBluetoothDeviceAdapter.notifyDataSetChanged();
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
                }
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
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mBluetoothLeManager.startScan(scanningTimeout);
            }
        });
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
