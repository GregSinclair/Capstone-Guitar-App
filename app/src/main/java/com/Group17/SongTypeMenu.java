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
    private int songType;
    JSONObject jSongProgression;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_screen);
        Intent intent = getIntent();
        songName = intent.getStringExtra("songName");
        songType = intent.getIntExtra("songType",0);
        try {
            String jtxt = loadJSONFromAsset(this);
            JSONObject json = new JSONObject(jtxt);
            partNames = (json.getJSONObject(songName)).getJSONArray("partNames");

            jSongProgression=MemoryInterface.readFile("progression.txt").getJSONObject(songName);

            //format of the progression file: json object for each song, containing numbered booleans. 0 is the full song. first part is always unlocked, so it doesn't need a boolean
            //have to make sure it initializes somewhere
            List<String> songNames = new ArrayList<String>();

            boolean locksDisabled = false;
            JSONObject jSettings = MemoryInterface.readFile("userSettings.txt");
            locksDisabled = "on".equals(jSettings.getString("Unlock All Songs"));


            if(jSongProgression.getBoolean(""+0) || locksDisabled){
                songNames.add("Full Song");
            }
            else{
                songNames.add("locked");
            }
            for(int i=0;i<partNames.length();i++){
                if(jSongProgression.getBoolean(""+i) || i==0 || locksDisabled){
                    songNames.add(partNames.get(i).toString());
                }
                else{
                    songNames.add("locked");
                }
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

                if(tv.getText().toString().equals("locked")){
                    return;
                }



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



                if (partKey == -1){
                    //check the setting here and go to TA if its enabled
                    resultIntent = new Intent( SongTypeMenu.this, GameActivity.class);

                    JSONObject jSettings = MemoryInterface.readFile("userSettings.txt");
                    if(jSettings.has("Always Training Mode")){
                        try {
                            String theSetting=jSettings.getString("Always Training Mode");
                            if (theSetting.equals("on")){
                                resultIntent = new Intent( SongTypeMenu.this, TrainingActivity.class);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    resultIntent.putExtra("repeatingSong", false);

                }
                else{
                    try {
                        if (jSongProgression.has(""+(partKey+1))) {
                            if (jSongProgression.getBoolean("" + (partKey + 1)) == false) {
                                jSongProgression.put("" + (partKey + 1), true); //this should open up the next one as soon as it's chosen
                                JSONObject newFullProgression = new JSONObject();
                                newFullProgression.put(songName, jSongProgression);
                                MemoryInterface.writeFile(newFullProgression, "progression.txt");
                            }
                        }
                        else if(partKey+1 == partNames.length() && jSongProgression.getBoolean("" + 0) == false){
                            jSongProgression.put("" + 0, true);
                            JSONObject newFullProgression = new JSONObject();
                            newFullProgression.put(songName, jSongProgression);
                            MemoryInterface.writeFile(newFullProgression, "progression.txt");
                        }

                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                    resultIntent = new Intent( SongTypeMenu.this, TrainingActivity.class);
                    resultIntent.putExtra("repeatingSong", true);

                }
                resultIntent.putExtra("songName", songName);
                if(songType==0){
                    resultIntent.putExtra("notesID", "song");
                }
                else if(songType==1){
                    resultIntent.putExtra("notesID", tv.getText());
                }
                //case for scales goes here

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
