package com.soxfmr.realtemp.ui;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.jaeger.library.StatusBarUtil;
import com.soxfmr.realtemp.R;
import com.soxfmr.realtemp.adapter.ScheduleAdapter;
import com.soxfmr.realtemp.bluetooth.BluetoothLeManager;
import com.soxfmr.realtemp.bluetooth.BluetoothService;
import com.soxfmr.realtemp.bluetooth.SessionManager;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSession;
import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSessionListener;
import com.soxfmr.realtemp.model.ScheduleInfo;
import com.soxfmr.realtemp.view.MaruisProgress;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private ListView mScheduleListView;
    private MaruisProgress mMaruisProgress;

    private ScheduleAdapter mScheduleAdapter;
    private List<ScheduleInfo> mScheduleInfoList;

    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        setupDrawerView();
        setupSchedule();

        StatusBarUtil.setColorForDrawerLayout(this, mDrawerLayout,
                getResources().getColor(R.color.colorPrimary));

        SessionManager sessionManager = BluetoothLeManager.getInstance().getSessionManager();
        sessionManager.setBluetoothLeSessionListener(new BluetoothLeSessionListener() {
            @Override
            public void onConnect(BluetoothLeSession session) {}

            @Override
            public void onDisconnect(boolean unexpected) {}

            @Override
            public void onReceive(BluetoothGattCharacteristic characteristic) {
                byte[] value = characteristic.getValue();
                if (value != null) {
                    for (int i = 0; i < value.length; i++) {
                        Log.w(TAG, value[i] + "");
                    }
                    BluetoothService.WAIT_READ_FLAG = false;
                }
            }

            @Override
            public void onReceive(BluetoothGattDescriptor descriptor) {}
        });

        Intent intent = new Intent(this, BluetoothService.class);
        intent.setAction(BluetoothService.ACTION_HEART_BEAT_PACKET);
        startService(intent);
    }

    private void setupDrawerView() {
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);

        DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this);
        drawerArrowDrawable.setColor(getResources().getColor(R.color.colorIcons));
        mToolbar.setNavigationIcon(drawerArrowDrawable);

        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.common_drawer_open, R.string.common_drawer_close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
    }

    private void setupSchedule() {
        View headerView = getLayoutInflater().inflate(R.layout.main_header, null);

        mMaruisProgress = (MaruisProgress) headerView.findViewById(R.id.main_measurer);
        mMaruisProgress.addRangeColor(30, getResources().getColor(R.color.md_light_green_600));
        mMaruisProgress.addRangeColor(60, getResources().getColor(R.color.md_yellow_600));
        mMaruisProgress.addRangeColor(100, getResources().getColor(R.color.md_red_400));

        mScheduleListView = (ListView) findViewById(R.id.main_lv_schedule);

        mScheduleInfoList = new ArrayList<>();
        mScheduleAdapter = new ScheduleAdapter(this, mScheduleInfoList);

        mScheduleListView.addHeaderView(headerView);
        mScheduleListView.setAdapter(mScheduleAdapter);
    }

    @Override
    public void finish() {
        Intent intent = new Intent(this, BluetoothService.class);
        intent.setAction(BluetoothService.ACTION_DESTROY_SESSION);
        stopService(intent);

        super.finish();
    }
}
