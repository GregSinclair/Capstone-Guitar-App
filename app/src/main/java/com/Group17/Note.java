package com.Group17;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.*;

import static com.Group17.GameView.screenX;
import static com.Group17.GameView.screenY;

public class Note {
    public int width, height;
    public int noteNum;
    Bitmap note;
    Bitmap box; //these are maintained so that the sprite can be reproduced. alternate idea is to create all needed sprites in the constructor and swap as needed, do this in v2
    public Note (Resources res, int x) {

        noteNum = x;

        switch (x)
        {
            case -2:
                note = null;
                break;
            case 0:
                note = BitmapFactory.decodeResource(res, R.drawable.sprite0_0);
                break;
            case 1:
                note = BitmapFactory.decodeResource(res, R.drawable.sprite1_0);
                break;
            case 2:
                note = BitmapFactory.decodeResource(res, R.drawable.sprite2_0);
                break;
            case 3:
                note = BitmapFactory.decodeResource(res, R.drawable.sprite3_0);
                break;
            case 4:
                note = BitmapFactory.decodeResource(res, R.drawable.sprite4_0);
                break;
            case 5:
                note = BitmapFactory.decodeResource(res, R.drawable.sprite5_0);
                break;
            case 6:
                note = BitmapFactory.decodeResource(res, R.drawable.sprite6_0);
                break;
            case 7:
                note = BitmapFactory.decodeResource(res, R.drawable.sprite7_0);
                break;
            case 8:
                note = BitmapFactory.decodeResource(res, R.drawable.sprite8_0);
                break;
            case 9:
                note = BitmapFactory.decodeResource(res, R.drawable.sprite9_0);
                break;
        }

        if(note != null)
        {
            box = BitmapFactory.decodeResource(res, R.drawable.spriteboxbase_0);

            width = screenX/11;
            height = screenY/9;

            note = Bitmap.createScaledBitmap(note, width, height, false);
            box = Bitmap.createScaledBitmap(box, width, height, false);
        }

    }

    public Bitmap getNote () {
        if(note != null)
            return overlay(box, note);
        else
            return null;
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    public Bitmap getFadedNote() {
        if(note != null) {
            Bitmap bmp1 = box;
            Bitmap bmp2 = note;
            Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
            Canvas canvas = new Canvas(bmOverlay);

            Paint alphaPaint = new Paint();
            alphaPaint.setColor(0xffffff80); //should be yellow
            alphaPaint.setAlpha(75);

            canvas.drawBitmap(bmp1, new Matrix(), alphaPaint);
            canvas.drawBitmap(bmp2, new Matrix(), alphaPaint);

            return bmOverlay;
        }
        return null;
    }

    public int getNoteNum()
    {
        return noteNum;
    }

}
