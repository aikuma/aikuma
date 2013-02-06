package au.edu.unimelb.boldapp.audio;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.media.MediaPlayer;
import android.util.Log;

import au.edu.unimelb.boldapp.FileIO;

/**
 * A player that allows individual audio files to be played.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SimplePlayer extends MarkedMediaPlayer 
		implements PlayerInterface {
	/**
	 * Standard constructor
	 *
	 * @param	uuid	The uuid of the recording to be played
	 */
	public SimplePlayer(UUID uuid) {
		super();
		try {
			setDataSource(new File(FileIO.getRecordingsPath(),
					uuid.toString() + ".wav").toString());
			prepare();
			Log.i("threads", "prepared");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor for use when the OnMarkerReachedListener will be
	 * used
	 *
	 * @param	uuid	The uuid of the recording to be played
	 * @param	onMarkerReachedListener	The callback that will be made when
	 * set markers are reached.
	 */
	public SimplePlayer(UUID uuid, MarkedMediaPlayer.OnMarkerReachedListener
			onMarkerReachedListener) {
		super(onMarkerReachedListener);
		try {
			setDataSource(new File(FileIO.getRecordingsPath(),
					uuid.toString() + ".wav").toString());
			prepare();
			Log.i("threads", "prepared");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Returns the sample rate of the file being played.
	 *
	 * @return	The sample rate of the file being played.
	 */
	public int getSampleRate() {
		// For now assume that all files are 44100Hz
		return Constants.SAMPLE_RATE;
	}

	/**
	 * Returns the current sample; a pointer to the current location in the
	 * audio file.
	 *
	 * @return	The current sample.
	 */
	public long getCurrentSample() {
	 	int milliseconds = getCurrentPosition();
		double sample = milliseconds * (getSampleRate() / (float) 1000);
		return (long) sample;
	}

	public int sampleToMsec(long sample) {
		return (int) sample / (getSampleRate() / 1000);
	}


	/**
	 * Rewind the player a number of milliseconds.
	 *
	 * @param	milliseconds	The number of milliseconds to rewind.
	 */
	public void rewind(int milliseconds) {
		int currentPosition = getCurrentPosition();
		int targetPosition = currentPosition - milliseconds;
		if (targetPosition < 0) {
			targetPosition = 0;
		}
		seekTo(targetPosition);
	}
}
