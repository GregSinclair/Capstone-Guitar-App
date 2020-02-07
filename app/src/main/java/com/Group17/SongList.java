package com.Group17;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.*;
import android.os.Bundle;

public class SongList extends AppCompatActivity {

    ListView songList;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_screen);


        String[] songNames = {"Funny internet skeleton","Instant regret"}; //put a function here to get the actual song names out of the JSON

        songList=(ListView)findViewById(R.id.list_view_song);
        ArrayAdapter<String> songListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, songNames);
        songList.setAdapter(songListAdapter);
        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                Toast.makeText(getApplicationContext(), tv.getText(), Toast.LENGTH_LONG).show();
            }
        });

    }
}
