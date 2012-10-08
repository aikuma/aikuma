package au.edu.unimelb.boldapp.audio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Set;

import android.media.MediaPlayer;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import au.edu.unimelb.boldapp.FileIO;

/**
 * Offers functionality to play a respeaking interleaved with the original.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class InterleavedPlayer implements PlayerInterface {

	/**
	 * Counter for which segments to update the notificationMarkerPosition to
	 * as the markers are reached.
	 */
	private int segCount;

	/**
	 * The Player for the original audio.
	 */
	private SimplePlayer original;

	/**
	 * The Player for the respeaking audio.
	 */
	private SimplePlayer respeaking;

	/**
	 * A list of time values in milliseconds that correspond to segments in the
	 * original audio.
	 */
	private List<Integer> originalSegments;

	/**
	 * A list of time values in milliseconds that correspond to segments in the
	 * respeaking audio.
	 */
	private List<Integer> respeakingSegments;

	/**
	 */
	private boolean toPlayOriginal;

	public InterleavedPlayer(UUID respeakingUUID) {
		
		// Initialize the players
		try {
			JSONParser parser = new JSONParser();
			String jsonStr = FileIO.read(FileIO.getRecordingsPath() +
					respeakingUUID + ".json");
			Object obj = parser.parse(jsonStr);
			JSONObject jsonObj = (JSONObject) obj;
			UUID originalUUID = UUID.fromString(
					jsonObj.get("originalUUID").toString());
			original = new SimplePlayer(originalUUID, new
					OriginalMarkerReachedListener());
			respeaking = new SimplePlayer(respeakingUUID, new
					RespeakingMarkerReachedListener());
		} catch (ParseException e) {
			// Cannot interleave the respeaking.
			e.printStackTrace();
		}

		// If the sample rates aren't the same, do something
		if (original.getSampleRate() != respeaking.getSampleRate()) {
			// What exactly, is yet to be decided.
		}

		originalSegments = new ArrayList<Integer>();
		respeakingSegments = new ArrayList<Integer>();
		// Reading the samples from the mapping file into a list of segments.
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					FileIO.getAppRootPath() + FileIO.getRecordingsPath() +
					respeakingUUID.toString() + ".map"));
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					String[] lineSplit = line.split(",");
					originalSegments.add( Integer.parseInt(lineSplit[0]) *
							1000/respeaking.getSampleRate());
					// If there is a respeaking sample
					if (lineSplit.length == 2) {
						respeakingSegments.add( Integer.parseInt(lineSplit[1])
								* 1000/respeaking.getSampleRate());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		originalSegments.add(original.getDuration());
		respeakingSegments.add(respeaking.getDuration());

		Log.i("yaw", " " + originalSegments);
		Log.i("yaw", " " + respeakingSegments);

		//new Thread(new Tellme()).start();

		// Set the first markers.
		segCount = 1;
		original.setNotificationMarkerPosition(
				originalSegments.get(segCount));
		respeaking.setNotificationMarkerPosition(
				respeakingSegments.get(segCount));
		segCount++;

		toPlayOriginal = true;
	}

	public int getSampleRate() {
		return original.getSampleRate();
	}

	public void start() {
		if (toPlayOriginal) {
			original.start();
		} else {
			respeaking.start();
		}
	}

	private class OriginalMarkerReachedListener extends
			MarkedMediaPlayer.OnMarkerReachedListener{
		public void onMarkerReached(MarkedMediaPlayer p) {
			original.pause();
			try {
				original.setNotificationMarkerPosition(
						originalSegments.get(segCount));
			} catch (IndexOutOfBoundsException e) {
				// No more to play
				original.setNotificationMarkerPosition(0);
				return;
			}
			respeaking.start();
		}
	}

	private class RespeakingMarkerReachedListener extends
			MarkedMediaPlayer.OnMarkerReachedListener{
		public void onMarkerReached(MarkedMediaPlayer p) {
			respeaking.pause();
			try {
				respeaking.setNotificationMarkerPosition(
						respeakingSegments.get(segCount));
			} catch (IndexOutOfBoundsException e) {
				// No more to play set the notificationMarkerPosition to be
				// greater than the duration.
				respeaking.setNotificationMarkerPosition(0);
				return;
			}
			segCount++;
			original.start();
		}
	}

	/*
	private class Tellme implements Runnable {
		public void run() {
			while (true) {
				Log.i("playing", " " + original.isPlaying() + " " +
						respeaking.isPlaying());
			}
		}
	}
	*/

	public void release() {
		original.release();
		respeaking.release();
	}

	public void setOnCompletionListener(
			MediaPlayer.OnCompletionListener listener) {
		original.setOnCompletionListener(listener);
		respeaking.setOnCompletionListener(listener);
	}

	public boolean isPlaying() {
		return original.isPlaying() || respeaking.isPlaying();
	}

	public void pause() {
		if (original.isPlaying()) {
			original.pause();
		} else {
			respeaking.pause();
		}
	}

	public void seekTo(int target) {
		Log.i("seek", "target: " + target);
		int total = 0;
		int previous;
		int i;
		for (i = 1; i < originalSegments.size(); i++) {
			previous = total;
			total += (originalSegments.get(i) - originalSegments.get(i-1));
			if (total > target) {
				original.seekTo(originalSegments.get(i-1) + target - previous);
				respeaking.seekTo(respeakingSegments.get(i-1));
				toPlayOriginal = true;
				this.segCount = i;
				Log.i("seek", "playing original at segCount" + i);
				return;
			}
			previous = total;
			total += (respeakingSegments.get(i) - respeakingSegments.get(i-1));
			if (total > target) {
				respeaking.seekTo(
						respeakingSegments.get(i-1) + target - previous);
				toPlayOriginal = false;
				this.segCount = i;
				Log.i("seek", "playing respeaking at segCount" + i);
				return;
			}
		}
		original.seekTo(original.getDuration());
		respeaking.seekTo(respeaking.getDuration());
		this.segCount = i;
	}

	public int getCurrentPosition() {
		return original.getCurrentPosition() + respeaking.getCurrentPosition();
	}

	public int getDuration() {
		return original.getDuration() + respeaking.getDuration();
	}
}
