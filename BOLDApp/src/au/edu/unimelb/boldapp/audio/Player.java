package au.edu.unimelb.boldapp.audio;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;

public class Player {

	public MediaPlayer player = new MediaPlayer();
	private boolean playing = false;

	public boolean isPlaying() {
		return playing;
	}
  
  /** Prepare playing with the given file. */
  public void prepare(String fileName) {
		try {
			player.setDataSource(generateFilePath(fileName));
		} catch (IOException e) {
		}
  }
  
  /** Start playing. */
	public void play() {
		if (playing) {
			return;
		}
		playing = true;
		try {
			player.prepare();
			player.start();
    } catch (IOException e) {
      
		}
	}

  /** Pause the player. */
	public void pause() {
		player.pause();
	}

  /** Rewind the player a number of miliseconds. */
	public void rewind(int miliseconds) {
		int currentPosition = player.getCurrentPosition();
		int targetPosition = currentPosition - miliseconds;
		if (targetPosition < 0) {
			targetPosition = 0;
		}
		player.seekTo(targetPosition);
	}
  
  /** Resume the player. */
	public void resume() {
		player.start();
	}

  /** Stop the player. */
	public void stop() {
		if (!playing) {
			return;
		}
		player.reset(); // TODO We might need to set the data source again.
		playing = false;
	}
  
  /** Generates all the necessary directories for the file. */
	protected String generateFilePath(String fileName) {
		File file = new File(fileName);
		File parentFile = new File(file.getParent());
		parentFile.mkdirs();
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
