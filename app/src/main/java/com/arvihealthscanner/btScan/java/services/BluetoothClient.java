package com.arvihealthscanner.btScan.java.services;

import android.bluetooth.BluetoothSocket;

import java.io.InputStream;

/**
 * Created by admin on 18/07/2018.
 */

public class BluetoothClient extends Thread{
    private final boolean nonBlockingMode;
    public final String mac;
    private BluetoothSocket mySocket;
    private boolean stop;
    private boolean remoteConnected;
    private BluetoothListener listener;
    private void addListener(BluetoothListener listener)
    {
        this.listener=listener;
    }
    private void removeListener()
    {
        this.listener=null;
    }
    public BluetoothClient(BluetoothSocket mySocket,String mac,BluetoothListener listener)
    {
        this.nonBlockingMode=true;
        this.mySocket=mySocket;
        this.mac=mac;
        this.stop=false;
        if(nonBlockingMode)
        {
            addListener(listener);
            this.start();
        }
    }
    @Override
    public void run()  {
        try{
            byte[] data;
            while(!this.stop)
            {
                if(this.mySocket!=null)
                {
                    while(this.mySocket.isConnected())//&& !this.mySocket.isClosed())
                    {
                        data=this.receive();
                        if(data==null)
                        {
                            this.mySocket.close();
                            this.remoteConnected=false;
                            if(this.listener!=null)
                                this.listener.onDisconnected();
                            break;
                        }
                        else if(data.length>0)
                        {
                            if(this.listener!=null)
                                this.listener.onReceived(data);
                        }
                        sleep(10);
                    }
                }
                else
                {
                    sleep(1000);
                }
            }
            if(!this.mySocket.isConnected())//.isClosed())
                this.mySocket.close();
        }
        catch(Exception e)
        {
            if(this.listener!=null)
                this.listener.onException(e);
        }

    }
    private boolean tryToConnect()
    {
        try{
            System.out.println("connecting to "+this.mac);
            //this.mySocket=new Socket(this.ip, this.mac);
            this.mySocket.connect();
            return true;
        }
        catch(Exception e)
        {
            if(this.listener!=null)
                this.listener.onException(e);
        }
        return false;
    }
    public boolean connect()
    {
        this.remoteConnected=this.tryToConnect();
        if(this.remoteConnected)
        {
            if(this.nonBlockingMode)
            {
                if(this.listener!=null)
                    this.listener.onConnected();
            }
        }
        return remoteConnected;
    }
    public void disconnect()
    {
        try{
            this.stop=true;
            if(this.mySocket!=null)
            {
                if (!this.mySocket.isConnected())//.isClosed())
                    this.mySocket.close();
            }
            if(this.nonBlockingMode)
            {
                if(this.remoteConnected)
                {
                    this.remoteConnected=false;
                    if(this.listener!=null)
                        this.listener.onDisconnected();
                }
                this.removeListener();
            }
        }
        catch(Exception e)
        {
            if(this.listener!=null)
                this.listener.onException(e);
        }
    }
    public byte[] receive()
    {
        try{
            if(this.mySocket.isConnected())//&& !this.mySocket.isClosed())
            {
                InputStream in = (this.mySocket.getInputStream());
                int firstByte=in.read();
                if(firstByte==-1)
                    return null;
                else
                {
                    int size=in.available();
                    byte[] data=new byte[1+size];
                    data[0]=(byte)firstByte;
                    in.read(data, 1, size);
                    return data;
                }
            }
        }
        catch(Exception e)
        {
            if(this.listener!=null)
                this.listener.onException(e);
        }
        return null;
    }
    public boolean send(byte[] data)
    {
        try{
            if(this.mySocket!=null)
            {
                if(this.mySocket.isConnected())//&&!this.mySocket.isClosed())
                {
                    this.mySocket.getOutputStream().write(data);
                    return true;
                }
            }
        }
        catch(Exception e)
        {
            if(this.listener!=null)
                this.listener.onException(e);
        }
        return false;
    }
}
