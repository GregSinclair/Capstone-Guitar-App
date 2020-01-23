package com.Group17;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {
    Set<BluetoothDevice> set_pairedDevices;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDevice;
    com.Group17.BlueConnect mBlueConnect;
    com.Group17.ConnectedThread mConnectedThread;
    private static final String TAG = "BluetoothService";
    private final IBinder mBinder = new BluetoothServiceBinder();

    class BluetoothServiceBinder extends Binder{
        public BluetoothService getService(){return BluetoothService.this;}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Started");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBlueConnect = new BlueConnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service Bound");
        return mBinder; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "OnStart Command");

        return START_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "Service Stoped");
        if (mBlueConnect != null) {
            mBlueConnect.disconnect();
            mBlueConnect = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mBluetoothAdapter.cancelDiscovery();
        return super.stopService(name);
    }


    @Override
    public void onDestroy() {
        stop();
        Log.d(TAG, "Destroyed");
        super.onDestroy();
    }

    public synchronized void stop() {
        if (mBlueConnect != null) {
            mBlueConnect.disconnect();
            mBlueConnect = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        stopSelf();
    }


    public void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public boolean connect(BluetoothDevice device){
        Log.d(TAG, "Connect");
        mDevice = device;
        mBlueConnect = new BlueConnect();
        mBlueConnect.setDevice(mDevice);
        mBlueConnect.connect();
        mConnectedThread = new ConnectedThread(mBlueConnect.getSocket());
        mConnectedThread.start();
        /*
        if (mBluetoothAdapter != null) {
            set_pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (set_pairedDevices.size() > 0) {

                for (BluetoothDevice device : set_pairedDevices) {
                    if (deviceName == device.getName() && deviceAddress == device.getAddress()) {
                        mDevice = device;
                        if (deviceAddress != null && deviceAddress.length() > 0) {
                            Log.d(TAG, "Connect Exists");
                            mBlueConnect = new BlueConnect();
                            mBlueConnect.connect();
                            mConnectedThread = new ConnectedThread(mBlueConnect.getSocket());
                            mConnectedThread.start();
                            return true;
                        } else {
                            Log.d(TAG, "No Address");
                            stopSelf();
                        }
                    }
                }
            }else{Log.d(TAG, "No Devices");}
        } else{Log.d(TAG, "No Adapter");}

         */
        return false;
    }

    public void disconnect(){}

    public void sendMessage(String msg){
        if(mBlueConnect != null && mBlueConnect.isConnected() && mConnectedThread != null){
            mConnectedThread.write(msg);
        }
    }

    public String getMessage(){
        String msg =  "{'type':'none'}*";
        if(mBlueConnect != null && mBlueConnect.isConnected() && mConnectedThread != null){
            msg = mConnectedThread.getLastMessage();
        }
        return msg;
    }

    public Boolean isRunning(){
        Log.d(TAG, "Is Running");
        if(mConnectedThread != null && mConnectedThread.isAlive()){return true;}
        return false;
    }

}
