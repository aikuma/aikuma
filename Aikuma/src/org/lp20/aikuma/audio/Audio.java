package org.lp20.aikuma.audio;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

/**
 * An extension of MediaPlayer
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Audio {
	
	public static void reset(Activity activity) {
		playThroughSpeaker(activity);
	}
	
	public static void playThroughEarpiece(Activity activity, boolean toSetMode) {
		AudioManager audioManager = getAudioManager(activity);
		if (toSetMode) {
			audioManager.setMode(AudioManager.MODE_IN_CALL); 
		}
		Log.i("speaker", "setting it off");
		audioManager.setSpeakerphoneOn(false);
		Log.i("speaker", "set it off. isSpeakerphoneOn: " + audioManager.isSpeakerphoneOn());
	}
	
	public static void playThroughSpeaker(Activity activity) {
		AudioManager audioManager = getAudioManager(activity);
		audioManager.setMode(AudioManager.MODE_NORMAL); 
		Log.i("speaker", "setting it on");
		audioManager.setSpeakerphoneOn(true);
		Log.i("speaker", "set it on. isSpeakerphoneOn: " + audioManager.isSpeakerphoneOn());
	}

	protected static AudioManager getAudioManager(Activity activity) {
		return (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
	}

}
