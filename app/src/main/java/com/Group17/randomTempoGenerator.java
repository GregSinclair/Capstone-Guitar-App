package com.Group17;

import android.util.Log;

import org.json.JSONArray;

public class randomTempoGenerator { //creates a random song for tempo practice

    public JSONArray randomSong;

    public randomTempoGenerator(int length, double density){ //density between 0 and 1
        randomSong = new JSONArray();
        JSONArray emptyBeat = new JSONArray();
        JSONArray zeroBeat = new JSONArray();
        for(int i=0;i<6;i++){
            emptyBeat.put(-2);
            zeroBeat.put(0);
        }

        for(int i=0;i<length;i++){
            if (Math.random()<density) {
                randomSong.put(zeroBeat);
            }
            else {
                randomSong.put(emptyBeat);
            }
        }
        Log.d("random tempo generator"," "+randomSong.toString());
    }
}
