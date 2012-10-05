package au.edu.unimelb.boldapp.audio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import au.edu.unimelb.boldapp.FileIO;

public class InterleavedPlayer {
	private SimplePlayer original;
	private SimplePlayer respeaking;

	private List<Long> originalSegments;
	private List<Long> respeakingSegments;

	public InterleavedPlayer(UUID respeakingUUID) {
		try {
			JSONParser parser = new JSONParser();
			String jsonStr = FileIO.read(FileIO.getRecordingsPath() +
					respeakingUUID + ".json");
			Object obj = parser.parse(jsonStr);
			JSONObject jsonObj = (JSONObject) obj;
			UUID originalUUID = UUID.fromString(
					jsonObj.get("originalUUID").toString());
			Log.i("yaw", " " + originalUUID);
			original = new SimplePlayer(originalUUID);
			respeaking = new SimplePlayer(respeakingUUID);
		} catch (ParseException e) {
			// Oh noes.
		}

		// If the sample rates aren't the same, do something
		if (original.getSampleRate() != respeaking.getSampleRate()) {
			// What exactly, is yet to be decided.
		}

		originalSegments = new ArrayList<Long>();
		respeakingSegments = new ArrayList<Long>();
		// Reading the samples from the mapping file into a list of segments.
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					FileIO.getAppRootPath() + FileIO.getRecordingsPath() +
					respeakingUUID.toString() + ".map"));
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					String[] lineSplit = line.split(",");
					originalSegments.add(Long.parseLong(lineSplit[0]));
					// If there is a respeaking sample
					if (lineSplit.length == 2) {
						respeakingSegments.add(Long.parseLong(lineSplit[1]));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		originalSegments.add((long)original.getDuration()*44100/1000);
		respeakingSegments.add((long)respeaking.getDuration()*44100/1000);

		Log.i("yaw", " " + originalSegments);
		Log.i("yaw", " " + respeakingSegments);

		// Initialize notification markers
		int msec = originalSegments.get(1).intValue()*1000/44100;
		original.setNotificationMarkerPosition(msec);
		msec = respeakingSegments.get(1).intValue()*1000/44100;
		respeaking.setNotificationMarkerPosition(msec);
		original.setOnMarkerReachedListener(new InterleavedMarkerListener());
		respeaking.setOnMarkerReachedListener(new InterleavedMarkerListener());
	}

	private class InterleavedMarkerListener extends
			MarkedMediaPlayer.OnMarkerReachedListener {

		private int segCount = 1;
		private int count = 0;

		public void onMarkerReached(MarkedMediaPlayer p) {
			Log.i("marking", "mark" + count++);
			// Pause the player
			p.pause();

			segCount++;

			if (p == original) {
				if (segCount >= originalSegments.size()) {
					if (originalSegments.size() == respeakingSegments.size()) {
						respeaking.start();
					}
				} else {
					int msec =
							originalSegments.get(segCount).intValue()*1000/44100;
					p.setNotificationMarkerPosition(msec);
					respeaking.start();
				}
			} else /* p == respeaking */ {
				if (segCount >= respeakingSegments.size()) {
					if (originalSegments.size() > respeakingSegments.size()) {
						original.start();
					}
				} else {
					int msec =
							respeakingSegments.get(segCount).intValue()*1000/44100;
					p.setNotificationMarkerPosition(msec);
					original.start();
				}
			}
		}
	}

	public void start() {
		new Thread(new PlayingStuff()).start();
	}

	private class PlayingStuff implements Runnable {
		public void run() {
			original.start();
		}
	}
}
