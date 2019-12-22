package com.Group17;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Fretboard {

    private int xsize, ysize;
    private int fretsOnScreen, spacing;
    private int frets;
    private Beat[] beatTreadmill;
    private JSONArray song;
    private Resources res;
    private Note[] emptyBeat;
    private int beatCounter;
    private int spaceinterval;

    public Fretboard(Resources res, int screenX, int screenY, JSONArray jsonSong, int fretsOnScreen, int spacing) {

        song = jsonSong;
        xsize = screenX;
        ysize = screenY;
        this.spacing = spacing;
        this.fretsOnScreen = fretsOnScreen;
        this.res = res;
        beatCounter = 0;
        spaceinterval = spacing;

        //paint = new Paint();
        //paint.setTextSize(128);
        //paint.setColor(Color.WHITE);

        //Enough to fill frets on the screen + one extra set
        beatTreadmill = new Beat[(fretsOnScreen + 1)];
        Note emptyNote = new Note(res, -2);
        emptyBeat = new Note[6];
        emptyBeat[0] = emptyNote;
        emptyBeat[1] = emptyNote;
        emptyBeat[2] = emptyNote;
        emptyBeat[3] = emptyNote;
        emptyBeat[4] = emptyNote;
        emptyBeat[5] = emptyNote;

        for(int i=0; i<beatTreadmill.length; i++)
        {
            if( i == beatTreadmill.length-1){getNextBeat();}
            else{ beatTreadmill[i] = new Beat(res, emptyBeat); }
        }

    }

    private void getNextBeat()
    {
        if(spaceinterval >= spacing)
        {
            try {
                JSONArray beat = song.getJSONArray(beatCounter);
                beatCounter++;
                Note[] notes = new Note[6];
                notes[0] = new Note(res, beat.getInt(0));
                notes[1] = new Note(res, beat.getInt(1));
                notes[2] = new Note(res, beat.getInt(2));
                notes[3] = new Note(res, beat.getInt(3));
                notes[4] = new Note(res, beat.getInt(4));
                notes[5] = new Note(res, beat.getInt(5));
                beatTreadmill[beatTreadmill.length-1] = new Beat(res, notes);
            } catch (JSONException e) {e.printStackTrace();}

            spaceinterval = 0;
        }
        else
        {
            beatTreadmill[beatTreadmill.length-1] = new Beat(res, emptyBeat);
            spaceinterval++;
        }
    }

}
