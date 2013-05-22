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
		Log.i("InterleavedSeekBar", "interleaved seek bar constructor 1");
	}

	public InterleavedSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.e("InterleavedSeekBar", "stack yo", new Exception());
		Log.i("InterleavedSeekBar", "interleaved seek bar constructor 2");
	}

	public InterleavedSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.i("InterleavedSeekBar", "interleaved seek bar constructor 3");
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.e("InterleavedSeekBar", "draw yo", new Exception());

		Paint paint = new Paint();
		paint.setColor(Color.rgb(142, 196, 0));

		//lines = new ArrayList<Float>();
		addLine(0.0f);
		addLine(50.0f);
		addLine(100.0f);
		drawLines(canvas, paint);
	}

	private void drawLines(Canvas canvas, Paint paint) {
		for(Float line : lines) {
			drawLine(line, canvas, paint);
		}
	}

	private void drawLine(Float line, Canvas canvas, Paint paint) {
		Rect bounds = canvas.getClipBounds();
		int barWidth = (bounds.right-16) - (bounds.left+16);
		float pixel = line * ((float) barWidth / 100.0f);
		canvas.drawLine(pixel+16, 0f, pixel+16, 32f, paint);
	}

	public void addLine(Float x) {
		if (x < 0 || x > 100) {
			throw new IllegalArgumentException(
					"The line location must be a percentage of the seekbar " +
					"between 0 and 100.");
		}
		lines.add(x);
	}

	public void setLines(List<Float> lines) {
		this.lines = lines;
	}

	private List<Float> lines = new ArrayList<Float>();
}
