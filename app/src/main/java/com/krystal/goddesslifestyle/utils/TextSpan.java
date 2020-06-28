package com.krystal.goddesslifestyle.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

/**
 * Created by Bhargav Thanki on 20 February,2020.
 */
public class TextSpan implements LineBackgroundSpan  {
    /**
     * Default radius used
     */

    private final String text;


    public TextSpan(String text) {
        this.text = text;
    }



    @Override
    public void drawBackground(
            Canvas canvas, Paint paint,
            int left, int right, int top, int baseline, int bottom,
            CharSequence charSequence,
            int start, int end, int lineNum
    ) {
        /*int oldColor = paint.getColor();
        if (color != 0) {
            paint.setColor(color);
        }
        canvas.drawCircle((left + right) / 2, bottom + radius, radius, paint);
        paint.setColor(oldColor);*/

        //paint.setTextSize(16);
        float textSize = paint.getTextSize();
        paint.setTextSize(24);
        canvas.drawText(text, left, bottom, paint);
        paint.setTextSize(textSize);
    }
}
