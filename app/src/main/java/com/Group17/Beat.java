package com.Group17;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

import static com.Group17.GameView.screenRatioX;
import static com.Group17.GameView.screenRatioY;

import static com.Group17.GameView.screenX;
import static com.Group17.GameView.screenY;

public class Beat {

    public int x, y, speed;
    private int width, height;
    Note[] notes;
    Bitmap beatSprite;
    Bitmap beatSprite2;
    Bitmap beatSprite3;
    public Beat(Resources res, Note[] frets){

        beatSprite = BitmapFactory.decodeResource(res, R.drawable.fret);
        beatSprite2 = BitmapFactory.decodeResource(res, R.drawable.fretdot);
        width = screenX/10;
        height = (int)(screenY * 0.8);
        this.x = 0;
        this.y = (int) (height * 0.1);
        beatSprite = Bitmap.createScaledBitmap(beatSprite, width, height, false);
        beatSprite2 = Bitmap.createScaledBitmap(beatSprite2, width, height, false);
        notes = frets;
        for(int i=0;i<6;i++){
            if(notes[i] != null && notes[i].getNote() != null) {
                updateBitmap(i, notes[i].getNote());
            }
        }
        combine();

    }

    public Bitmap getBeat () {
        return this.beatSprite3;
    }
    public Bitmap getEmptyBeat () {
        return this.beatSprite2;
    }
    public int getWidth() { return this.beatSprite3.getWidth();}
    public int getHeight() { return this.beatSprite3.getHeight();}
    private void updateBitmap(int position, Bitmap newSprite){ //smashes a new sprite onto the current pile
        int top = beatSprite.getHeight()*position/6 + (beatSprite.getHeight()/6 - newSprite.getHeight())/2;
        int left = (beatSprite.getWidth() - newSprite.getWidth())/2;
        Bitmap result = Bitmap.createBitmap(beatSprite.getWidth(), beatSprite.getHeight(), beatSprite.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(beatSprite, new Matrix(), null);
        canvas.drawBitmap(newSprite, left, top,null); //based on the    drawBitmap(Bitmap bitmap, float left, float top, Paint paint)
        this.beatSprite = result;
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    private void combine() {
        Bitmap bmOverlay = Bitmap.createBitmap(beatSprite.getWidth() + beatSprite2.getWidth(), beatSprite.getHeight(), beatSprite.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(beatSprite, new Matrix(), null);
        canvas.drawBitmap(beatSprite2, beatSprite.getWidth(), 0,null);
        beatSprite3 = bmOverlay;
    }

    Rect getCollisionShape () {
        return new Rect(x, y, x + width, y + height);
    }
}


