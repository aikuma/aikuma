package au.edu.unimelb.aikuma.audio;

import android.util.Log;
import android.util.Pair;
import au.edu.unimelb.aikuma.FileIO;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * A class to represent the alignment between segments in an original recording
 * and a respeaking.
 */
public class NewSegments {

	public LinkedHashMap<Segment, Segment> segmentMap;
	private UUID respeakingUUID;

	public NewSegments(UUID respeakingUUID) {
		this();
		this.respeakingUUID = respeakingUUID;
		try {
			readSegments(new File(
					FileIO.getRecordingsPath(), respeakingUUID + ".map"));
		} catch (Exception e) {
			Log.i("segments", "caught exception");
			//Issue with reading mapping. Maybe throw an exception?
		}
	}
	public NewSegments() {
		segmentMap = new LinkedHashMap<Segment, Segment>();
	}

	public Segment getRespeakingSegment(Segment originalSegment) {
		return segmentMap.get(originalSegment);
	}

	public void put(Segment originalSegment,
					Segment respeakingSegment) {
		segmentMap.put(originalSegment, respeakingSegment);
	}

	public Iterator<Segment> getOriginalSegmentIterator() {
		return segmentMap.keySet().iterator();
	}

	public void readSegments(File path) throws Exception {
		String mapString = FileIO.read(path);
		String[] lines = mapString.split("\n");
		segmentMap = 
				new LinkedHashMap<Segment, Segment>();
		for (String line : lines) {
			String[] segmentMatch = line.split(":");
			if (segmentMatch.length != 2) {
				throw new Exception(
						"There must be just one colon on in a segment mapping line");
			}
			String[] originalSegment = segmentMatch[0].split(",");
			String[] respeakingSegment = segmentMatch[1].split(",");
			segmentMap.put(new Segment(Long.parseLong(originalSegment[0]),
								Long.parseLong(originalSegment[1])),
					new Segment(Long.parseLong(respeakingSegment[0])
						, Long.parseLong(respeakingSegment[1])));
		}
	}

	public void write(File path) throws IOException {
		String mapString = new String();
		Segment respeakingSegment;
		for (Segment originalSegment : segmentMap.keySet()) {
			respeakingSegment = segmentMap.get(originalSegment);
			mapString +=
					originalSegment.getStartSample() + "," +
					originalSegment.getEndSample() + ":" 
					+ respeakingSegment.getStartSample() + "," +
					respeakingSegment.getEndSample() + "\n";
		}
		FileIO.write(path, mapString);
		Log.i("segments", "path: " + path + "mapstring: " + mapString);
	}

	/**
	 * Represents a segment.
	 */
	public static class Segment {
		private Pair<Long, Long> pair;

		public Segment(Long startSample, Long endSample) {
			this.pair = new Pair<Long, Long>(startSample, endSample);
		}

		public Long getStartSample() {
			return this.pair.first;
		}

		public Long getEndSample() {
			return this.pair.second;
		}
	}
}
