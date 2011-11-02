package au.edu.melbuni.boldapp;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

public class Player extends Sounder {

	private static final String LOG_TAG = "Player";

	private MediaPlayer player = new MediaPlayer();
	private boolean playing = false;

	public void startPlaying(String fileName, OnCompletionListener listener) {
		if (playing) {
			return;
		}
		playing = true;
		try {
			player.setDataSource(generateFullFilename(fileName));
			player.prepare();
			if (listener != null) { player.setOnCompletionListener(listener); }
			player.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "#prepare() failed");
		}
	}

	public void stopPlaying() {
		if (!playing) {
			return;
		}
		player.reset();
		playing = false;
	}

//	public void pause() {
//		if (player != null) {
//			player.release();
//			player = null;
//		}
//	}
}
