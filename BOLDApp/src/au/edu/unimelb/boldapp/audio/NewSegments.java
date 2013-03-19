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

	private HashMap<Pair<Long, Long>, Pair<Long, Long>> segmentMap;
	private UUID respeakingUUID;

	public NewSegments(UUID respeakingUUID) {
		this();
		this.respeakingUUID = respeakingUUID;
	}
	public NewSegments() {
		segmentMap = new HashMap<Pair<Long, Long>, Pair<Long, Long>>();
	}

	public Pair<Long, Long> 
			get(Pair<Long, Long> originalSegment) {
		return segmentMap.get(originalSegment);
	}

	public void put(Pair<Long, Long> originalSegment,
					Pair<Long, Long> respeakingSegment) {
		segmentMap.put(originalSegment, respeakingSegment);
	}

	public void readSegments(File path) throws Exception {
		String mapString = FileIO.read(path);
		String[] lines = mapString.split("\n");
		segmentMap = 
				new HashMap<Pair<Long, Long>, Pair<Long, Long>>();
		for (String line : lines) {
			String[] segmentMatch = line.split(":");
			if (segmentMatch.length != 2) {
				throw new Exception(
						"More than one colon on in a segment mapping line");
			}
			String[] originalSegment = segmentMatch[0].split(",");
			String[] respeakingSegment = segmentMatch[1].split(",");
			segmentMap.put(new Pair<Long, Long>(Long.parseLong(originalSegment[0]),
								Long.parseLong(originalSegment[1])),
					new Pair<Long, Long>(Long.parseLong(respeakingSegment[0])
						, Long.parseLong(respeakingSegment[1])));
		}
	}
}
