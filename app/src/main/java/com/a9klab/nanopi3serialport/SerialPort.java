package com.a9klab.nanopi3serialport;

import android.util.Log;


public class SerialPort {
    private static DataListener dataListener;
    //private Handler mHandler;

    public SerialPort(final DataListener dataListener) {
        this.dataListener = dataListener;

        InitComm();
    }

    public static void receiveData(byte[] data) {

        if (dataListener != null) {
            dataListener.getData(data);
        } else {
            Log.e("Class SerialPort","dataListener null");
        }

    }
    public native int InitComm();
    public static native int Send(byte[] data);
    public static native int SendEx(byte[] data);
    public static native int Start();
    public static native int Stop();
    public native short GetCRC16(byte[] data,int len);

}
