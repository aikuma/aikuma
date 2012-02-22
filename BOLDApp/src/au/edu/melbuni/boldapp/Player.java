package au.edu.melbuni.boldapp;

import java.io.IOException;

import android.media.MediaPlayer;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;

public class Player extends Sounder {

	private MediaPlayer player = new MediaPlayer();
	private boolean playing = false;

	float leftVolume;
	float rightVolume;
	int currentRampUp;
	Thread rampUpThread;

	public boolean isPlaying() {
		return playing;
	}

	public void startPlaying(String fileName,
			final OnCompletionListener listener) {
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
			LogWriter.log(e.getMessage());
			if (listener != null) {
				listener.onCompletion(this);
			}
		}
	}

	public void pause() {
		player.pause();
	}

	public void rewind(int miliseconds) {
		int currentPosition = player.getCurrentPosition();
		int targetPosition = currentPosition - miliseconds;
		if (targetPosition < 0) {
			targetPosition = 0;
		}
		player.seekTo(targetPosition);
	}

	public void resume() {
		player.start();
	}

	public void stopPlaying() {
		if (!playing) {
			return;
		}
		player.reset();
		playing = false;
	}

	public void stopRampUpThread() {
		if (rampUpThread.isAlive()) {
			rampUpThread.stop();
		}
	}

	public void rampUp(int miliseconds) {
		leftVolume = 0f;
		rightVolume = 0f;
		currentRampUp = miliseconds / 10;

		final float target = 255f;

		rampUpThread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (currentRampUp-- < 0) {
					leftVolume = 1;
					rightVolume = 1;
					stopRampUpThread();
				}
				;
				LogWriter.log("Current volume: " + leftVolume);
				player.setVolume(leftVolume, rightVolume);
				leftVolume = (target - leftVolume) / 2;
				rightVolume = (target - rightVolume) / 2;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		rampUpThread.start();
	}
}
