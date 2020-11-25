package com.arvihealthscanner.btScan.java.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


import com.arvihealthscanner.btScan.java.arvi.Config;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


/**
 * Created by admin on 18/07/2018.
 */

public class SlaveService extends Service implements BluetoothListener {

    private static final String TAG = "EMS SlaveService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private static enum STATE {UNKNOWN, WAIT_FOR_BT_ON, WAIT_FOR_BT_PAIR, WAIT_FOR_BT_OFF, CONNECTED, DISCONNECTED}

    ;
    private STATE state = STATE.UNKNOWN, previousState = STATE.UNKNOWN;
    private BluetoothClient BThandler;
    private Thread workerThread = null;
    private final SyncPacket rxPacket = SyncPacket.getRxPacket();
    private IBinder mBinder = new MyServiceBinder();
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mDevice = null;
    private Timer timer;
    private SlaveListener listener;

    private boolean isThreadRunning = false;
    public byte[] rxData = null, txData;
    private int stateTimeout = 0, rxTimeout = 0;
    public static boolean connected, slaveOff = true, serviceOn = false;
    private static int slaveDetachedTimeout = 0;
    public static int triggerTimeout = 0;
    private static boolean bluetoothPaired, noResponse = false;
    public static String temperature = null;
    //todo:: oximeter reading start
    public static int[] oximeter;

    //todo:: end

    public SlaveService() {
    }

    public class MyServiceBinder extends Binder {
        public SlaveService getService() {
            return SlaveService.this;
        }
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            String action = intent.getAction();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                every100ms();
                if (listener != null) {
                    listener.every100ms();
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 100);
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "In onStartCommend, thread id: " + Thread.currentThread().getId());

        if (!isThreadRunning) {
            Log.d(TAG, "thread not running. starting workerThread");
            isThreadRunning = true;
            workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "In WorkerThread, thread id: " + Thread.currentThread().getId());
                    while (isThreadRunning) {
                        try {
                            stateMachine();
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            workerThread.start();
        } else {
            Log.d(TAG, "thread already running");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service Destroyed");
        //stopBluetooth();
        unregisterReceiver(mBatInfoReceiver);
        timer.cancel();
        timer.purge();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "In OnBind");
        return mBinder;
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected");
        connected = true;
        bluetoothPaired = true;
    }

    @Override
    public void onReceived(byte[] data) {
        Log.d(TAG, "onReceived - " + Helper.byteArrayToHexString(data));
        if (rxPacket.rxHandler(data)) {
            if (rxPacket.isValid()) {
                rxData = new byte[rxPacket.getSize()];
                System.arraycopy(rxPacket.buff, 0, rxData, 0, rxData.length);
                processResponseFromSlave(this.rxData);
            }
        }
    }

    @Override
    public void onException(Exception e) {
        Log.d(TAG, "onException - " + e.getMessage());
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
        connected = false;
        bluetoothPaired = false;
    }

    private boolean connectToBluetooth() {
        Log.d(TAG, "connectToBluetooth");
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    //    if (device.getAddress().equalsIgnoreCase("00:19:10:08:C8:11")) {
                    //   if (device.getAddress().equalsIgnoreCase("98:D3:21:FC:86:64")) {
                    if (device.getAddress().equalsIgnoreCase(Config.MAC_ADDRESS)) {
                        mDevice = device;
                        Log.d(TAG, "Device " + mDevice.getName() + "Found");
                        break;
                    }
                }
            }

            if (mDevice != null) {
                try {
                    Log.d(TAG, "creating BT socket");
                    BThandler = new BluetoothClient(mDevice.createRfcommSocketToServiceRecord(MY_UUID), "", SlaveService.this);
                    if (BThandler.connect()) {
                        slaveDetachedTimeout = 12000; //20 mins x 60 sec x 10
                        if (slaveOff) {
                            slaveOff = false;
                        }
                        Log.d(TAG, "socket found");
                        return true;
                    } else {
                        if (slaveDetachedTimeout == 0) {
                            if (!slaveOff) {
                                slaveOff = true;
                                //log slave detached
                            }
                        }
                        Log.d(TAG, "socket not found");
                        BThandler.disconnect();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "BT socket creation exception: " + e.getMessage());
                }
                // Establish the Bluetooth socket connection.
           /* try {
                Log.d(TAG, "connecting BT socket");
                BThandler.connect();

            } catch (Exception e) {

                Log.e(TAG, "connecting BT socket exception: " + e.getMessage());
                try {
                    Log.d(TAG, "closing BT socket");
                    BThandler.disconnect();
                } catch (Exception ex) {
                    // insert code to deal with this
                    Log.e(TAG, "closing BT socket exception: " + ex.getMessage());
                }
            } */
            } else {
                Log.d(TAG, "Device Not Found");
            }
        }
        return false;
    }

    private boolean startBluetooth() {
        Log.d(TAG, "startBluetooth");
        bluetoothPaired = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "Device does not support bluetooth");
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth is Already ON");
                BluetoothReceiver.bluetoothOn = true;
                connectToBluetooth();
                return true;
            } else {
                Log.d(TAG, "Turning ON Bluetooth");
                return mBluetoothAdapter.enable();
            }
        }
        return false;
    }

    private void stopBluetooth() {
        Log.d(TAG, "stopBluetooth()");
        if (mBluetoothAdapter != null) {
            if (BThandler != null) {
                BThandler.disconnect();
            }
            mBluetoothAdapter.disable();
        }
    }

    public void stateMachine() {
        if (state != previousState) {
            Log.d(TAG, previousState.toString() + "--->" + state.toString());
            previousState = state;
        }

        if (!connected && state == STATE.CONNECTED) {
            Log.d(TAG, "not connected but state is CONNECTED");
            // stopBluetooth();
            state = STATE.DISCONNECTED;
        }

        switch (state) {
            case WAIT_FOR_BT_ON:
                if (BluetoothReceiver.bluetoothOn || mBluetoothAdapter.isEnabled()) {
                    stateTimeout = 0;
                    this.state = STATE.WAIT_FOR_BT_PAIR;
                }
                break;
            case WAIT_FOR_BT_PAIR:
                if (SlaveService.bluetoothPaired) {
                    this.state = STATE.CONNECTED;
                } else if (stateTimeout == 0) {

                    stateTimeout = 50;
                    connectToBluetooth();
                }
                break;
            case CONNECTED:
                if (!serviceOn) {
                    stopBluetooth();
                    this.state = STATE.WAIT_FOR_BT_OFF;
                }
                break;

            case WAIT_FOR_BT_OFF:
                if (!mBluetoothAdapter.isEnabled()) {
                    stateTimeout = 0;
                    this.state = STATE.DISCONNECTED;
                }
                break;
            case DISCONNECTED:
                if (serviceOn) {
                    startBluetooth();
                    this.state = STATE.WAIT_FOR_BT_ON;
                }
                break;
            default:
                this.state = STATE.DISCONNECTED;
                break;
        }
    }


    private void processResponseFromSlave(byte[] data) {
        Log.d(TAG, "processResponseFromSlave " + Helper.byteArrayToHexString(data));

        short command = Helper.getUint8(data, 1);
//todo:: oximeter reading start
        int size = Helper.getUint16_LE(data, 2);
//todo:: end
        Log.d(TAG, "case " + String.format("%02X", command));


        switch (command) {
            case 0xB1: {
                {
                    if (Helper.getUint16_LE(data, 2) >= 31) {
                        String detecetdTemp = Helper.getString(data, 4, 31);
                        String[] str = detecetdTemp.split("-");
                        if (str.length >= 3) {
                            str = str[2].split(":");
                            if (str.length >= 2) {
                                //mService.greenLEDon(10);
                                temperature = str[1].substring(0, 5);
                            } else {
                            }
                        } else {
                        }
                    } else {
                    }

                }
            }
            break;
            case 0xC1: {


            }
            break;
//todo:: oximeter reading start
            case 0xD1: {
                if (size >= 5) {
                    oximeter = new int[5];
                    for (int i = 0; i < 5; i++) {
                        oximeter[i] = Helper.getUint8(data[4 + i]);
                    }
                }
            }
            break;
//todo:: end
        }
    }

    public int redLEDon() {
        return operateOutput(1, 7, 0, 1, 1, 1, 1);
    }

    public int redLEDon(int time100Ms) {
        return operateOutput(1, 7, 0, 1, time100Ms, 1, 2);
    }

    public int redLEDblink(int on, int off, int blinks) {
        return operateOutput(1, 7, 0, 1, on, off, blinks * 2);
    }

    public int redLEDblink(int blinks) {
        return operateOutput(1, 7, 0, 1, 1, 10, blinks * 2);
    }

    public int redLEDoff() {
        return operateOutput(1, 7, 0, 0, 1, 1, 1);
    }

    public int greenLEDon() {
        return operateOutput(1, 6, 0, 1, 1, 1, 1);
    }

    public int greenLEDon(int time100Ms) {
        return operateOutput(1, 6, 0, 1, time100Ms, 1, 2);
    }

    public int greenLEDblink(int on, int off, int blinks) {
        return operateOutput(1, 6, 0, 1, on, off, blinks * 2);
    }

    public int greenLEDblink(int blinks) {
        return operateOutput(1, 6, 0, 1, 1, 10, blinks * 2);
    }

    public int greenLEDoff() {
        return operateOutput(1, 6, 0, 0, 1, 1, 1);
    }

    public int sanitizerSpray(int delay100ms, int time100Ms) {
        return operateOutput(0, 13, 0, 0, delay100ms, time100Ms, 3);
    }

    public int sanitizerOn() {
        return operateOutput(0, 13, 0, 1, 1, 1, 1);
    }

    public int sanitizerOff() {
        return operateOutput(0, 13, 0, 0, 1, 1, 1);
    }

    public void readTemperature(int timeout100ms) {
        //return "TR1:34.2C-TR2:36.2C-TP:35.2C";
        byte[] tx = new byte[9];
        Helper.setUint8(tx, 0, (short) 0);
        Helper.setUint8(tx, 1, (short) 14);
        Helper.setUint8(tx, 2, (short) 0);
        Helper.setUint8(tx, 3, (short) 1);
        Helper.setUint8(tx, 4, (short) 2);
        Helper.setUint8(tx, 5, (short) 1);
        Helper.setUint8(tx, 6, (short) 2);
        Helper.setUint8(tx, 7, (short) timeout100ms);
        Helper.setUint8(tx, 8, (short) 31);
        temperature = null;
        sendRequest(0xB1, tx, timeout100ms);
        triggerTimeout = 600;
    }

    //todo:: oximenter reading start
    public void readOximeter(int val)
    {
        byte[] tx=new byte[8];
        Helper.setUint8(tx,0,(short)0);
        Helper.setUint8(tx,1, (short) 10);
        Helper.setUint8(tx,2, (short) 0);
        Helper.setUint8(tx,3, (short) 1);
        Helper.setUint8(tx,4,(short)2);
        Helper.setUint8(tx,5,(short)1);
        Helper.setUint8(tx,6,(short)2);
        Helper.setUint8(tx,7,(short)val);
        oximeter=null;
        sendRequest(0xD1,tx,2);
    }
    //todo:: end


    public int operateOutput(int port, int pin, int ppod, int state, int pw, int mw, int np) {
        byte[] tx = new byte[7];
        Helper.setUint8(tx, 0, (short) port);
        Helper.setUint8(tx, 1, (short) pin);
        Helper.setUint8(tx, 2, (short) ppod);
        Helper.setUint8(tx, 3, (short) state);
        Helper.setUint8(tx, 4, (short) pw);
        Helper.setUint8(tx, 5, (short) mw);
        Helper.setUint8(tx, 6, (short) np);
        sendRequest(0xC1, tx, 5);
        switch (np) {
            case 0:
            case 1:
                return 2;
            case 2:
                return pw + 1;
            case 3:
                return pw + mw + 1;
            default:
                if (np % 2 == 0) {
                    return ((np / 2) * (pw + mw) - mw) + 1;
                } else {
                    return ((np / 2) * (pw + mw)) + 1;
                }

        }
    }

    public boolean sendRequest(int cmd, byte[] data, int timeout) {
        this.rxTimeout = timeout / 100;
        if (this.rxTimeout <= 0) {
            this.rxTimeout = 1; //1 = 100 ms
        }
        noResponse = false;
        return sendPacket(SyncPacket.START_BYTE, cmd, data);
    }

    private boolean sendPacket(short startbyte, int seqNo, byte[] data) {
        SyncPacket txPacket = SyncPacket.getTxPacket(startbyte, seqNo, data);
        Log.d(TAG, "sendPacket - " + Helper.byteArrayToHexString(txPacket.buff));
        try {
            if (this.BThandler != null) {
                this.BThandler.send(txPacket.buff);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void addListener(SlaveListener listener) {
        Log.d(TAG, "addListener");
        this.listener = listener;
    }

    public void removeListener() {
        Log.d(TAG, "removeListener");
        this.listener = null;
    }

    public void every100ms() {
        if (this.stateTimeout > 0) {
            this.stateTimeout--;
        }
        if (triggerTimeout > 0) {
            triggerTimeout--;
        }
        if (this.rxTimeout > 0) {
            this.rxTimeout--;
            if (this.rxTimeout == 0) {
                this.noResponse = true;
            }
        }
        if (slaveDetachedTimeout > 0)
            slaveDetachedTimeout--;
        this.rxPacket.checkCharTimeout();
    }
}
