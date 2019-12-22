package com.Group17;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying, isGameOver = false;
    public int score = 0;
    public static int screenX, screenY;
    public static float screenRatioX, screenRatioY;
    private Paint paint;
    private Beat[] beats;
    private SharedPreferences prefs;
    private Random random;
    private SoundPool soundPool;
    private List<Bullet> bullets;
    private int sound;
    private Flight flight;
    private GameActivity activity;
    private Background background;
    private int sleeptime = 20;
    private TriggerVisual tracker;

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

        sound = soundPool.load(activity, R.raw.shoot, 1);


        this.screenX = screenX;
        this.screenY = screenY;
        screenRatioX = 2160f / screenX;
        screenRatioY = 1080f / screenY;

        background = new Background(screenX, screenY, getResources());

        flight = new Flight(this, screenY, getResources());

        bullets = new ArrayList<>();

        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);
        beats = new Beat[song.length()];

        try {

            for(int i=0; i<song.length(); i++)
            {
                JSONArray beat = song.getJSONArray(i);

                Note[] notes = new Note[6];
                notes[0] = new Note(getResources(), beat.getInt(0));
                notes[1] = new Note(getResources(), beat.getInt(1));
                notes[2] = new Note(getResources(), beat.getInt(2));
                notes[3] = new Note(getResources(), beat.getInt(3));
                notes[4] = new Note(getResources(), beat.getInt(4));
                notes[5] = new Note(getResources(), beat.getInt(5));
                beats[i] = new Beat(getResources(), notes);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        int i = 0;
        int tempo = 60;
        int speed = (int)( (tempo * beats[0].getWidth() * sleeptime) / 60000.0 );

        tracker = new TriggerVisual(getResources(), beats[0].getWidth()/2, screenY);
        tracker.x = (int) (screenX * 0.2);

        Beat prevbeat = beats[0];
        for (Beat beat : beats) {
            if(i == 0){beat.x = screenX;}
            else{beat.x = prevbeat.x + prevbeat.getWidth();}
            beat.speed = speed;
            prevbeat = beat;
            i++;
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

        if (flight.isGoingUp)
            flight.y -= 30 * screenRatioY;
        else
            flight.y += 30 * screenRatioY;

        if (flight.y < 0)
            flight.y = 0;

        if (flight.y >= screenY - flight.height)
            flight.y = screenY - flight.height;

        List<Bullet> trash = new ArrayList<>();

/*
        for (Bullet bullet : bullets) {

            if (bullet.x > screenX)
                trash.add(bullet);

            bullet.x += 50 * screenRatioX;

            for (Bird bird : birds) {

                if (Rect.intersects(bird.getCollisionShape(),
                        bullet.getCollisionShape())) {

                    score++;
                    bird.x = -500;
                    bullet.x = screenX + 500;
                    bird.wasShot = true;

                }

            }

        }

        for (Bullet bullet : trash)
            bullets.remove(bullet);
*/
        for (Beat beat : beats) {
            beat.x -= beat.speed;
            if (beat.x + beat.getWidth() < 0) {

                //beat.speed = (int) (10 * screenRatioX);
                //beat.x = screenX;

            }
            if (Rect.intersects(beat.getCollisionShape(), flight.getCollisionShape())) {

                //isGameOver = true;
                //return;
            }

        }

    }

    private void draw () {

        if (getHolder().getSurface().isValid()) {

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background.background, background.x, background.y, paint);



            for (Beat beat : beats)
            {
                canvas.drawBitmap(beat.getBeat(), beat.x, beat.y, paint);
            }


            canvas.drawBitmap(tracker.getBitmap(), tracker.x, tracker.y, paint);

            canvas.drawText(score + "", screenX / 2f, 164, paint);

            if (isGameOver) {
                isPlaying = false;
                canvas.drawBitmap(flight.getDead(), flight.x, flight.y, paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting ();
                return;
            }

            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, paint);

            for (Bullet bullet : bullets)
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);

            

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < screenX / 2) {
                    flight.isGoingUp = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                flight.isGoingUp = false;
                if (event.getX() > screenX / 2)
                    flight.toShoot++;
                break;
        }

        return true;
    }

    public void newBullet() {

        if (!prefs.getBoolean("isMute", false))
            soundPool.play(sound, 1, 1, 0, 0, 1);

        Bullet bullet = new Bullet(getResources());
        bullet.x = flight.x + flight.width;
        bullet.y = flight.y + (flight.height / 2);
        bullets.add(bullet);

    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
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
