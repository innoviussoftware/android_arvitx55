package com.arvihealthscanner.btScan.java.services;

/**
 * Created by admin on 18/07/2018.
 */

public interface BluetoothListener {
    public void onConnected();
    public void onReceived(byte[] data);
    public void onException(Exception e);
    public void onDisconnected();
}
