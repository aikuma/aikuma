/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

/**
 * Provides methods to change which of the phone's speakers the audio is played
 * through.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Audio {

	/**
	 * Resets the activity playing through the main speakers.
	 *
	 * @param	activity	The activity in question.
	 */
	public static void reset(Activity activity) {
		playThroughSpeaker(activity);
	}

	/**
	 * Plays the audio through the earpiece, like a phone call.
	 *
	 * @param	activity	The activity that this method governs.
	 * @param	toSetMode	True if the Audiomanager needs to be set to call mode.
	 */
	public static void playThroughEarpiece(Activity activity, boolean toSetMode) {
		AudioManager audioManager = getAudioManager(activity);
		if (toSetMode) {
			audioManager.setMode(AudioManager.MODE_IN_CALL); 
		}
		audioManager.setSpeakerphoneOn(false);
	}

	/**
	 * Plays the audio through the main speaker (not the earpiece as a phone
	 * call would)
	 *
	 * @param	activity	The activity that this method governs.
	 */
	public static void playThroughSpeaker(Activity activity) {
		AudioManager audioManager = getAudioManager(activity);
		audioManager.setMode(AudioManager.MODE_NORMAL); 
		audioManager.setSpeakerphoneOn(true);
	}

	/**
	 * Gets the audio manager for the given activity.
	 *
	 * @param	activity	The activity whose AudioManager is needed.
	 * @return	The audiomanager.
	 */
	protected static AudioManager getAudioManager(Activity activity) {
		return (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
	}

}
