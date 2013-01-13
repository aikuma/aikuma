package au.edu.unimelb.boldapp.audio;

import au.edu.unimelb.boldapp.FileIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A class to represent the alignment between segments in an original recording
 * and a respeaking.
 */
public class Segments {
	private List<Integer> originalSegments;
	private List<Integer> respeakingSegments;
	private UUID respeakingUUID;

	public List<Integer> getOriginalSegments() {
		return originalSegments;
	}

	public List<Integer> getRespeakingSegments() {
		return respeakingSegments;
	}

	public Segments(UUID respeakingUUID) throws Exception {
		this.respeakingUUID = respeakingUUID;
		originalSegments = new ArrayList<Integer>();
		respeakingSegments = new ArrayList<Integer>();
		readSegments();
	}

	public void readSegments() throws Exception {
		String mapString = FileIO.read(new File(
				FileIO.getRecordingsPath(), respeakingUUID.toString()));
		String[] lines = mapString.split("\n");
		for (String line : lines) {
			String[] lineSegments = line.split(",");
			if (lineSegments.length == 1) {
				originalSegments.add(Integer.parseInt(lineSegments[0]));
			} else if (lineSegments.length == 2) {
				originalSegments.add(Integer.parseInt(lineSegments[0]));
				respeakingSegments.add(Integer.parseInt(lineSegments[1]));
			} else {
				throw new Exception("More than two segments on a line");
			}
		}
	}
}
