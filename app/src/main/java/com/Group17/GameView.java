package com.Group17;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;

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

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying, isGameOver = false;

    public static int screenX, screenY;
    private Paint paint;

    private SharedPreferences prefs;
    private SoundPool soundPool;

    private GameActivity activity;
    private Background background;
    private final int sleeptime = 50;
    private TriggerVisual tracker;
    private Fretboard fretboard;

    private ConnectedThread mBluetoothConnection;

    public GameView(Context context) {
        super(context);
    }
    public GameView(GameActivity activity, int screenX, int screenY, JSONArray song) {
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

        int fretsOnScreen = 10;
        int tempo = 40;
        int spacing = 2;

        tracker = new TriggerVisual(getResources(), screenX/fretsOnScreen, screenY);
        tracker.x = (int) (screenX * 0.2);

        fretboard = new Fretboard(getResources(), screenX, (int) (screenY * 0.8), song, fretsOnScreen, spacing, tempo, sleeptime, tracker.x);
        fretboard.setBluetooth(mBluetoothConnection);
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
            canvas.drawBitmap(fretboard.getNextFrame(), 0, (int) (screenY*0.1), paint);
            canvas.drawBitmap(tracker.getBitmap(), tracker.x, tracker.y, paint);



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

    public void setBluetooth(ConnectedThread mBTC){
        this.mBluetoothConnection = mBTC;
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
