package com.arvihealthscanner.btScan.java.services;

/**
 * Created by admin on 18/07/2018.
 */

public interface SlaveListener {
    void processResponseFromSlave(byte[] data);
    void every100ms();
}
