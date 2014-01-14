/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio;


import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import java.io.IOException;
import org.lp20.aikuma.R;

/**
 * Provides methods to make beeping noises for when recording is started and
 * stopped.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Beeper {
	
	private Context context;
	private MediaPlayer beep;
	private MediaPlayer beepBeep;
	
	public Beeper(Context context) {
		this.context = context;
	}

	public void beep() { beep(null); }
	public void beep(OnCompletionListener listener) {
		getBeep(listener).start();
	}
	
	public void beepBeep() { beepBeep(null); }
	public void beepBeep(OnCompletionListener listener) {
		getBeepBeep(listener).start();
	}
	
	/** Static convenience methods */
	
	public static void beep(Context context, OnCompletionListener listener) {
		start(context, listener, R.raw.beep);
	}

	
	public static void beepBeep(Context context, OnCompletionListener listener) {
		start(context, listener, R.raw.beeps2);
	}

	public static void longBeep(Context context, OnCompletionListener listener) {
		start(context, listener, R.raw.longbeep);
	}
	
	private static void start(Context context, final OnCompletionListener listener, int resource) {
		new Beeper(context).create(listener, resource).start();
	}
	
	/** Private methods */
	
	private MediaPlayer getBeep(final OnCompletionListener listener) {
		return create(listener, R.raw.beep);
	}
	
	private MediaPlayer getBeepBeep(final OnCompletionListener listener) {
		Log.i("beep", "beepin");
		return create(listener, R.raw.beeps2);
	}
	
	private MediaPlayer create(final OnCompletionListener listener, int resource) {
		final MediaPlayer beeper = MediaPlayer.create(context, resource);
		if (listener != null) {
			beeper.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mediaPlayer) {
					listener.onCompletion(mediaPlayer);
					beeper.release();
				}
			});
		}
		beeper.setVolume(.10f, .10f);
		return beeper;
	}

}
