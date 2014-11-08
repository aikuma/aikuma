/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.util.Log;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;
import org.lp20.aikuma2.R;
import java.util.ArrayList;
import java.util.List;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
class InterleavedSeekBar extends SeekBar {

	public InterleavedSeekBar(Context context) {
		super(context);
		//this.getThumb().setColorFilter(0xff4c516d, Mode.SRC_IN);
		Drawable thumb = context.getResources().
				getDrawable(R.drawable.thumb);
		Drawable progressColor = context.getResources().
				getDrawable(R.drawable.seekbar_background);
		this.setThumb(thumb);
		this.setProgressDrawable(progressColor);
	}

	public InterleavedSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		Drawable thumb = context.getResources().
				getDrawable(R.drawable.thumb);
		Drawable progressColor = context.getResources().
				getDrawable(R.drawable.seekbar_background);
		this.setThumb(thumb);
		this.setProgressDrawable(progressColor);
	}

	public InterleavedSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Drawable thumb = context.getResources().
				getDrawable(R.drawable.thumb);
		Drawable progressColor = context.getResources().
				getDrawable(R.drawable.seekbar_background);
		this.setThumb(thumb);
		this.setProgressDrawable(progressColor);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setColor(Color.rgb(196, 0, 1));

		drawLines(canvas, paint);
	}

	// Draws all the lines on the given canvas with the given paint.
	private void drawLines(Canvas canvas, Paint paint) {
		for(Float line : lines) {
			drawLine(line, canvas, paint);
		}
	}

	// Draws a line on the canvas with the given paint
	private void drawLine(Float line, Canvas canvas, Paint paint) {
		Rect bounds = canvas.getClipBounds();
		int barWidth = (bounds.right-16) - (bounds.left+16);
		float pixel = line * ((float) barWidth / 100.0f);

		canvas.drawLine(pixel+16, 0f, pixel+16, 32f, paint);
	}

	/**
	 * Adds a line at the specified point in the seek bar.
	 *
	 * @param	x	The place to put the line, as a percentage of the seekbar.
	 */
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
