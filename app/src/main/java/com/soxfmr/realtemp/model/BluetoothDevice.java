package com.soxfmr.realtemp.model;

import java.util.UUID;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public class BluetoothDevice {

    private String name;

    private String macAddress;

    private UUID uuid;

    private int status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
