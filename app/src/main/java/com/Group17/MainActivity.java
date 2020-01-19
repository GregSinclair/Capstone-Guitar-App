package com.Group17;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.*;



public class MainActivity extends AppCompatActivity {

    private boolean isMute;

    //BluetoothConnectionService mBluetoothConnection;
    ConnectedThread mBluetoothConnection = null;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.title_screen);





        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("songName", "Fun");

                startActivity(intent);
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SongList.class);
                //intent.putExtra("songName", "Fun2");

                startActivityForResult(intent,1);

                //mBluetoothConnection = bluetoothIntent.getParcelableExtra("bluetoothConnection");


            }
        });

        findViewById(R.id.b_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int myVersion = Build.VERSION.SDK_INT;
                Log.d(TAG, "Running API version " + myVersion);

                Intent intent = new Intent(MainActivity.this, BluetoothConnection.class);

                startActivity(intent);
            }
        });

        findViewById(R.id.button_training).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mBluetoothConnection==null) {
                    Log.d(TAG, "no bluetooth connection");

                    Toast.makeText(getApplicationContext(), "No bluetooth connection", Toast.LENGTH_LONG).show();
                    //return;
                }

                Intent intent = new Intent(MainActivity.this, TrainingActivity.class);
                intent.putExtra("noteName", "A");
                startActivity(intent);
            }
        });

/*

        TextView highScoreTxt = findViewById(R.id.highScoreTxt);

        final SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);
        highScoreTxt.setText("HighScore: " + prefs.getInt("highscore", 0));

        isMute = prefs.getBoolean("isMute", false);

        final ImageView volumeCtrl = findViewById(R.id.volumeCtrl);

        if (isMute)
            volumeCtrl.setImageResource(R.drawable.ic_volume_off_black_24dp);
        else
            volumeCtrl.setImageResource(R.drawable.ic_volume_up_black_24dp);

        volumeCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isMute = !isMute;
                if (isMute)
                    volumeCtrl.setImageResource(R.drawable.ic_volume_off_black_24dp);
                else
                    volumeCtrl.setImageResource(R.drawable.ic_volume_up_black_24dp);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isMute", isMute);
                editor.apply();

            }
        });


 */
    }//end of on create

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //this should never be reached as of jan18
        Log.d(TAG, "in activity result"); //this doesn't seem to ever trigger
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if(data != null) {
                    Log.d(TAG, "intent received");
                    mBluetoothConnection = data.getParcelableExtra("bluetoothConnection");
                }
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "no intent received");
            }
        }
    }


}
