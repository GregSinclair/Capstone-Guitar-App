package com.Group17;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.*;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class SongTypeMenu extends AppCompatActivity {

    ListView songList;
    Intent resultIntent;
    private static final String TAG = "SongTypeMenu";
    private String songName;
    private JSONArray partNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_screen);
        Intent intent = getIntent();
        songName = intent.getStringExtra("songName");

        try {
            String jtxt = loadJSONFromAsset(this);
            JSONObject json = new JSONObject(jtxt);
            partNames = (json.getJSONObject(songName)).getJSONArray("partNames");

            List<String> songNames = new ArrayList<String>();
            songNames.add("Full Song");
            for(int i=0;i<partNames.length();i++){
                songNames.add(partNames.get(i).toString());
            }
            //maybe add a "memorized" option here, where the numbers aren't shown? There's probably a better place for it tho
            songList=(ListView)findViewById(R.id.list_view_song);
            ArrayAdapter<String> songListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, songNames);
            songList.setAdapter(songListAdapter);
            songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                Toast.makeText(getApplicationContext(), tv.getText(), Toast.LENGTH_LONG).show();

                int partKey=-1;

                for(int i=0;i<partNames.length();i++){
                    try {
                        if (tv.getText().equals(partNames.getString(i))){
                            partKey=i;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                resultIntent = new Intent( SongTypeMenu.this, GameActivity.class);
                resultIntent.putExtra("songName", songName);
                    resultIntent.putExtra("partKey", partKey);
                startActivity(resultIntent);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (resultIntent==null){
            resultIntent = new Intent();
            resultIntent.putExtra("song", "Fun");
        }
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {

            InputStream is=context.getAssets().open("songs.json");

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
