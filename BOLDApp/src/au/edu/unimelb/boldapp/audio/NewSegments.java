package au.edu.unimelb.aikuma.audio;

import android.util.Log;
import android.util.Pair;
import au.edu.unimelb.aikuma.FileIO;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;

/**
 * A class to represent the alignment between segments in an original recording
 * and a respeaking.
 */
public class NewSegments {

	private HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> segmentMap;
	private UUID respeakingUUID;

	public NewSegments(UUID respeakingUUID) {
		this.respeakingUUID = respeakingUUID;
	}

	public Pair<Integer, Integer> 
			get(Pair<Integer, Integer> originalSegment) {
		return segmentMap.get(originalSegment);
	}

	public void readSegments(File path) throws Exception {
		String mapString = FileIO.read(path);
		String[] lines = mapString.split("\n");
		segmentMap = 
				new HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();
		for (String line : lines) {
			String[] segmentMatch = line.split(":");
			if (segmentMatch.length != 2) {
				throw new Exception(
						"More than one colon on in a segment mapping line");
			}
			String[] originalSegment = segmentMatch[0].split(",");
			String[] respeakingSegment = segmentMatch[1].split(",");
			for (String s : originalSegment) {
				Log.i("segments", "o: " + s);
			}
			for (String s : respeakingSegment) {
				Log.i("segments", "r: " + s);
			}
			segmentMap.put(new Pair<Integer, Integer>(Integer.parseInt(originalSegment[0]),
								Integer.parseInt(originalSegment[1])),
					new Pair<Integer, Integer>(Integer.parseInt(respeakingSegment[0])
						, Integer.parseInt(respeakingSegment[1])));
		}
	}
}
