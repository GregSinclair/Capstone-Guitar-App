package com.Group17;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import android.content.res.Resources;

import android.graphics.Matrix;
import android.graphics.Paint;

import android.media.AudioAttributes;
import android.media.AudioManager;

import android.media.SoundPool;

import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.io.IOException;
import java.io.InputStream;

import org.json.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import static android.graphics.Bitmap.createScaledBitmap;
import android.content.ServiceConnection;
import android.widget.Toast;

public class TrainingView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying, isGameOver = false;
    private int score = 0;
    public static int screenX, screenY;
    private Paint paint;

    private SharedPreferences prefs;
    private SoundPool soundPool;

    private TrainingActivity activity;
    private Background background;
    private final int sleeptime = 50;

    private Beat trainingBeat;
    private Note[] notes;
    private Bitmap neck;
    private JSONArray song;
    private boolean goToNextBeat=false;
    private BluetoothService myService;
    private boolean isServiceBound=false;

    private int successThreshold=6;
    private int index=0;

    private static final String TAG = "TrainingView";

    private String bluetooth_message="00";

    public TrainingView(Context context) {
        super(context);
    }
    public TrainingView(TrainingActivity activity, int screenX, int screenY, JSONArray song, BluetoothService myService) throws JSONException { //pass in a json array containing 1 note
        super(activity);
        this.song=song;
        this.activity = activity;
        this.myService = myService;
        if(myService!=null){
            isServiceBound=true;
        }
        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);




        this.screenX = screenX;
        this.screenY = screenY;


        background = new Background(screenX, screenY, getResources());



        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);








    }

    private void loadNextBeat() throws JSONException {
        JSONArray beat = song.getJSONArray(index);
        Resources res = getResources();
        //check externally if index is valid

        try {


            Note[] notes = new Note[6];
            notes[0] = new Note(res, beat.getInt(0), screenX, screenY);
            notes[1] = new Note(res, beat.getInt(1), screenX, screenY);
            notes[2] = new Note(res, beat.getInt(2), screenX, screenY);
            notes[3] = new Note(res, beat.getInt(3), screenX, screenY);
            notes[4] = new Note(res, beat.getInt(4), screenX, screenY);
            notes[5] = new Note(res, beat.getInt(5), screenX, screenY);


            trainingBeat = new Beat(res, notes, (int)(screenX*0.1) ,(int)(screenY*0.8), 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final int[] neckSequence = {0,0,1,0,1,0,1,0,1,0,0,2,0,0,1,0,1,0,1,0,0};

        Bitmap[] neckParts = new Bitmap[5];

        int lowest = 99;
        for (int i=0;i<6;i++) {
            if (beat.getInt(i)>=0 && beat.getInt(i)<lowest) {
                lowest = beat.getInt(i);
            }
        }
        if (lowest==99){
            return;
        }
        if(lowest==0){
            lowest=1;
        }
        for(int i=0;i<5;i++){
            switch(neckSequence[lowest+i-1]){
                case 0:
                    neckParts[i] = BitmapFactory.decodeResource(res, R.drawable.sprite_trainerbg0_0);
                    break;
                case 1:
                    neckParts[i] = BitmapFactory.decodeResource(res, R.drawable.sprite_trainerbg1_0);
                    break;
                case 2:
                    neckParts[i] = BitmapFactory.decodeResource(res, R.drawable.sprite_trainerbg2_0);
                    break;
            }
        }
        neck = Bitmap.createBitmap(5*neckParts[0].getWidth(), neckParts[0].getHeight(), Bitmap.Config.ARGB_8888); //scale this for screen resolution after its complete
        for(int i=0;i<5;i++){
            updateBitmap(i*neckParts[0].getWidth(),0,neckParts[i]);
        }

        Bitmap finger = BitmapFactory.decodeResource(res, R.drawable.sprite_trainer_finger_0);
        finger = createScaledBitmap(finger, (int)(neckParts[0].getWidth()*0.5), (int)(neckParts[0].getHeight()*0.15), false);
        for(int i=0;i<6;i++){
            if(beat.getInt(i)>0){
                if(lowest==0){ //can't actually happen currently
                    updateBitmap(((int)(neckParts[0].getWidth()*0.25) + (neckParts[0].getWidth()*(beat.getInt(i)-lowest-1))), (int)(neck.getHeight() - neck.getHeight()*(0+((i+1)*0.164))), finger);
                }
                else{
                    updateBitmap(((int)(neckParts[0].getWidth()*0.25) + (neckParts[0].getWidth()*(beat.getInt(i)-lowest))), (int)(neck.getHeight() - neck.getHeight()*(0+((i+1)*0.164))), finger);
                }
                //updateBitmap(((int)(neckParts[0].getWidth()*0.25) + (neckParts[0].getWidth()*(beat.getInt(i)-lowest-1))), (int)(neck.getHeight() - neck.getHeight()*(0+((i+1)*0.164))), finger); //figure out why it needs i+1... very weird. probably the 0.22 constant
                //updateBitmap((neck.getWidth() - (int)(neckParts[0].getWidth()*0.75) - (neckParts[0].getWidth()*(beat.getInt(i)-lowest))), (int)(neck.getHeight()*(0.025+(i*0.164))), finger); //changed the json while testing, change it back
            }
        }

        neck = createScaledBitmap(neck, (int)(screenX*0.6), (int)(screenY*0.8), false);

        if(isServiceBound && myService != null && myService.isRunning()){
            JSONArray mJSONArray = new JSONArray();
            int[] values = {beat.getInt(0),beat.getInt(1),beat.getInt(2),beat.getInt(3),beat.getInt(4),beat.getInt(5)};
            for(int value : values){mJSONArray.put(value);}
            JSONObject json = new JSONObject();
            json.put("type", 4);
            json.put("sequence", 0);
            json.put("timeStamp", 420);
            json.put("values", mJSONArray);
            json.put("duration", -1); //duration of -1 indicates training mode

            myService.sendMessage(json.toString()); //not totally sure about this one
            return;
        }


    }

    @Override
    public void run() {
        try {
            loadNextBeat();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        while (isPlaying) {

            update ();
            draw ();
            sleep ();

            if(goToNextBeat){
                goToNextBeat=false;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    loadNextBeat();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void updateBitmap(int x, int y, Bitmap newSprite){ //smashes a new sprite onto the current pile

        Bitmap result = Bitmap.createBitmap(neck.getWidth(), neck.getHeight(), neck.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(neck, new Matrix(), null);
        canvas.drawBitmap(newSprite, x, y,null); //based on the    drawBitmap(Bitmap bitmap, float left, float top, Paint paint)
        this.neck = result;
    }

    private void update () {

        if(isServiceBound && myService != null && myService.isRunning()){
            String stringMessage = myService.getMessage();
            if (stringMessage.length()>0) {
                try {
                    JSONObject jsonMessage = new JSONObject(stringMessage);
                    //need to configure this on the RPI end to only send stuff at times that make sense, can't just overwrite the successful ones immediately.
                    //have it do the usual OR thing, and then clear if it detects more than a second of silence?
                    //JSONArray jValues = jsonMessage.getJSONArray("values");
                    int[] values = new int[6];//there is a duplicate call check in the applyFeedback method, though it would make more sense here
                    try {
                        //JSONArray jFeedback = lastFeedback.getJSONArray("values");
                        String sFeedback = jsonMessage.getString("values"); //whole thing is being fucky rn
                        String[] splitFeedback = sFeedback.split(", ");
                        splitFeedback[0] =""+splitFeedback[0].charAt(1); //if theres a -ve number this causes problems
                        splitFeedback[5] =""+splitFeedback[5].charAt(0);
                        Log.d("Fretboard", "split feedback is " + splitFeedback);

                        for(int j=0;j<6;j++){
                            try {
                                values[j] = Integer.parseInt(splitFeedback[j]);

                            }
                            catch(NumberFormatException e){
                                Log.d("Fretboard", "number format exception");
                                return;
                            }
                        }
                        Log.d("Fretboard", "int feedback is " + values);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    trainingBeat.applyFeedback(values);
                    trainingBeat.resetFeedbackCheck();

                    if(song.length()>1){
                        int score=0;
                        for(int i=0;i<6;i++){
                            score += values[i];
                        }
                        if (score>=successThreshold){
                            index++;
                            if (index>=song.length()){
                                index=0;
                            }
                            goToNextBeat=true;
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void draw () {

        if (getHolder().getSurface().isValid()) {

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background.background, background.x, background.y, paint);
            canvas.drawBitmap(trainingBeat.getBeat(), (int)(screenX*0.1),(int)(screenY*0.1), paint);
            canvas.drawBitmap(neck, (int)(screenX*0.3),(int)(screenY*0.1), paint);
            //draw the finger position sprite here. it is static.


            if (isGameOver) {
                isPlaying = false;
                getHolder().unlockCanvasAndPost(canvas);
                waitBeforeExiting ();
                return;
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void waitBeforeExiting() {

        try {
            Thread.sleep(3000);
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void sleep () {
        try {
            Thread.sleep(sleeptime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume () {
    }

    public void beginGame()
    {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause () {

        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();

        return true;
    }

    public String loadJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("songs.json");

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
