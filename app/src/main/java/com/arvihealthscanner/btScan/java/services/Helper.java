package com.arvihealthscanner.btScan.java.services;



import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 *
 * @author nbpatil
 */
public class Helper
{
    private static boolean useOldCRC=false;
    private static boolean debugMode=false;
    private static boolean exceptionStackTrace=true;
    public static void setDebugMode(boolean mode)
    {
        debugMode=mode;
        exceptionStackTrace=true;
    }
    public static void setDebugMode(boolean mode,boolean exception)
    {
        debugMode=mode;
        exceptionStackTrace=exception;
    }
    public static void println(String msg)
    {
        if(debugMode)
            System.out.println(msg);
    }

    public static void println()
    {
        if(debugMode)
            System.out.println();
    }
    public static void print(String msg)
    {
        if(debugMode)
            System.out.print(msg);
    }
    public static void printStackTrace(Exception e)
    {
        if(debugMode)
        {
            if(exceptionStackTrace)
                e.printStackTrace();
            else
                System.out.println(e.getMessage());
        }
    }
    public static BigInteger getBigInteger(byte[] buff,int start,int len)
    {
        byte[] no;
        if(buff.length>=(start+len))
        {
            no=new byte[len];
        }
        else
        {
            no=new byte[buff.length-start];
        }
        System.arraycopy(buff,start, no, 0, no.length);
        return new BigInteger(1,no);
    }

    public static int calcCheckSumINT(byte[] buff,int offset,int size)
    {
        int checksum=0;
        for(int i = offset;i<(size+offset);i++)
        {
            checksum = checksum+(buff[i]&0xff);
        }
        return (checksum);
    }
    public static byte calcCheckSum(byte[] buff,int offset,int size)
    {
        int checksum=0;
        for(int i = offset;i<(size+offset);i++)
        {
            checksum += buff[i];
        }
        return (byte)(~(checksum&0xff));
    }
    public static byte hexToBin(String hexString)
    {
        char[] hex=new char[2];
        hex[0]=hexString.charAt(0);
        hex[1]=hexString.charAt(1);

        // Convert 0-9, A-F to 0x0-0xF
        if(hex[1] > '9')
            hex[1] -= 'A' - 10;
        else
            hex[1] -= '0';

        if(hex[0] > '9')
            hex[0] -= 'A' - 10;
        else
            hex[0] -= '0';

        // Concatenate
        return ((byte)(((hex[0]<<4) |  hex[1])&0xff));
    }
    private static final char[] auchCRCLower={
            0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2,
            0xC6, 0x06, 0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04,
            0xCC, 0x0C, 0x0D, 0xCD, 0x0F, 0xCF, 0xCE, 0x0E,
            0x0A, 0xCA, 0xCB, 0x0B, 0xC9, 0x09, 0x08, 0xC8,
            0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA, 0x1A,
            0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC,
            0x14, 0xD4, 0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6,
            0xD2, 0x12, 0x13, 0xD3, 0x11, 0xD1, 0xD0, 0x10,

            0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3, 0xF2, 0x32,
            0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4,
            0x3C, 0xFc, 0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE,		  // 0xFC is New   0xF4 is OLD
            0xFA, 0x3A, 0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38,
            0x28, 0xE8, 0xE9, 0x29, 0xEB, 0x2B, 0x2A, 0xEA,
            0xEE, 0x2E, 0x2F, 0xEF, 0x2D, 0xED, 0xEC, 0x2C,
            0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7, 0xE6, 0x26,
            0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0,

            0xA0, 0x60, 0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62,
            0x66, 0xA6, 0xA7, 0x67, 0xA5, 0x65, 0x64, 0xA4,
            0x6C, 0xAC, 0xAD, 0x6D, 0xAF, 0x6F, 0x6E, 0xAE,
            0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68,
            0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA,
            0xBE, 0x7E, 0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C,
            0xB4, 0x74, 0x75, 0xB5, 0x77, 0xB7, 0xB6, 0x76,
            0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71, 0x70, 0xB0,

            0x50, 0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92,
            0x96, 0x56, 0x57, 0x97, 0x55, 0x95, 0x94, 0x54,
            0x9C, 0x5C, 0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E,
            0x5A, 0x9A, 0x9B, 0x5B, 0x99, 0x59, 0x58, 0x98,
            0x88, 0x48, 0x49, 0x89, 0x4B, 0x8B, 0x8A, 0x4A,
            0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D, 0x4C, 0x8C,
            0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86,
            0x82, 0x42, 0x43, 0x83, 0x41, 0x81, 0x80, 0x40
    };
    private static final char[] auchCRCHigher ={
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x0, 0xC1, 0x81,  0x40
    };
    public static byte[] getCRC(byte[] sendBuffer, int offset, int dataLen)
    {
        int index;
        char crcHigher = 0xFF;
        char crcLower = 0xFF;
        for (int i = 0; i < dataLen; i++) {

            index = (0x00ff) & (crcHigher ^ sendBuffer[offset + i]);
            crcHigher = (char) (crcLower ^ auchCRCHigher[index]);
            if((auchCRCLower[index]==0xFC)&&(useOldCRC))
                crcLower = (char) (0xF4);
            else
                crcLower = (char) (auchCRCLower[index]);
        }
        byte[] crc = new byte[2];
        crc[0] = (byte) (crcHigher);
        crc[1] = (byte) (crcLower);
        return crc;
    }
    public static boolean setCRC(byte[] sendBuffer, int offset, int dataLen)
    {
        if(sendBuffer==null || sendBuffer.length<(dataLen+2))
            return false;

        int index;
        char crcHigher = 0xFF;
        char crcLower = 0xFF;
        for (int i = 0; i < dataLen; i++) {

            index = (0x00ff) & (crcHigher ^ sendBuffer[offset + i]);
            crcHigher = (char) (crcLower ^ auchCRCHigher[index]);
            if((auchCRCLower[index]==0xFC)&&(useOldCRC))
                crcLower = (char) (0xF4);
            else
                crcLower = (char) (auchCRCLower[index]);
        }
        sendBuffer[dataLen] = (byte) (crcHigher);
        sendBuffer[dataLen+1] = (byte) (crcLower);
        return true;
    }
    public static boolean checkCRC(byte[] sendBuffer, int offset, int dataLen)
    {
        byte[] crc = getCRC(sendBuffer,offset,dataLen-2);
        if((sendBuffer[dataLen-2]==crc[0])&&(sendBuffer[dataLen-1]==crc[1]))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public static void setOldCRC(boolean val)
    {
        useOldCRC=val;
    }
    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        if(len%2 !=0) //odd length
        {
            s="0"+s;
            len++;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte)(Integer.parseInt(s.substring(i, i+2),16)&0xff);
        }
        return data;
    }
    public static String byteArrayToHexString(byte[] in)
    {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in)
        {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static short getUint8(byte data)
    {
        return (short)(data&0xFF);
    }
    public static short getUint8(byte[] data,int start)
    {
        short val=-1;
        if(data!=null && data.length >= (start+1))
        {
            val=(short)(data[start]&0xFF);
        }
        return val;
    }
    public static int getUint16_BE(byte[] data,int start)
    {
        int val=-1;
        if(data!=null && data.length >= (start+2))
        {
            val=(data[start]&0xFF)<<8;
            val|=(data[start+1]&0xFF);
            val&=0xFFFF;
        }
        return val;
    }
    public static long getUint32_BE(byte[] data,int start)
    {
        long val=-1;
        if(data!=null && data.length >= (start+4))
        {
            byte[] buff=new byte[4];
            for(int i=0;i<4;i++)
                buff[i]=data[start+i];
            val=(new BigInteger(1,buff)).longValue();
        }
        return val;
    }
    public static BigInteger getUint64_BE(byte[] data,int start)
    {
        BigInteger val=null;
        if(data!=null && data.length >= (start+8))
        {
            byte[] buff=new byte[8];
            for(int i=0;i<8;i++)
                buff[i]=data[start+i];
            val=new BigInteger(1, buff);
        }
        return val;
    }
    public static Float getFloat_BE(byte[] data,int start)
    {
        Float val=null;
        if(data!=null && data.length >= (start+4))
        {
            byte[] buff=new byte[4];
            for(int i=0;i<4;i++)
                buff[i]=data[start+i];
            val=ByteBuffer.wrap(buff).getFloat();
        }
        return val;
    }

    public static Double getDouble_BE(byte[] data,int start)
    {
        Double val=null;
        if(data!=null && data.length >= (start+8))
        {
            byte[] buff=new byte[8];
            for(int i=0;i<8;i++)
                buff[i]=data[start+i];
            val=ByteBuffer.wrap(buff).getDouble();
        }
        return val;
    }
    public static int getUint16_LE(byte[] data,int start)
    {
        int val=-1;
        if(data!=null && data.length >= (start+2))
        {
            val=(data[start+1]&0xFF)<<8;
            val|=(data[start]&0xFF);
            val&=0xFFFF;
        }
        return val;
    }
    public static long getUint32_LE(byte[] data,int start)
    {
        long val=-1;
        if(data!=null && data.length >= (start+4))
        {
            byte[] buff=new byte[4];
            for(int i=0;i<4;i++)
                buff[3-i]=data[start+i];
            val=(new BigInteger(1,buff)).longValue();
        }
        return val;
    }
    public static BigInteger getUint64_LE(byte[] data,int start)
    {
        BigInteger val=null;
        if(data!=null && data.length >= (start+8))
        {
            byte[] buff=new byte[8];
            for(int i=0;i<8;i++)
                buff[7-i]=data[start+i];
            val=new BigInteger(1, buff);
        }
        return val;
    }
    public static Float getFloat_LE(byte[] data,int start)
    {
        Float val=null;
        if(data!=null && data.length >= (start+4))
        {
            byte[] buff=new byte[4];
            for(int i=0;i<4;i++)
                buff[3-i]=data[start+i];
            val=ByteBuffer.wrap(buff).getFloat();
        }
        return val;
    }

    public static Double getDouble_LE(byte[] data,int start)
    {
        Double val=null;
        if(data!=null && data.length >= (start+8))
        {
            byte[] buff=new byte[8];
            for(int i=0;i<8;i++)
                buff[7-i]=data[start+i];
            val=ByteBuffer.wrap(buff).getDouble();
        }
        return val;
    }
    public static String getString(byte[] data,int start,int len)
    {
        String val=null;
        if(data!=null && data.length >= (start+len))
        {
            byte[] buff=new byte[len];
            System.arraycopy(data, start, buff, 0, buff.length);
            val=new String(buff);
        }
        return val;
    }
    public static boolean setUint8(byte[] data,int start,short val)
    {
        data[start]=(byte)val;
        return true;
    }
    public static boolean setUint16_BE(byte[] data,int start,int val)
    {
        if(data!=null && data.length >= (start+2))
        {
            data[start]=(byte)((val>>8)&0xFF);
            data[start+1]=(byte)((val)&0xFF);
            return true;
        }
        return false;
    }
    public static boolean setUint32_BE(byte[] data,int start,long val)
    {
        if(data!=null && data.length >= (start+4))
        {
            data[start]=(byte)((val>>24)&0xFF);
            data[start+1]=(byte)((val>>16)&0xFF);
            data[start+2]=(byte)((val>>8)&0xFF);
            data[start+3]=(byte)((val)&0xFF);
            return true;
        }
        return false;
    }
    public static boolean setUint64_BE(byte[] data,int start,BigInteger val)
    {
        if(data!=null && data.length >= (start+8))
        {
            byte[] buff=val.toByteArray();
            for(int i=7,j=(buff.length-1);i>=0;i--)
            {
                if(j<0)
                    data[start+i]=0;
                else
                {
                    data[start+i]=buff[j];
                    j--;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean setFloat_BE(byte[] data,int start,Float val)
    {
        if(data!=null && data.length >= (start+4))
        {
            byte[] bytes = new byte[4];
            ByteBuffer.wrap(bytes).putFloat(val);
            for(int i=0;i<4;i++)
            {
                data[start+i]=bytes[i];
            }
            return true;
        }
        return false;
    }
    public static boolean setDouble_BE(byte[] data,int start,Double val)
    {
        if(data!=null && data.length >= (start+8))
        {
            byte[] bytes = new byte[8];
            ByteBuffer.wrap(bytes).putDouble(val);
            for(int i=0;i<8;i++)
            {
                data[start+i]=bytes[i];
            }
            return true;
        }
        return false;
    }
    public static boolean setUint16_LE(byte[] data,int start,int val)
    {
        if(data!=null && data.length >= (start+2))
        {
            data[start+1]=(byte)((val>>8)&0xFF);
            data[start]=(byte)((val)&0xFF);
            return true;
        }
        return false;
    }
    public static boolean setUint32_LE(byte[] data,int start,long val)
    {
        if(data!=null && data.length >= (start+4))
        {
            data[start+3]=(byte)((val>>24)&0xFF);
            data[start+2]=(byte)((val>>16)&0xFF);
            data[start+1]=(byte)((val>>8)&0xFF);
            data[start]=(byte)((val)&0xFF);
            return true;
        }
        return false;
    }
    public static boolean setUint64_LE(byte[] data,int start,BigInteger val)
    {
        if(data!=null && data.length >= (start+8))
        {
            byte[] buff=val.toByteArray();
            for(int i=0,j=(buff.length-1);i<8;i++)
            {
                if(j<0)
                    data[start+i]=0;
                else
                {
                    data[start+i]=buff[j];
                    j--;
                }
            }
            return true;
        }
        return false;
    }public static boolean setFloat_LE(byte[] data,int start,Float val)
{
    if(data!=null && data.length >= (start+4))
    {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putFloat(val);
        for(int i=0;i<4;i++)
        {
            data[start+i]=bytes[3-i];
        }
        return true;
    }
    return false;
}
    public static boolean setDouble_LE(byte[] data,int start,Double val)
    {
        if(data!=null && data.length >= (start+8))
        {
            byte[] bytes = new byte[8];
            ByteBuffer.wrap(bytes).putDouble(val);
            for(int i=0;i<8;i++)
            {
                data[start+i]=bytes[7-i];
            }
            return true;
        }
        return false;
    }
    public static boolean setString(byte[] data,int start,String val)
    {
        if(data!=null && val!=null && data.length >= (start+val.length()))
        {
            byte[] buff=val.getBytes();
            System.arraycopy(buff, 0, data, start, buff.length);
            return true;
        }
        return false;
    }
    public static int swaps(int val)
    {
        byte[] buff=new byte[2];
        buff[0]=(byte)((val>>8)&0xFF);
        buff[1]=(byte)((val)&0xFF);

        val=(buff[1]&0xFF)<<8;
        val|=(buff[0]&0xFF);
        val&=0xFFFF;
        return val;
    }
    public static long swapl(long val)
    {
        byte[] buff=new byte[4];
        buff[3]=(byte)((val>>24)&0xFF);
        buff[2]=(byte)((val>>16)&0xFF);
        buff[1]=(byte)((val>>8)&0xFF);
        buff[0]=(byte)((val)&0xFF);

        val=(new BigInteger(1,buff)).longValue();
        return val;
    }
    public static BigInteger swapll(BigInteger val)
    {
        if(val!=null)
        {
            byte[] data=new byte[8];
            byte[] buff=val.toByteArray();
            for(int i=7,j=(buff.length-1);i>=0;i--)
            {
                if(j<0)
                    data[i]=0;
                else
                {
                    data[i]=buff[j];
                    j--;
                }
            }
            for(int i=0;i<4;i++)
            {
                byte tmp=data[i];
                data[i]=data[7-i];
                data[7-i]=tmp;
            }
            val=new BigInteger(1,data);
        }
        return val;
    }

    public static byte[] decimalToBCD(long num) {
        int digits = 0;

        long temp = num;
        while (temp != 0) {
            digits++;
            temp /= 10;
        }

        int byteLen = digits % 2 == 0 ? digits / 2 : (digits + 1) / 2;

        byte bcd[] = new byte[byteLen];

        for (int i = 0; i < digits; i++) {
            byte tmp = (byte) (num % 10);

            if (i % 2 == 0) {
                bcd[i / 2] = tmp;
            } else {
                bcd[i / 2] |= (byte) (tmp << 4);
            }

            num /= 10;
        }

        for (int i = 0; i < byteLen / 2; i++) {
            byte tmp = bcd[i];
            bcd[i] = bcd[byteLen - i - 1];
            bcd[byteLen - i - 1] = tmp;
        }

        return bcd;
    }
    public static long BCDtoDecimal(byte[] bcd) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < bcd.length; i++) {

            byte high = (byte) (bcd[i] & 0xf0);
            high >>>= (byte) 4;
            high = (byte) (high & 0x0f);
            byte low = (byte) (bcd[i] & 0x0f);

            sb.append(high);
            sb.append(low);
        }

        return Long.valueOf(sb.toString());
    }

}
