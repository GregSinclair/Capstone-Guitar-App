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
        String songName = intent.getStringExtra("songName");

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
        bindService();


    }

    @Override
    protected void onPause() {
        super.onPause();
        trainView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(trainView != null) {
            trainView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JSONObject json = new JSONObject();
        try {
            json.put("type", 5);
            json.put("sequence", 0);
            json.put("timeStamp", 420);
            json.put("values", null);
            json.put("duration", -1); //duration of -1 indicates training mode
            myService.sendMessage(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
   }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("chords.json");

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
                    play();
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

    public void play()
    {
        if(myService  != null && myService.isRunning() && song != null)
        {
            Log.d("ACTIVITY SetBluetooth", "Not NULL");
            if(song != null)
            {
                try {
                    trainView = new TrainingView(TrainingActivity.this, point.x, point.y, song, myService);
                    setContentView(trainView);
                    trainView.beginGame();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            Log.d("ACTIVITY SetBluetooth", "NULL");
        }
    }

}
