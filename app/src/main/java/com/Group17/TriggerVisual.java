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


public class TriggerVisual { //this is not the hitbox, make that its own thing


    public int x, y;
    Bitmap topArrow;
    Bitmap bottomArrow;
    Bitmap line;
    Bitmap target;

    public TriggerVisual (Resources res, int width, int height) {

        topArrow = BitmapFactory.decodeResource(res, R.drawable.sprite_indicator_arrow_top_0);
        topArrow = Bitmap.createScaledBitmap(topArrow, width, (int) (height * 0.1), false);
        bottomArrow = BitmapFactory.decodeResource(res, R.drawable.sprite_indicator_arrow_bottom_0);
        bottomArrow = Bitmap.createScaledBitmap(bottomArrow, width, (int) (height * 0.1), false);
        line = BitmapFactory.decodeResource(res, R.drawable.sprite_indicator_red_line_0); //fix this
        line = Bitmap.createScaledBitmap(line, line.getWidth(), (int) (height * 0.8), false);

        target = Bitmap.createBitmap(width, height, topArrow.getConfig());
        Canvas canvas = new Canvas(target);
        canvas.drawBitmap(topArrow, 0, 0, null);
        canvas.drawBitmap(bottomArrow, 0, line.getHeight() + topArrow.getHeight(), null);
        canvas.drawBitmap(line, (int)((topArrow.getWidth() /2.0) - line.getWidth()/2.0) ,topArrow.getHeight(), null);

        this.x = 0;
        this.y = 0;

    }

    public Bitmap getBitmap(){return target;}

}
