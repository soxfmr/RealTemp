package com.soxfmr.realtemp.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.IBinder;

import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSession;
import com.soxfmr.realtemp.utils.GattHelper;

public class BluetoothService extends Service {
    public static final String TAG = BluetoothService.class.getName();

    public final static byte[] SEND_INSTRUCTIONS = { 0x4F, 0x4B, 0x2B, 0x04, 0x12, 0x16, 0x54 };

    BluetoothGattCharacteristic characteristic;

    private static boolean RUN = true;

    public BluetoothService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            SessionManager sessionManager = BluetoothLeManager.getInstance()
                    .getSessionManager();
            final BluetoothLeSession session = sessionManager.getSession();
            if (session != null) {
                RUN = true;
                characteristic = session.getCharacteristic(GattHelper.BASE_SERVICE_HEART_RATE_MEASURE);
                 session.enableNotification(characteristic, true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (RUN) {
                                session.write(characteristic, SEND_INSTRUCTIONS);
                                Thread.sleep(2000);
                            }
                        } catch (InterruptedException e) {}
                    }
                }).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (RUN) {
                                session.read(characteristic);
                                Thread.sleep(2000);
                            }
                        } catch (InterruptedException e) {}
                    }
                }).start();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        RUN = false;
        super.onDestroy();
    }
}
