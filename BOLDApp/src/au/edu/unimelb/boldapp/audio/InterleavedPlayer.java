package au.edu.unimelb.boldapp.audio;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.media.MediaPlayer;
import android.util.Log;
import android.util.Pair;

import au.edu.unimelb.boldapp.FileIO;
import au.edu.unimelb.boldapp.Recording;

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

	// For unit testing
	public InterleavedPlayer() {
	}

	/**
	 * Will create a broken InterleavedPlayer; only useful for unit testing
	 * methods
	 */
	public InterleavedPlayer(UUID respeakingUUID,
			ArrayList<Integer> originalSegments,
			ArrayList<Integer> respeakingSegments) {

		this.originalSegments = originalSegments;
		this.respeakingSegments = respeakingSegments;


		//new Thread(new Tellme()).start();

		toPlayOriginal = true;
	}

	public InterleavedPlayer(UUID respeakingUUID) {

		this(respeakingUUID, new ArrayList<Integer>(), new
		ArrayList<Integer>());
		initializePlayers(respeakingUUID);
		readSegments(respeakingUUID);

		// Set the first notification markers.

		// If the length of the segments list is only 1, then there was either
		// no mapping file, or nothing was actually recorded for the respective
		// recording. The one element in the list would be the duration as
		// added in readSegments

		if (originalSegments.size() == 1) {
			original.setNotificationMarkerPosition(
					originalSegments.get(0));
			segCount = 1;
		} else {
			original.setNotificationMarkerPosition(
					originalSegments.get(1));
			segCount = 2;
		}

		if (respeakingSegments.size() == 1) {
			respeaking.setNotificationMarkerPosition(
					respeakingSegments.get(0));
		} else {
			respeaking.setNotificationMarkerPosition(
					respeakingSegments.get(1));
		}

	}

	private void initializePlayers(UUID respeakingUUID) {

		try {
			Recording respeakingMeta = FileIO.readRecording(respeakingUUID);
			UUID originalUUID = respeakingMeta.getOriginalUUID();

			original = new SimplePlayer(originalUUID, new
					OriginalMarkerReachedListener());
			respeaking = new SimplePlayer(respeakingUUID, new
					RespeakingMarkerReachedListener());

			// If the sample rates aren't the same, do something
			if (original.getSampleRate() != respeaking.getSampleRate()) {
				// What exactly, is yet to be decided.
			}
		} catch (IOException e) {
			// What would one do? Something bad has happened.
		}

	}

	/* Returns samples innit.
	 */
	private Map<String, List> readSegments(UUID respeakingUUID) {
		Map<String, List> segments = new HashMap<String, List>();
		segments.put("original", new ArrayList<Integer>());
		segments.put("respeaking", new ArrayList<Integer>());

		try {
			String mapString = FileIO.read(new File(
					FileIO.getRecordingsPath(), respeakingUUID.toString()));
			//Log.i("InterleavedPlayerTest", mapString);
			String[] lines = mapString.split("\n");
			for (String line : lines) {
				String[] lineSegments = line.split(",");
				if (lineSegments.length == 1) {
					segments.get("original").add(Integer.parseInt(lineSegments[0]));
				} else if (lineSegments.length == 2) {
					segments.get("original").add(Integer.parseInt(lineSegments[0]));
					segments.get("respeaking").add(Integer.parseInt(lineSegments[1]));
				} 
			}
		} catch (IOException e) {
			//Probably should just leave this as is and return empty lists
		}
		//originalSegments.add(original.getDuration());
		//respeakingSegments.add(respeaking.getDuration());

		return segments;
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
			Log.i("segCount", "segCount before respeaking = " + segCount);
			Log.i("segCount", "respeaking position = " + 
					respeaking.getCurrentPosition());
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
			Log.i("segCount", "segCount before original = " + segCount);
			Log.i("segCount", "original position = " + 
				original.getCurrentPosition());
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
		Result results = calculateOffsets(target);
		// First element denotes whether the original is to play or not.
		segCount = results.segCount;
		toPlayOriginal = results.toPlayOriginal;
		original.seekTo(results.originalSeekTo);
		respeaking.seekTo(results.respeakingSeekTo);

		original.setNotificationMarkerPosition(
				originalSegments.get(segCount));
		respeaking.setNotificationMarkerPosition(
				respeakingSegments.get(segCount));
	}

	/**
	 * Result of a call to calculateOffsets, to be unpacked by seekTo
	 */
	public static class Result {
		public Integer segCount;
		public Boolean toPlayOriginal;
		public Integer originalSeekTo;
		public Integer respeakingSeekTo;
	}

	public Result calculateOffsets(int target) {
		Result result = new Result();
		int total = 0;
		int previous;
		int i;
		for (i = 1; i < originalSegments.size(); i++) {
			previous = total;
			total += (originalSegments.get(i) - originalSegments.get(i-1));
			//i will be segCount
			result.segCount = i;
			if (total > target) {
				result.toPlayOriginal = true;
				result.originalSeekTo = originalSegments.get(i-1) +
						target - previous;
				result.respeakingSeekTo = respeakingSegments.get(i-1);
				return result;
			}
			previous = total;
			if (i >= respeakingSegments.size()) {
				result.toPlayOriginal = false;
				result.originalSeekTo = originalSegments.get(i);
				result.respeakingSeekTo = respeakingSegments.get(i-1);
				return result;
			}
			total += (respeakingSegments.get(i) - respeakingSegments.get(i-1));
			if (total > target) {
				result.toPlayOriginal = false;
				result.originalSeekTo = originalSegments.get(i);
				result.respeakingSeekTo = 
						respeakingSegments.get(i-1) + target - previous;
				return result;
			}
		}
		result.segCount = originalSegments.size();
		result.toPlayOriginal = true;
		result.originalSeekTo = original.getDuration();
		result.respeakingSeekTo = respeaking.getDuration();
		return result;
	}

	public int getCurrentPosition() {
		return original.getCurrentPosition() + respeaking.getCurrentPosition();
	}

	public int getDuration() {
		return original.getDuration() + respeaking.getDuration();
	}

	public void rewind(int msec) {
	}
}
