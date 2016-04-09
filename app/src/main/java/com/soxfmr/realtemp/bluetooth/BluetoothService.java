package com.soxfmr.realtemp.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.IBinder;

import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSession;
import com.soxfmr.realtemp.utils.GattHelper;

public class BluetoothService extends Service {
    public static final String TAG = BluetoothService.class.getName();

    private static final int MAX_TIMEOUT_COUNT = 5;

    private static final byte[] READ_HEART_RATE_INSTRUCTION = { 0x4F, 0x4B, 0x2B, 0x04, 0x12, 0x16, 0x54 };

    public static final String ACTION_HEART_BEAT_PACKET = "heart_beat";
    public static final String ACTION_DESTROY_SESSION = "destroy_session";

    private BluetoothLeSession mBluetoothLeSession;

    private static boolean RUN = false;
    private static int timeoutCounter = 0;
    public static boolean WAIT_READ_FLAG = false;

    private BluetoothGattCharacteristic mHeartRateCharacteristic;

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
            mBluetoothLeSession = sessionManager.getSession();

            if (action.equals(ACTION_HEART_BEAT_PACKET)) {
                if (mBluetoothLeSession != null) {
                    RUN = true;
                    // Enable the notification
                    mHeartRateCharacteristic = mBluetoothLeSession.getCharacteristic(
                            GattHelper.buildBaseServiceUUID(GattHelper.BASE_SERVICE_HEART_RATE_MEASURE)
                    );
                    mBluetoothLeSession.enableNotification(mHeartRateCharacteristic, true);

                    Thread heartBeatThread = new HeartBeatThread();
                    Thread receiveThread = new ReceiveThread();

                    heartBeatThread.start();
                    receiveThread.start();
                }

            } else if (action.equals(ACTION_DESTROY_SESSION)) {
                if (mBluetoothLeSession != null && ! mBluetoothLeSession.isClosed()) {
                    RUN = false;
                    mBluetoothLeSession.destroy();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private class HeartBeatThread extends Thread {

        @Override
        public void run() {
            while (RUN) {
                try {
                    if (! WAIT_READ_FLAG || timeoutCounter++ >= MAX_TIMEOUT_COUNT) {
                        mBluetoothLeSession.write(mHeartRateCharacteristic, READ_HEART_RATE_INSTRUCTION);

                        timeoutCounter = 0;
                        WAIT_READ_FLAG = true;
                    }
                    Thread.currentThread().sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class ReceiveThread extends Thread {

        @Override
        public void run() {
            while (RUN) {
                try {
                    if (WAIT_READ_FLAG) {
                        mBluetoothLeSession.read(mHeartRateCharacteristic);
                    }
                    Thread.currentThread().sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
