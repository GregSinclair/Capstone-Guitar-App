package com.Group17;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

public class Beat {

    public int x, y;
    private int width, height;

    Note[] notes;
    Bitmap beatSprite;

    private boolean triggerCollide;
    boolean feedbackApplied = false;
    Resources res; //dumb implementation, temporary

    public Beat(Resources res, Note[] frets, int width, int height){
        this.width = width;
        this.height = height;
        this.x = 0;
        this.y = 0;
        this.res = res;
        triggerCollide = false;
        beatSprite = BitmapFactory.decodeResource(res, R.drawable.fret);
        beatSprite = Bitmap.createScaledBitmap(beatSprite, width, height, false);
        notes = frets;
        for(int i=0;i<6;i++){
            if(notes[i] != null && notes[i].getNote() != null) {
                updateBitmap(i, notes[i].getNote());
            }
        }
    }

    public void applyFeedback(int[] feedback) { //6 bits
        if (!feedbackApplied) {
            feedbackApplied = true;

            beatSprite = BitmapFactory.decodeResource(res, R.drawable.fret);
            beatSprite = Bitmap.createScaledBitmap(beatSprite, width, height, false);
            for (int i = 0; i < 6; i++) {
                if (notes[i] != null && notes[i].getNote() != null) {
                    if(feedback[i] == 0){
                        updateBitmap(i, notes[i].getNote());
                    }
                    else {
                        updateBitmap(i, notes[i].getFadedNote());
                    }
                }
            }
        }
    }


    public Bitmap getBeat () {return this.beatSprite;}
    public int getWidth() { return this.beatSprite.getWidth();}
    public int getHeight() { return this.beatSprite.getHeight();}

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

    public boolean isTriggered (int checkX) {
        return (x<checkX && x+this.beatSprite.getWidth() >= checkX );
    }
}


