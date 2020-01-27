package com.Group17;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class TrainingActivity extends AppCompatActivity {

    private TrainingView trainView;

    private static final String TAG = "TrainingActivity";

    private BluetoothService myService;
    private boolean isServiceBound;
    private ServiceConnection serviceConnection;
    private  Intent serviceIntent;

    private JSONArray song = null;
    private Point point;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String songName = intent.getStringExtra("noteName");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        serviceIntent=new Intent(getApplicationContext(),BluetoothService.class);
        bindService();
        if(isServiceBound && myService  != null && myService.isRunning())
        {
            Log.d(TAG, "MyService Not NULL");
        }
        else
        {
            Log.d("ACTIVITY SetBluetooth", "NULL");
        }


        point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);

        try {
            String jtxt = loadJSONFromAsset(this);
            JSONObject json = new JSONObject(jtxt);
            song = json.getJSONArray(songName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        serviceIntent=new Intent(getApplicationContext(),BluetoothService.class);
        int i=0;
        while(i<10 && !isServiceBound) {
            i++;
            bindService();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(i==10 && !isServiceBound){
            unbindService();
            finish();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        trainView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        trainView.resume();
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("notes.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    private void bindService(){
        if(serviceConnection==null){
            serviceConnection=new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    BluetoothService.BluetoothServiceBinder myServiceBinder=(BluetoothService.BluetoothServiceBinder)iBinder;
                    myService=myServiceBinder.getService();
                    isServiceBound=true;

                    if(song != null)
                    {
                        try {
                            trainView = new TrainingView(TrainingActivity.this, point.x, point.y, song, myService);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setContentView(trainView);
                    }


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
