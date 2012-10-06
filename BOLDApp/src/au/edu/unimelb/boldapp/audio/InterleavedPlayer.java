package au.edu.unimelb.boldapp.audio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Set;

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
public class InterleavedPlayer {

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
			original = new SimplePlayer(originalUUID);
			respeaking = new SimplePlayer(respeakingUUID);
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
	}

	public void start() {
		original.start();
	}

}
