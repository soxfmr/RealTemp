package com.soxfmr.realtemp.ui;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jaeger.library.StatusBarUtil;
import com.soxfmr.realtemp.R;
import com.soxfmr.realtemp.adapter.BluetoothDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceActivity extends AppCompatActivity {

    private ListView mDeviceListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<BluetoothDevice> mBluetoothDeviceList;
    private BluetoothDeviceAdapter mBluetoothDeviceAdapter;

    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        init();
    }

    private void init() {
        setupDeviceListView();
        setupSwipeRefreshLayout();

        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary));
    }

    private void setupDeviceListView() {
        mDeviceListView = (ListView) findViewById(R.id.device_listview_device);

        mBluetoothDeviceList = new ArrayList<>();
        mBluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, mBluetoothDeviceList);

        mDeviceListView.setAdapter(mBluetoothDeviceAdapter);
        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.device_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 5000);
            }
        });
    }
}
