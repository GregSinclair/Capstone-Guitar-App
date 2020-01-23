package com.Group17;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BlueConnect{
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private String TAG = "BlueConnect";
    private Boolean isConnected;

    public BlueConnect() {
        isConnected = false;
        Log.d(TAG, "OnCreate");

    }

    private Boolean createSocket()
    {
        Log.d(TAG, "BlueConnect: Connecting");
        BluetoothSocket tmp = null;
        Method m = null;
        try {m = mmDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});}
        catch (NoSuchMethodException ex){Log.d(TAG, "Connect Thread: NoMethod");}
        try {
            int bt_port_to_connect = 5;
            tmp = (BluetoothSocket) m.invoke(mmDevice, bt_port_to_connect);
        }
        catch (IllegalAccessException e1) {Log.d(TAG, "Connect Thread: IllegalAccessException");}
        catch (InvocationTargetException e2) {Log.d(TAG, "Connect Thread: TargetException");}
        mmSocket = tmp;
        if(mmSocket != null){return true;}
        else{return false;}
    }

    public void setDevice(BluetoothDevice device){mmDevice = device;}

    public void connect() {
        Log.d(TAG, "Connect Thread: Run");
        if(createSocket())
        {
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                if(mmSocket != null){
                    Log.d(TAG, "Connect Thread: Connected Start");
                    isConnected = true;
                }
                else{
                    Log.d(TAG, "Connect Thread: Connected Socket Null");
                    isConnected = false;
                }
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                isConnected = false;
                try { mmSocket.close(); }
                catch (IOException closeException) {Log.d(TAG, "Connect Thread: Cannot Close");}
            }
        }
        else{isConnected = false;}
    }

    public void disconnect()
    {
        if(mmSocket != null){
            try { mmSocket.close(); }
            catch (IOException closeException) { Log.d(TAG, "Connect Thread: Cannot Close"); }
        }
        isConnected = false;
        mmSocket = null;
    }

    public Boolean isConnected(){return isConnected;}
    public BluetoothSocket getSocket(){return mmSocket;}
}
