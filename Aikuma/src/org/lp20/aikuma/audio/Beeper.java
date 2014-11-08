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
import org.lp20.aikuma2.R;

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
	
	/**
	 * Constructor
	 *
	 * @param	context	The context that the Beeper should beep in.
	 */
	public Beeper(Context context) {
		this.context = context;
	}

	/**
	 * Constructor
	 *
	 * @param	context	The context that the Beeper should beep in.
	 * @param	listener	The callback to play when the beep is complete.
	 */
	public Beeper(Context context, OnCompletionListener listener) {
		this.context = context;
		beepBeep = getBeepBeep(listener);
	}

	/**
	 * Plays one beep.
	 */
	public void beep() { beep(null); }

	/**
	 * Plays one beep.
	 *
	 * @param	listener	The callback to play when the beep is complete.
	 */
	public void beep(OnCompletionListener listener) {
		getBeep(listener).start();
	}

	/**
	 * Plays two beeps in succession.
	 */
	public void beepBeep() { beepBeep.start(); }

	/**
	 * Plays two beeps in succession.
	 *
	 * @param	listener	The callback to play when the beep is complete.
	 */
	public void beepBeep(OnCompletionListener listener) {
		getBeepBeep(listener).start();
	}

	/**
	 * Plays one beep.
	 *
	 * @param	context	The context to play the beep in
	 * @param	listener	The callback to play when the beep is complete.
	 */
	public static void beep(Context context, OnCompletionListener listener) {
		start(context, listener, R.raw.beep);
	}

	/**
	 * Plays two beeps in succession.
	 *
	 * @param	context	The context to play the beep in
	 * @param	listener	The callback to play when the beep is complete.
	 */
	public static void beepBeep(Context context, OnCompletionListener listener) {
		start(context, listener, R.raw.beeps2);
	}

	/**
	 * Plays a long beep.
	 *
	 * @param	context	The context to play the beep in
	 * @param	listener	The callback to play when the beep is complete.
	 */
	public static void longBeep(Context context, OnCompletionListener listener) {
		start(context, listener, R.raw.longbeep);
	}

	// Starts playing the given audio resource.
	private static void start(Context context, final OnCompletionListener listener, int resource) {
		new Beeper(context).create(listener, resource).start();
	}
	
	// Gets a mediaplayer that plays a beep.
	private MediaPlayer getBeep(final OnCompletionListener listener) {
		return create(listener, R.raw.beep);
	}
	
	// Gets a mediaplayer that plays two beeps in succession
	private MediaPlayer getBeepBeep(final OnCompletionListener listener) {
		return create(listener, R.raw.beeps2);
	}
	
	// Creates a mediaplayer to play the beeps with.
	private MediaPlayer create(final OnCompletionListener listener, int resource) {
		final MediaPlayer beeper = MediaPlayer.create(context, resource);
		if (listener != null) {
			beeper.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mediaPlayer) {
					listener.onCompletion(mediaPlayer);
				}
			});
		}
		beeper.setVolume(.10f, .10f);
		return beeper;
	}

}
