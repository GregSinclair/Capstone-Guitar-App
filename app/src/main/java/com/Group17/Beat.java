package com.Group17;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

public class Beat {

    private int xStart;
    private int width, height;
    private int beatIndex;
    Note[] notes;
    Bitmap beatSprite = null;

    private boolean drawn = false;
    private boolean emptyBeat=true;
    private boolean sentBeat=false;

    private int[] feedback = new int[6]; //use this so that the Beat array can be used to reconstruct the song afterwards and give feedback

    boolean feedbackApplied = false;
    Resources res; //dumb implementation, temporary

    public Beat(Resources res, Note[] frets, int width, int height, int index) {
        this.width = width;
        this.height = height;
        this.xStart = 0;
        this.res = res;
        beatIndex = index;
        notes = frets;
        for(int i=0;i<notes.length;i++){
            if(notes[i].getNoteNum()!=-2){
                emptyBeat=false;
            }
        }
    }

    private void initSprite(){
        beatSprite = BitmapFactory.decodeResource(res, R.drawable.fret);
        beatSprite = Bitmap.createScaledBitmap(beatSprite, width, height, false);

        //throw the arrow in here based on some algorithm
        for (int i = 0; i < 6; i++) {
            if (notes[i] != null && notes[i].getNote() != null) {
                updateBitmap(5-i, notes[i].getNote());
            }
        }
    }

    public void applyFeedback(int[] feedback) { //6 bits
        if (!feedbackApplied && feedback!=null) {
            feedbackApplied = true;
            this.feedback = feedback;
            beatSprite = BitmapFactory.decodeResource(res, R.drawable.fret);
            beatSprite = Bitmap.createScaledBitmap(beatSprite, width, height, false);
            Log.d("Beat", "feedback is " + feedback[0] + feedback[1]);
            for (int i = 0; i < 6; i++) {
                if (notes[i] != null && notes[i].getNote() != null) {
                    if (feedback[i] != 1) { //1 for good, other numbers will eventually be reserved for different feedback
                        updateBitmap(5-i, notes[i].getNote());
                    } else {
                        updateBitmap(5-i, notes[i].getFadedNote());
                    }
                }
            }
        }
    }


    public Bitmap getBeat() {
        if (!drawn){
            initSprite();
            drawn = true;
        }
        return this.beatSprite;
    }

    public void releaseBitmap(){
        beatSprite = null;
    }

    public int getWidth() {
        return this.beatSprite.getWidth();
    }

    public int getHeight() {
        return this.beatSprite.getHeight();
    }

    public boolean isEmpty(){
        return emptyBeat;
    }

    private void updateBitmap(int position, Bitmap newSprite) { //smashes a new sprite onto the current pile
        int top = beatSprite.getHeight() * position / 6 + (beatSprite.getHeight() / 6 - newSprite.getHeight()) / 2;
        int left = (beatSprite.getWidth() - newSprite.getWidth()) / 2;
        Bitmap result = Bitmap.createBitmap(beatSprite.getWidth(), beatSprite.getHeight(), beatSprite.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(beatSprite, new Matrix(), null);
        canvas.drawBitmap(newSprite, left, top, null); //based on the drawBitmap(Bitmap bitmap, float left, float top, Paint paint)
        this.beatSprite = result;
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    public boolean isTriggered(int checkX) {
        if(!drawn){
            return false;
        }
        return (xStart < checkX && xStart + this.beatSprite.getWidth() >= checkX);
    }

    public void setPosition(int x) {
        this.xStart = x;
    }

    public boolean isSent() {
        if(beatIndex == 0 || beatIndex == -5){
            return true;
        }
        return sentBeat;
    }
    public void sendBeat() {
        sentBeat = true;
    }
    public int[] getNoteArray()
    {
        int noteNums[] = new int[6];
        for(int i = 0; i<6; i++)
        {
            noteNums[i] = notes[i].getNoteNum();
        }
        return noteNums;
    }
    public int getIndex(){return beatIndex;}

    public boolean gottenFeedback(){
        if(beatIndex == 0 || beatIndex == -5){
            return true;
        }
        return feedbackApplied;
    }

    public int viewFeedback(){ //this assumes boolean feedback, will have to rework it once we have nuanced feedback
        int result =0;
        for(int i=0;i<6;i++){
            if(notes[i].getNoteNum()!=-2) {
                result += feedback[i];
            }
        }
        return result;
    }

    public int maxFeedback(){
        int result =0;
        for(int i=0;i<6;i++){
            if(notes[i].getNoteNum()!=-2) {
                result += 1;
            }
        }
        return result;
    }

    public void resetFeedbackCheck(){
        feedbackApplied = false;
    }
}


