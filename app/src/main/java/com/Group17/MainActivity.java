package com.Group17;

import androidx.appcompat.app.AppCompatActivity;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private BluetoothService myService;
    private boolean isServiceBound;
    private ServiceConnection serviceConnection;
    private  Intent serviceIntent;

    //BluetoothConnectionService mBluetoothConnection;
    ConnectedThread mBluetoothConnection = null;

    private String theSong = "Shorty";

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.start_screen);


        findViewById(R.id.b_songs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SongList.class);
                intent.putExtra("songType", 0);
                startActivity(intent);
            }
        });

        findViewById(R.id.b_chords).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SongList.class);
                intent.putExtra("songType", 1);
                startActivity(intent);
            }
        });

        findViewById(R.id.b_scales).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SongList.class);
                intent.putExtra("songType", 2);
                startActivity(intent);
            }
        });


        findViewById(R.id.b_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //arbitrary testing button

            }
        });

        findViewById(R.id.b_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SettingsMenu.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.b_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int myVersion = Build.VERSION.SDK_INT;
                Log.d(TAG, "Running API version " + myVersion);

                Intent intent = new Intent(MainActivity.this, BluetoothConnection.class);

                startActivityForResult(intent,1); //tries to get a BTA intent
            }
        });

        serviceIntent=new Intent(getApplicationContext(),BluetoothService.class);

        startService(serviceIntent);

    }

    @Override //not used anymore. in fact, no reason to check for bt at the start, game does it
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "in activity result"); //this doesn't seem to ever trigger
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) { //song choice
            if (resultCode == RESULT_OK) {
                if(data != null) {
                    Log.d(TAG, "intent received");
                    theSong = data.getParcelableExtra("song");
                }
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "no intent received");
            }
        }
    }


    private void bindService(){ //doubt these are needed
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
