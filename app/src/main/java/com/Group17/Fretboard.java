package com.Group17;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.Group17.GameView.screenX;
import static com.Group17.GameView.screenY;


public class Fretboard {

    private int xsize, ysize;
    private int posX;
    private int beatWidth, beatHeight;
    private int fretsOnScreen, spacing;
    private int speed;
    private List<Beat> beatTreadmill;
    private JSONArray song;
    private Resources res;
    private int beatCounter;
    private int spaceInterval;
    private Bitmap fretboard;
    private int trackerX;
    private Beat currentBeat;
    private int beatIndexCounter;
    private JSONObject lastFeedback;
    public JSONObject feedback = null;
    private String nextMessage;
    private String songName;
    private int lastBeatPos = 0;
    private int lastTrueBeat = 0;
    public boolean finished = false;

    private static final String TAG = "Fretboard";

    private String bluetooth_message="00";

    private int duration;

    public Fretboard(Resources res, int screenX, int screenY, JSONArray jsonSong, int fretsOnScreen, int spacing, int tempo, int sleeptime, int trackerX, String songName) {
        duration = 1500; //figure out the actual conversion later
        beatIndexCounter = 0;
        song = jsonSong;
        xsize = screenX;
        ysize = screenY;
        posX = 0;
        currentBeat = null;
        this.spacing = spacing;
        this.fretsOnScreen = fretsOnScreen;
        this.res = res;
        this.trackerX = trackerX;
        nextMessage = "";
        beatCounter = 0;
        spaceInterval = spacing;
        beatWidth = screenX/fretsOnScreen;
        beatHeight = screenY;
        speed = (int)( (tempo * beatWidth * (spacing + 1) * sleeptime) / 60000.0 );
        beatTreadmill = new ArrayList<>(); //jan 30: gonna rework beatTreadmill to hang onto the whole song now


        this.songName = songName;

        for(int i=0; i<fretsOnScreen; i++) //fill up the screen with empty beats
        {
            beatTreadmill.add(createEmptyBeat(lastBeatPos++));
        }
        loadBeatTreadmill();
        lastTrueBeat = lastBeatPos - 1;
        for(int i=0; i<fretsOnScreen+1; i++) //fill up the screen with empty beats
        {
            beatTreadmill.add(createEmptyBeat(lastBeatPos++));
        }
        lastBeatPos--;
        fretboard = Bitmap.createBitmap(beatWidth*(fretsOnScreen+4), beatHeight, Bitmap.Config.ARGB_8888);
        drawFullImage();
        lastFeedback = new JSONObject();
        try {
            lastFeedback.put("sequence",0);
            lastFeedback.put("type",3);
            lastFeedback.put("timeStamp",0);
            lastFeedback.put("values",new int[6]);
            lastFeedback.put("duration",0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadBeatTreadmill() { //loads up the entire song at start
        spaceInterval = 0;
        int b = 0;
        try {

            while (b < song.length()) {
                JSONArray beat = song.getJSONArray(b);

                if (spaceInterval >= spacing) {
                    Note[] notes = new Note[6];
                    notes[0] = new Note(res, beat.getInt(0), screenX, screenY);
                    notes[1] = new Note(res, beat.getInt(1), screenX, screenY);
                    notes[2] = new Note(res, beat.getInt(2), screenX, screenY);
                    notes[3] = new Note(res, beat.getInt(3), screenX, screenY);
                    notes[4] = new Note(res, beat.getInt(4), screenX, screenY);
                    notes[5] = new Note(res, beat.getInt(5), screenX, screenY);
                    beatTreadmill.add(new Beat(res, notes, beatWidth, beatHeight, lastBeatPos));
                    b++;
                    spaceInterval = 0;
                } else {
                    beatTreadmill.add(createEmptyBeat(lastBeatPos));
                    spaceInterval++;
                }
                lastBeatPos++; //removed the case where it infinitely sends a note
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Beat createEmptyBeat(int b)
    {
        //Creates a beat with nothing in it
        Note[] emptyNotes = new Note[6];
        for (int i = 0; i < 6; i++)
        {
            emptyNotes[i] = new Note(res, -2, screenX, screenY);
        }
        return new Beat(res, emptyNotes, beatWidth, beatHeight, b);
    }


    private void drawFullImage(){ //new way to draw the n+1 beats

        //Log.d("Fretboard", "beatCounter " + beatCounter);
        Beat[] drawnBeats = new Beat[fretsOnScreen+1];
        Iterator<Beat> frets = beatTreadmill.iterator();
        /*
        int i = 0;
        while (frets.hasNext() && i<beatCounter) {
            Beat fret = frets.next();
            i++;
        }
        int j=0;
        while (frets.hasNext() && j<fretsOnScreen+1) {
            Beat fret = frets.next();
            fret.setPosition(j * beatWidth); //they need to know their position for collision purposes
            drawnBeats[j] = fret;
            j++;
        }
        */
        int j=0;
        while (j<fretsOnScreen+1) {
            Beat fret = beatTreadmill.get(beatCounter + j);
            fret.setPosition(j * beatWidth); //they need to know their position for collision purposes
            drawnBeats[j] = fret;
            j++;
        }
        //now the n+1 beats are selected, draw them
        Canvas comboImage = new Canvas(fretboard);
        for(int m=0;m<fretsOnScreen+1;m++){
            comboImage.drawBitmap(drawnBeats[m].getBeat(), m*beatWidth, 0, null);
        }
        //this is the full image, gets scrolled along the screen

    }



    public void setFeedback(JSONObject newFeedback) {
        try {
            if(newFeedback.getInt("sequence") > lastFeedback.getInt("sequence")) { //note that this means some can be skipped. might be troubling. look into possible setups for this later
                lastFeedback = newFeedback;
               // Log.d("Fretboard", "feedback updated");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkFeedback(int i){

        if(i==0){
            return;
        }

        try {
            int sequence = lastFeedback.getInt("sequence");
            if(sequence != i){
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        int[] feedback = new int[6];//there is a duplicate call check in the applyFeedback method, though it would make more sense here
        try {
            //JSONArray jFeedback = lastFeedback.getJSONArray("values");
            String sFeedback = lastFeedback.getString("values"); //whole thing is being fucky rn
            String[] splitFeedback = sFeedback.split(", ");
            splitFeedback[0] =""+splitFeedback[0].charAt(1); //if theres a -ve number this causes problems
            splitFeedback[5] =""+splitFeedback[5].charAt(0);
            //Log.d("Fretboard", "split feedback is " + splitFeedback);

            for(int j=0;j<6;j++){
                try {
                    feedback[j] = Integer.parseInt(splitFeedback[j]);

                }
                catch(NumberFormatException e){
                    Log.d("Fretboard", "number format exception");
                    return;
                }
            }
            //Log.d("Fretboard", "int feedback is " + feedback);

            /*
            for(int j=0;j<6;j++){
                try {
                    feedback[j]=jFeedback.getInt(j);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

             */
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.d("Fretboard", "feedback is " + feedback[0] + feedback[1]+feedback[2] + feedback[3]+feedback[4] + feedback[5]);

        //Beat fret = beatTreadmill.get(i); //gotta use iterator bc of all the useless ones
        Beat fret;
        Iterator<Beat> frets = beatTreadmill.iterator();

        while (frets.hasNext()) {
            fret = frets.next();
            if(fret.getIndex()==i){
                fret.applyFeedback(feedback);
                //Canvas comboImage = new Canvas(fretboard);
                //comboImage.drawBitmap(fret.getBeat(), beatWidth * i, 0, null);
                beatCounter--;
                drawFullImage();
                beatCounter++;
                return;
            }
        }
    }



    public Bitmap getNextFrame()
    {
        posX = posX + speed;

        if(posX >= beatWidth)
        {
            posX = posX - beatWidth;
            drawFullImage();
            beatCounter++;
        }
            //getFeedback(); //disabled until we have proper JSON object passing

        int hitbox = posX + trackerX;
        Iterator<Beat> frets = beatTreadmill.iterator();
        int i = 0;
        Beat fret=frets.next();
        while (frets.hasNext()&&fret.getIndex() <= lastTrueBeat) {
            if(fret.isSent() && !fret.gottenFeedback()){ //case where we are looking for the response
                checkFeedback(fret.getIndex());
                //looks like the sprite is redrawn in CF, is this all there is?
            }
            else if(fret.isTriggered(hitbox) && !fret.isSent()) { //case where we must send a new message
                fret.sendBeat();
                int noteArray[] = fret.getNoteArray();
                JSONObject messageJSON = new JSONObject();
                JSONArray mJSONArray = new JSONArray();
                for(int value : noteArray){mJSONArray.put(value);}
                try
                {
                    messageJSON.put("type",2);
                    messageJSON.put("sequence",fret.getIndex());
                    messageJSON.put("timeStamp",0);
                    messageJSON.put("values",mJSONArray);  //there may be some inconsistency here with putting it as an int array vs json array
                                                                //Using a json array is better. gonna go through and change all to that
                    messageJSON.put("duration",duration);
                }
                catch(org.json.JSONException er)
                {

                }
               // Log.d("Fretboard", messageJSON.toString());
                nextMessage = messageJSON.toString();
                //myService.sendMessage(nextMessage); //not totally sure about this one

            }
            i++;
            fret = frets.next();
        }

        //this is such a stupid place to do the song end wrapup, I should set a flag and move all this to view or activity instead
        if (beatCounter >= lastBeatPos - fretsOnScreen) { //this is the final fret, implying the song is finished
            //verify that this works, needs to actually register the feedback for the last beat, might need i-1 or something
            int result=0;
            Iterator<Beat> fretsFinal = beatTreadmill.iterator();
            while (fretsFinal.hasNext()) {
                fret = fretsFinal.next();
                result += fret.viewFeedback();
            }
            result = (int)((result*100.00)/(6*beatTreadmill.size()));
            JSONObject json = new JSONObject();
            try {
                json.put(songName,result);
                MemoryInterface.writeFile(json, "scores.txt");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //write the results to file
            Log.d(TAG, "Fretboard: score is "+result);
            finished = true;
        }


        Bitmap croppedFretboard = Bitmap.createBitmap(fretboard, posX, 0, beatWidth * fretsOnScreen, fretboard.getHeight());

        return croppedFretboard;
    }

    public String getNextMessage() {
        return nextMessage;
    }

}
