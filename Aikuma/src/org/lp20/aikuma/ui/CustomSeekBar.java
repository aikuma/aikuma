package org.lp20.aikuma.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.SeekBar;

class CustomSeekBar extends SeekBar {

public CustomSeekBar(Context context) {
    super(context);
}

public CustomSeekBar(Context context, AttributeSet attrs) {
    super(context, attrs);
}

public CustomSeekBar(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
}

protected void onDraw(Canvas canvas) {

    super.onDraw(canvas);

    Paint paintRect = new Paint();
    paintRect.setColor(Color.rgb(142, 196, 0));

    Rect audioRect = new Rect();
    audioRect.set(0, 0, 10, 100);

    canvas.drawRect(audioRect, paintRect);
}
}
