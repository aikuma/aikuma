package au.edu.melbuni.boldapp;

import java.io.IOException;

import android.media.MediaPlayer;
import android.util.Log;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;

public class Player extends Sounder {

	private static final String LOG_TAG = "Player";

	private MediaPlayer player = new MediaPlayer();
	private boolean playing = false;

	public void startPlaying(String fileName, final OnCompletionListener listener) {
		if (playing) {
			return;
		}
		playing = true;
		try {
			player.setDataSource(generateFilePath(fileName));
			player.prepare();
			if (listener != null) {
				player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						listener.onCompletion(Player.this);
					}
				});
			}
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
