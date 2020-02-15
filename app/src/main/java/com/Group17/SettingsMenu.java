package com.Group17;

import android.content.Context;
import android.content.Intent;
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
    ListView settingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_screen);

        initSettings();


        JSONObject json = MemoryInterface.readFile(fileName);

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
                if(tokens[1] == "off"){ //simple toggle
                    tokens[1] = "on";
                }
                else{
                    tokens[1] = "off";
                }
                JSONObject jSettingChange = new JSONObject();
                try {
                    jSettingChange.put(tokens[0], tokens[1]);
                    MemoryInterface.writeFile(jSettingChange, fileName);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void initSettings(){
        //check if the settings file exists, if not call resetSettings
        if(!(MemoryInterface.checkIfFileExists(fileName))){
            resetSettings();
        }
    }

    private void resetScores(){
        //delete scores file
        //seems there's some nuance to this, test it once the scores are implemented
    }

    private void resetSettings(){
        JSONObject jSettings = new JSONObject();
        try {
            jSettings.put("manualTrainingProgression", "off");
            jSettings.put("tempoOverride", "off");
            //add more
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MemoryInterface.writeFile(jSettings, fileName);
    }

}
