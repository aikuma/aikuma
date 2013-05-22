package org.lp20.aikuma.ui;

import android.util.Log;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.SeekBar;
import java.util.ArrayList;
import java.util.List;

class InterleavedSeekBar extends SeekBar {

	public InterleavedSeekBar(Context context) {
		super(context);
		lines = new ArrayList<Float>();
	}

	public InterleavedSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public InterleavedSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		Paint paint = new Paint();
		paint.setColor(Color.rgb(142, 196, 0));

		canvas.drawLine(0, 0, 0, 32, paintRect);
		canvas.drawLine(16, 0, 16, 32, paintRect);

		Rect bounds = canvas.getClipBounds();
		canvas.drawLine(bounds.right-16, 0, bounds.right-16, 32, paintRect);

		Log.i("InterleavedSeekBar", "clip bounds: " + canvas.getClipBounds());
	}

	public void addLine(Float x) {
		if (x < 0 || x > 100) {
			throw new IllegalArgumentException(
					"The line location must be a percentage of the seekbar " +
					"between 0 and 100.");
		}
		lines.add(x);
	}

	private List<Float> lines;
}
