package com.Group17;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
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
    ConnectedThread mBluetoothConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String songName = intent.getStringExtra("noteName");
        mBluetoothConnection = intent.getParcelableExtra("bluetoothConnection"); //gonna have to remove all this now that we use Service

        if(mBluetoothConnection!=null) {
            Toast.makeText(getApplicationContext(), "Bluetooth persists", Toast.LENGTH_LONG).show();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        JSONArray song = null;
        try {
            String jtxt = loadJSONFromAsset(this);
            JSONObject json = new JSONObject(jtxt);
            song = json.getJSONArray(songName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(song != null)
        {
            try {
                trainView = new TrainingView(this, point.x, point.y, song);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setContentView(trainView);
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

}
