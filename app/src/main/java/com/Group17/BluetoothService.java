package com.Group17;

import android.app.IntentService;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothService extends IntentService {


    ConnectedThread mConnectedThread;

    @Override
    protected void onHandleIntent(Intent workIntent){

        String dataString = workIntent.getDataString();

        //do work here
        //take some arguments and use them to spawn a ConnectedThread

    }



}
