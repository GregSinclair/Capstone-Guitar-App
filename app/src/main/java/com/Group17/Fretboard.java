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
    private JSONObject lastFeedback = null;
    private String nextMessage;

    private BluetoothService myService;
    private boolean isServiceBound=false;

    private static final String TAG = "Fretboard";

    private String bluetooth_message="00";

    private int duration;

    public Fretboard(Resources res, int screenX, int screenY, JSONArray jsonSong, int fretsOnScreen, int spacing, int tempo, int sleeptime, int trackerX, BluetoothService myService) {

        this.myService = myService;

        duration = tempo; //figure out the actual conversion later

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
        beatTreadmill = new ArrayList<>();

        for(int i=0; i<fretsOnScreen+1; i++)
        {
            if( i == fretsOnScreen)
            {
                beatTreadmill.add(createEmptyBeat());
                getNextBeat();
            }
            else{ beatTreadmill.add(createEmptyBeat()); }
        }
        fretboard = Bitmap.createBitmap(beatWidth*(fretsOnScreen+1), beatHeight, Bitmap.Config.ARGB_8888);
        initImage();

    }

    private Beat createEmptyBeat()
    {
        //Creates a beat with nothing in it
        Note[] emptyNotes = new Note[6];
        for (int i = 0; i < 6; i++)
        {
            emptyNotes[i] = new Note(res, -2, screenX, screenY);
        }
        return new Beat(res, emptyNotes, beatWidth, beatHeight, beatIndexCounter++);
    }

    private void initImage()
    {
        //Creates main image for fretboard
        //ie. concatinates beats together
        Iterator<Beat> frets = beatTreadmill.iterator();
        Canvas comboImage = new Canvas(fretboard);
        int i = 0;
        while (frets.hasNext()) {
            Beat fret = frets.next();
            comboImage.drawBitmap(fret.getBeat(), i*beatWidth, 0, null);
            fret.setPosition(i * beatWidth);
            i++;
        }
    }

    private void setImage()
    {
        Iterator<Beat> frets = beatTreadmill.iterator();
        int i = 0;
        while (frets.hasNext()) {
            Beat fret = frets.next();
            fret.setPosition(i * beatWidth);
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
                notes[0] = new Note(res, beat.getInt(0), screenX, screenY);
                notes[1] = new Note(res, beat.getInt(1), screenX, screenY);
                notes[2] = new Note(res, beat.getInt(2), screenX, screenY);
                notes[3] = new Note(res, beat.getInt(3), screenX, screenY);
                notes[4] = new Note(res, beat.getInt(4), screenX, screenY);
                notes[5] = new Note(res, beat.getInt(5), screenX, screenY);
                beatTreadmill.remove(0);
                beatTreadmill.add(new Beat(res, notes, beatWidth, beatHeight, beatCounter++));
            } catch (JSONException e) {e.printStackTrace();}

            spaceInterval = 0;
        }
        else
        {
            beatTreadmill.remove(0);
            beatTreadmill.add(createEmptyBeat());
            spaceInterval++;
        }
    }

    private boolean getFeedback(){

        //move this into gameview update and have it pass in the message to a global variable
        try {
            JSONObject feedback = new JSONObject(myService.getMessage());
            if(lastFeedback.getInt("sequence")!=feedback.getInt("sequence")){ //tempted to force it to check if its +1 of last time, but that could lead to one sequence error fucking everything up
                lastFeedback = feedback;
                return true;
            }
            else {
                Log.d(TAG, "Fretboard: sequence error in received message");
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void checkFeedback(int i){

        try {
            int sequence = lastFeedback.getInt("sequence");
            if(sequence != i){
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int[] feedback = new int[6];//there is a duplicate call check in the applyFeedback method, though it would make more sense here
        try {
            JSONArray jFeedback = lastFeedback.getJSONArray("values");

            for(int j=0;j<6;j++){
                try {
                    feedback[j]=jFeedback.getInt(j);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Beat fret = beatTreadmill.get(i);
        fret.applyFeedback(feedback);
        Canvas comboImage = new Canvas(fretboard);
        comboImage.drawBitmap(fret.getBeat(), beatWidth * i, 0, null); //this implementation is still weird to me.
                                                                                    //the bitmap is larger than the screen? this might be why it runs like ass on my main phone

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

            if(fret.isSent() && !fret.gottenFeedback()){ //case where we are looking for the response
                getFeedback(); //was originally going to have this return a boolean, but that's useless if this calls multiple times in a loop
                checkFeedback(i);
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
                    messageJSON.put("sequence",i);
                    messageJSON.put("timeStamp",0);
                    messageJSON.put("values",mJSONArray);  //there may be some inconsistency here with putting it as an int array vs json array
                                                                //Using a json array is better. gonna go through and change all to that
                    messageJSON.put("duration",duration);
                }
                catch(org.json.JSONException er)
                {

                }
                Log.d("Fretboard", messageJSON.toString());
                nextMessage = messageJSON.toString();
                myService.sendMessage(nextMessage); //not totally sure about this one

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

    public String getNextMessage() {
        return nextMessage;
    }

    public void setFeedback(JSONObject feedback){
        lastFeedback = feedback;
    }

}
