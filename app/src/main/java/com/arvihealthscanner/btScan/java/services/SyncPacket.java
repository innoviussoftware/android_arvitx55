package com.arvihealthscanner.btScan.java.services;


/**
 * Created by admin on 11/06/2018.
 */

public class SyncPacket {

    private static enum RX_STATE{STATE_START,STATE_CMD,STATE_SIZE,STATE_DATA}
    public final static short START_BYTE=0x24;
    private final static int HEADER_LENGTH=4;
    public RX_STATE rxState= RX_STATE.STATE_START;
    public short startByte;
    public int cmd;
    public int size;
    public byte[] data;
    public byte[] buff;
    public int dataCount;
    private int charTimeout;

    private SyncPacket()
    {

    }

    public static SyncPacket getRxPacket()
    {
        return new SyncPacket();
    }

    public static SyncPacket getTxPacket(short startByte, int cmd,byte[] data)
    {
        if(data==null)
        {
            data=new byte[0];
        }
        SyncPacket pkt=new SyncPacket();
        pkt.startByte=startByte;
        pkt.cmd=cmd;
        pkt.size=data.length;
        pkt.data=data;
        pkt.toByteArray();
        return pkt;
    }

    private byte[] toByteArray()
    {
        this.buff=new byte[this.size+HEADER_LENGTH];
        Helper.setUint8(this.buff, 0, this.startByte);
        Helper.setUint8(this.buff, 1, (short)this.cmd);
        Helper.setUint16_LE(this.buff, 2, this.size);
        System.arraycopy(this.data, 0, this.buff, HEADER_LENGTH, this.size);
        return this.buff;
    }

    public byte[] getData()
    {
        return this.data;
    }

    public int getSize()
    {
        return this.size+HEADER_LENGTH;
    }

    public boolean isValid()
    {
        this.toByteArray();
        return true;
    }

    public boolean rxHandler(byte [] buff)
    {
        for(int i=0;i<buff.length;i++)
        {
            this.charTimeout = 2;

            switch(this.rxState)
            {
                case STATE_START:
                    if(buff[i]==SyncPacket.START_BYTE )
                    {
                        this.startByte= Helper.getUint8(buff[i]);
                        this.data=new byte[2];
                        this.dataCount=0;
                        this.rxState= RX_STATE.STATE_CMD;
                    }
                    break;
                case STATE_CMD:
                    this.data[this.dataCount]=buff[i];
                    this.cmd= Helper.getUint8(this.data, 0);
                    this.data=new byte[2];
                    this.dataCount=0;
                    this.rxState= RX_STATE.STATE_SIZE;
                    break;
                case STATE_SIZE:
                    this.data[this.dataCount]=buff[i];
                    this.dataCount++;
                    if(this.dataCount>=2)
                    {
                        this.size= Helper.getUint16_LE(this.data, 0);
                        this.data=new byte[this.size];
                        this.dataCount=0;
                        this.rxState= RX_STATE.STATE_DATA;
                    }
                    break;
                case STATE_DATA:
                    this.data[this.dataCount]=buff[i];
                    this.dataCount++;
                    if(this.dataCount>=this.size)
                    {
                        this.dataCount=0;
                        this.rxState= RX_STATE.STATE_START;
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    public void checkCharTimeout()
    {
        if(this.charTimeout>0)
        {
            this.charTimeout--;
            if(this.charTimeout==0)
            {
                this.rxState= RX_STATE.STATE_START;
            }
        }
    }
}
