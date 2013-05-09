package org.lp20.aikuma.audio;

import android.media.MediaPlayer;
import java.io.IOException;
import org.lp20.aikuma.model.Recording;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Player {

	/**
	 * Creates a player to play the supplied recording.
	 *
	 * @param	recording	The metadata of the recording to play.
	 */
	public Player(Recording recording) throws IOException {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(recording.getFile().getCanonicalPath());
		mediaPlayer.prepare();
	}

	/** * Starts or resumes playback of the recording. */
	public void play() {
		mediaPlayer.start();
	}

	/** The MediaPlayer used to play the recording. **/
	private MediaPlayer mediaPlayer;
}
