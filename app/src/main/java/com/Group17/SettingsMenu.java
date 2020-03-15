package com.Group17;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsMenu extends AppCompatActivity {

    private static final String TAG = "SongList";
    private final String fileName = "userSettings.txt";
    private boolean gotPermissions = false;
    ListView settingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //do the permission checks before any of this
        //pause and wait for the permission thing to resume it
        if(MemoryInterface.checkReadPermission(this)){
            if(MemoryInterface.checkWritePermission(this)) {
                gotPermissions = true; //this stops it from bypassing that check
            }
        }

        while(!gotPermissions){} //ideally it wouldnt freeze up if the permissions arent granted, but I should really have this loop terminate after 10s or so
        //Still need this so it doesn't try to load settings on an empty file
        setContentView(R.layout.list_screen);

        initSettings();

        loadButtons();


    }

    private void loadButtons(){
        JSONObject json = MemoryInterface.readFile(fileName);

        //does this have to be in the onCreate? Throwing this in a function and recreating the list after a press would be good. Try that after this is stable

        List<String> settingNames = new ArrayList<String>();
        Iterator<String> settingIterator = json.keys();
        while (settingIterator.hasNext()) {
            String key = settingIterator.next();
            try {
                settingNames.add(key + ": "+ json.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        settingList = (ListView) findViewById(R.id.list_view_song);
        ArrayAdapter<String> songListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settingNames);
        settingList.setAdapter(songListAdapter);
        settingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                String tvText = (String)tv.getText();
                String[] tokens = tvText.split(": ");
                if(tokens[0].equals("Tempo Override")) {
                    if(tokens[1].equals("off")){
                        tokens[1]=""+40;
                    }
                    else{
                        int tempo = Integer.parseInt(tokens[1]);
                        if (tempo>=400){
                            tokens[1]="off";
                        }
                        else{
                            tokens[1]=""+(tempo+20);
                        }
                    }
                }
                else{
                    if (tokens[1].equals("off")) { //simple toggle
                        tokens[1] = "on";
                        Toast.makeText(getApplicationContext(), tokens[0] + " :" + tokens[1], Toast.LENGTH_SHORT).show();
                    } else {
                        tokens[1] = "off";
                        Toast.makeText(getApplicationContext(), tokens[0] + " :" + tokens[1], Toast.LENGTH_SHORT).show();
                    }
                }
                JSONObject jSettingChange = new JSONObject();
                try {
                    jSettingChange.put(tokens[0], tokens[1]);
                    MemoryInterface.writeFile(jSettingChange, fileName);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loadButtons();
            }
        });
    }

    private void initSettings(){
        //check if the settings file exists, if not call resetSettings
        if(!(MemoryInterface.checkIfFileExists(fileName))){
            resetSettings();
        }
        //if the settings got fucked up somehow (empty file) it'll be necessary to return the lost settings
        try {
            JSONObject original = MemoryInterface.readFile(fileName);
            JSONObject jSettings = getDefaultSettings();
            Iterator<String> settingsIterator=jSettings.keys();
            while(settingsIterator.hasNext()) {
                String current = settingsIterator.next();
                if(!original.has(current)){
                    original.put(current, jSettings.get(current));
                }
            }
            MemoryInterface.writeFile(original, fileName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void resetScores(){
        //delete scores file
        //seems there's some nuance to this, test it once the scores are implemented
    }

    private JSONObject getDefaultSettings(){
        JSONObject jSettings = new JSONObject();
        try {
            jSettings.put("Tempo Override", "off");
            jSettings.put("Always Training Mode", "off");
            jSettings.put("Manual Training Mode Progression", "off"); //still gotta do this
            jSettings.put("Unlock All Songs", "off");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jSettings;
    }

    private void resetSettings(){
        JSONObject jSettings = getDefaultSettings();
        MemoryInterface.writeFile(jSettings, fileName);
    }

    @Override //throw this in the game as well
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MemoryInterface.checkWritePermission(this);
                } else {
                    Toast.makeText(getApplicationContext(),"Unable to get permissions: READ",Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                return;
            }
            case 2: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gotPermissions = true;
                } else {
                    Toast.makeText(getApplicationContext(),"Unable to get permissions: WRITE",Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}
