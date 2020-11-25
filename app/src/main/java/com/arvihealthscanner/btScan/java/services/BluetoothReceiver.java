package com.arvihealthscanner.btScan.java.services;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothReceiver extends BroadcastReceiver {

    private static final String TAG = "EMS BTReceiver";
    public static boolean bluetoothOn = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        Log.d(TAG,action);
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1))
            {
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG,"BT ON");
                    bluetoothOn = true;
                    break;

                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG,"BT OFF");
                    bluetoothOn = false;
                    break;

                case BluetoothAdapter.STATE_DISCONNECTED:
                    Log.d(TAG,"BT DEVICE DISCONNECTED");
                    bluetoothOn = false;
                    break;
            }
        }
    }
}
