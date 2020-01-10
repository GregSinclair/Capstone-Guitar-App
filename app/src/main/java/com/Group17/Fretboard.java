package com.Group17;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Fretboard {

    private int xsize, ysize;
    private int posX;
    private int beatWidth, beatHeight;
    private int fretsOnScreen, spacing;
    private int speed;
    private List<Beat> beatTreadmill;
    private JSONArray song;
    private Resources res;
    private Note[] emptyBeat;
    private int beatCounter;
    private int spaceInterval;
    private Bitmap fretboard;
    private int trackerX;


    private ConnectedThread mBluetoothConnection;
    private JSONObject lastFeedback = null;

    public Fretboard(Resources res, int screenX, int screenY, JSONArray jsonSong, int fretsOnScreen, int spacing, int tempo, int sleeptime, int trackerX) {


        song = jsonSong;
        xsize = screenX;
        ysize = screenY;
        posX = 0;
        this.spacing = spacing;
        this.fretsOnScreen = fretsOnScreen;
        this.res = res;
        this.trackerX = trackerX;
        beatCounter = 0;
        spaceInterval = spacing;
        beatWidth = screenX/fretsOnScreen;
        beatHeight = screenY;
        speed = (int)( (tempo * beatWidth * (spacing + 1) * sleeptime) / 60000.0 );
        beatTreadmill = new ArrayList<>();
        Note emptyNote = new Note(res, -2);
        emptyBeat = new Note[6];
        emptyBeat[0] = emptyNote;
        emptyBeat[1] = emptyNote;
        emptyBeat[2] = emptyNote;
        emptyBeat[3] = emptyNote;
        emptyBeat[4] = emptyNote;
        emptyBeat[5] = emptyNote;

        for(int i=0; i<fretsOnScreen+1; i++)
        {
            if( i == fretsOnScreen)
            {
                beatTreadmill.add(new Beat(res, emptyBeat, beatWidth, beatHeight));
                getNextBeat();
            }
            else{ beatTreadmill.add(new Beat(res, emptyBeat, beatWidth, beatHeight)); }
        }
        fretboard = Bitmap.createBitmap(beatWidth*(fretsOnScreen+1), beatHeight, Bitmap.Config.ARGB_8888);
        initImage();

    }

    private void initImage()
    {
        Iterator<Beat> frets = beatTreadmill.iterator();
        Canvas comboImage = new Canvas(fretboard);
        int i = 0;
        while (frets.hasNext()) {
            Beat fret = frets.next();
            comboImage.drawBitmap(fret.getBeat(), i*beatWidth, 0, null);
            i++;
        }
    }

    private void setImage()
    {
        Iterator<Beat> frets = beatTreadmill.iterator();
        int i = 0;
        while (frets.hasNext()) {
            Beat fret = frets.next();
            fret.x = i * beatWidth;
            i++;
        }
        Beat fret = beatTreadmill.get(beatTreadmill.size()-1);
        Bitmap croppedFretboard = Bitmap.createBitmap(fretboard, beatWidth, 0, beatWidth * fretsOnScreen, fretboard.getHeight());
        Canvas comboImage = new Canvas(fretboard);
        comboImage.drawBitmap(croppedFretboard, 0, 0, null);
        comboImage.drawBitmap(fret.getBeat(), beatWidth * fretsOnScreen, 0, null);
    }

    private void getNextBeat()
    {
        if(spaceInterval >= spacing)
        {
            try {
                JSONArray beat = song.getJSONArray(beatCounter);
                if(!(song.getJSONArray(beatCounter+1).getInt(0)==-5)) {
                    beatCounter++;
                }


                Note[] notes = new Note[6];
                notes[0] = new Note(res, beat.getInt(0));
                notes[1] = new Note(res, beat.getInt(1));
                notes[2] = new Note(res, beat.getInt(2));
                notes[3] = new Note(res, beat.getInt(3));
                notes[4] = new Note(res, beat.getInt(4));
                notes[5] = new Note(res, beat.getInt(5));
                Beat tempBeat = beatTreadmill.get(0);
                beatTreadmill.remove(0);
                beatTreadmill.add(new Beat(res, notes, beatWidth, beatHeight));
            } catch (JSONException e) {e.printStackTrace();}

            spaceInterval = 0;
        }
        else
        {
            Beat tempBeat = beatTreadmill.get(0);
            beatTreadmill.remove(0);
            beatTreadmill.add(new Beat(res, emptyBeat, beatWidth, beatHeight));
            spaceInterval++;
        }
    }

    public void setBluetooth(ConnectedThread mBTC){
        mBluetoothConnection=mBTC;
    }

    private void getFeedback(){
        lastFeedback = mBluetoothConnection.getLastJSONMessage();
    }

    private void checkFeedback(int i){

        try {
            int sequence = lastFeedback.getInt("beat");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int[] feedback = new int[6];//there is a duplicate call check in the applyFeedback method, though it would make more sense here
        for(int j=0;j<6;j++){
            try {
                feedback[j]=lastFeedback.getInt(""+j); //if theres some array issue, make sure this is allowed
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Beat fret = beatTreadmill.get(i);
        fret.applyFeedback(feedback); //would pass in feedback here if it exists.
        Canvas comboImage = new Canvas(fretboard);
        comboImage.drawBitmap(fret.getBeat(), beatWidth * i, 0, null);

    }

    public Bitmap getNextFrame()
    {
        posX = posX + speed;

        if(posX >= beatWidth)
        {
            posX = posX - beatWidth;
            getNextBeat();
            setImage();

        }
            //getFeedback(); //disabled until we have proper JSON object passing

        int hitbox = posX + trackerX;
        Iterator<Beat> frets = beatTreadmill.iterator();
        int i = 0;
        while (frets.hasNext()) {
            Beat fret = frets.next();
            if(fret.isTriggered(hitbox))
            {
                //checkFeedback(i); //disabled until we have proper JSON object passing
            }
            i++;
        }
        //Canvas comboImage = new Canvas(fretboard);
        //comboImage.drawBitmap(bird, hitbox, 0, null);


        Bitmap croppedFretboard = Bitmap.createBitmap(fretboard, posX, 0, beatWidth * fretsOnScreen, fretboard.getHeight());
        //Canvas comboImage = new Canvas(croppedFretboard);
        //comboImage.drawBitmap(bird, hitbox, 0, null);

        return croppedFretboard;
    }

}
