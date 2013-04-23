package au.edu.unimelb.aikuma.audio;

import java.io.IOException;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import au.edu.unimelb.aikuma.R;

/**
 * A thing that beeps.
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
		start(context, listener, R.raw.beeps);
	}
	
	private static void start(Context context, final OnCompletionListener listener, int resource) {
		new Beeper(context).create(listener, resource).start();
	}
	
	/** Private methods */
	
	private MediaPlayer getBeep(final OnCompletionListener listener) {
		return create(listener, R.raw.beep);
	}
	
	private MediaPlayer getBeepBeep(final OnCompletionListener listener) {
		return create(listener, R.raw.beeps);
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