package com.soxfmr.realtemp.ui;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();

    private static final int MSG_CHANGE_PROGRESS = 0x2;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private ListView mScheduleListView;
    private MaruisProgress mMaruisProgress;

    private ScheduleAdapter mScheduleAdapter;
    private List<ScheduleInfo> mScheduleInfoList;

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHANGE_PROGRESS:
                    mMaruisProgress.setProgress(msg.arg1);
                    break;
            }
            super.handleMessage(msg);
        }
    };

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
                if (value != null && value.length == 5) {
                    final int temperature = value[4] * 100 + value[3] * 10 + value[2];
                    final int progress = mMaruisProgress.getProgress();
                    if (temperature > progress) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    for (int i = progress; i <= temperature; i++) {
                                        mHandler.obtainMessage(MSG_CHANGE_PROGRESS, i, 0).sendToTarget();
                                        Thread.sleep(80);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } else if (temperature < progress) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    for (int i = progress; i >= temperature; i--) {
                                        mHandler.obtainMessage(MSG_CHANGE_PROGRESS, i, 0).sendToTarget();
                                        Thread.sleep(80);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }

                Log.w(TAG, "Characteristic read " + Arrays.toString(characteristic.getValue()));
            }
        });

        Intent intent = new Intent(this, BluetoothService.class);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (int i = 0, len = 20; i < len; i++) {
            ScheduleInfo info = new ScheduleInfo();
            info.setTitle("Schedule " + i);
            info.setTriggeredAt(dateFormat.format(new Date()));
            mScheduleInfoList.add(info);
        }
        mScheduleAdapter = new ScheduleAdapter(this, mScheduleInfoList);

        mScheduleListView.addHeaderView(headerView);
        mScheduleListView.setAdapter(mScheduleAdapter);
    }

    @Override
    public void finish() {
        Intent intent = new Intent(this, BluetoothService.class);
        stopService(intent);

        super.finish();
    }
}
