package com.soxfmr.realtemp.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BluetoothService extends Service {
    public BluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
