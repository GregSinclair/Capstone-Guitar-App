package com.Group17;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;

import android.content.res.Resources;

import android.graphics.Paint;

import android.media.AudioAttributes;
import android.media.AudioManager;

import android.media.SoundPool;

import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;

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

    public TrainingView(Context context) {
        super(context);
    }
    public TrainingView(TrainingActivity activity, int screenX, int screenY, JSONArray song) { //pass in a json array containing 1 note
        super(activity);
        this.activity = activity;
        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();

        } else
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        //sound = soundPool.load(activity, R.raw.shoot, 1);


        GameView.screenX = screenX;
        GameView.screenY = screenY;


        background = new Background(screenX, screenY, getResources());



        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);

        Resources res = getResources();

        JSONArray beat = null;
        try {
            beat = song.getJSONArray(0);

            Note[] notes = new Note[6];
            notes[0] = new Note(res, beat.getInt(0));
            notes[1] = new Note(res, beat.getInt(1));
            notes[2] = new Note(res, beat.getInt(2));
            notes[3] = new Note(res, beat.getInt(3));
            notes[4] = new Note(res, beat.getInt(4));
            notes[5] = new Note(res, beat.getInt(5));


            trainingBeat = new Beat(res, notes, (int)(screenX*0.1) ,screenY);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        while (isPlaying) {

            update ();
            draw ();
            sleep ();

        }

    }

    private void update () {

    }

    private void draw () {

        if (getHolder().getSurface().isValid()) {

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background.background, background.x, background.y, paint);
            canvas.drawBitmap(trainingBeat.getBeat(), (int)(screenX*0.05),(int)(screenY*0.1), paint);

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

    private void saveIfHighScore() {

        if (prefs.getInt("highscore", 0) < score) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", score);
            editor.apply();
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
