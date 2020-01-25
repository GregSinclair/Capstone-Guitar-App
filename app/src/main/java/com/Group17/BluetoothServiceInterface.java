package com.Group17;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;

public class BluetoothServiceInterface extends AppCompatActivity implements Runnable{

    String bluetooth_message="00";
    private static final String TAG = "BluetoothServiceInterface";

    private BluetoothService myService;
    private boolean isServiceBound;
    private ServiceConnection serviceConnection;
    private Intent serviceIntent;

    public BluetoothServiceInterface(Intent serviceIntent){
        this.serviceIntent = serviceIntent;
    }

    public void run(){
        bindService();
    }

    private void bindService(){
        if(serviceConnection==null){
            serviceConnection=new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    BluetoothService.BluetoothServiceBinder myServiceBinder=(BluetoothService.BluetoothServiceBinder)iBinder;
                    myService=myServiceBinder.getService();
                    isServiceBound=true;
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    isServiceBound=false;
                }
            };
        }

        bindService(serviceIntent,serviceConnection, Context.BIND_AUTO_CREATE);

    }

    private void unbindService(){
        if(isServiceBound){
            unbindService(serviceConnection);
            isServiceBound=false;
        }
    }




}
