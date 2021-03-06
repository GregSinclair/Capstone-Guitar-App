package com.Group17;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GameView extends SurfaceView implements Runnable  {

    private Thread thread;
    private boolean isPlaying, isGameOver = false;

    public static int screenX, screenY;
    private Paint paint;

    private SharedPreferences prefs;
    private SoundPool soundPool;

    private GameActivity activity;
    private Background background;
    private final int sleeptime = 30;
    private TriggerVisual tracker;
    private Fretboard fretboard;
    private String sentMessage = "";
    private String songName;
    private BluetoothService myService;
    private boolean isServiceBound=false;
    private boolean IOPermissions=false;
    private boolean repeatingGame;
    private static final String TAG = "GameView";

    public boolean isFinished=false;

    public GameView(Context context) {
        super(context);
    }
    public GameView(GameActivity activity, int screenX, int screenY, JSONArray song, BluetoothService myService, String songName, boolean repeatingGame, int tempo) {
        super(activity);
        this.songName = songName;
        this.activity = activity;
        this.repeatingGame = repeatingGame;
        this.myService = myService;

        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);

        GameView.screenX = screenX;
        GameView.screenY = screenY;


        background = new Background(screenX, screenY, getResources());



        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);

        int fretsOnScreen = 10;
        int spacing = 2;

        tracker = new TriggerVisual(getResources(), screenX/fretsOnScreen, screenY);
        tracker.x = (int) (screenX * 0.2);

        fretboard = new Fretboard(getResources(), screenX, (int) (screenY * 0.8), song, fretsOnScreen, spacing, tempo, sleeptime, tracker.x, songName, repeatingGame);
    }

    @Override
    public void run() {

        while (!fretboard.finished) {

            update ();
            draw ();
            sleep ();

        }
        activity.finish();

    }

    private void update () {

        try {
            String feedback = myService.getMessage();
            if(feedback.length()>1)
            fretboard.setFeedback(new JSONObject(feedback));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (fretboard.finished){
            if(MemoryInterface.checkReadPermission(activity)){
                if(MemoryInterface.checkWritePermission(activity)) {
                    IOPermissions = true; //this stops it from bypassing that check
                }
            }
            while(!IOPermissions){} //again, this should be modified to timeout eventually
            concludeSong();
            isFinished=true;

        }

        //this will be where we maintain the bluetooth connection and try to reconnect if it drops
        //currently nothing is implemented in the game or training that involves this, assumed it runs perfectly and doesn't need to be manually stopped


    }

    private void draw () {

        if (getHolder().getSurface().isValid()) {

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background.background, background.x, background.y, paint);
            canvas.drawBitmap(fretboard.getNextFrame(), 0, (int) (screenY*0.1), paint);
            canvas.drawBitmap(tracker.getBitmap(), tracker.x, tracker.y, paint);

            String newMessage = fretboard.getNextMessage();
            if(newMessage != sentMessage)
            {
                sentMessage = newMessage;
                myService.sendMessage(newMessage);
            }

            getHolder().unlockCanvasAndPost(canvas);

        }

    }

    private void concludeSong(){
        JSONObject json = new JSONObject();
        try {
            json.put(songName, fretboard.getFinalScore());
            MemoryInterface.writeFile(json, "scores.txt");
        } catch (JSONException e) {
            e.printStackTrace();
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

    public void beginGame() //old threading method
    {
        thread = new Thread(this);
        thread.start();
        //while(!isFinished){}


    }

    public void beginUnthreadedGame(){ //just causes blackscreen
        run();
    }

    public void pause () {

        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void setPermissions(){
        IOPermissions=true;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_UP:

                break;
        }

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
