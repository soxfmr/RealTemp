package com.soxfmr.realtemp.bluetooth;

import com.soxfmr.realtemp.bluetooth.contract.BluetoothLeSession;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public class SessionManager {

    private BluetoothLeSession mSession;

    public BluetoothLeSession getSession() {
        return mSession;
    }

    public void setSession(BluetoothLeSession session) {
        this.mSession = session;
    }

    public void release() {
        mSession = null;
    }
}
