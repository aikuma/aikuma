/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.model;

import android.util.Pair;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.lp20.aikuma.model.FileModel.FileType;
import org.lp20.aikuma.util.FileIO;

/**
 * A class to represent the alignment between segments in an original recording
 * and a respeaking.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Segments {

	/**
	 * Creates an object that represents a mapping of recording segments between
	 * the original and the respeaking.
	 *
	 * @param	respeaking	the respeaking in question.
	 */
	public Segments(Recording respeaking) {
		this();
		this.respeaking = respeaking;
		try {
			readJSONSegments(new File(respeaking.getRecordingsPath(), 
					respeaking.getGroupId() + "/" +
					respeaking.getId() + 
					FileModel.getSuffixExt(respeaking.getVersionName(), FileType.MAPPING)));
		} catch (IOException e) {
			// Temporary solution (If file is not in JSON format, try older format)
			try {
				readSegments(new File(respeaking.getRecordingsPath(), 
						respeaking.getGroupId() + "/" +
						respeaking.getId() + "-mapping.txt"));
			} catch (IOException e1) {
				//Issue with reading mapping.
				throw new RuntimeException(e1);
			}
		}
	}

	/**
	 * Constructor to create an empty Segments object.
	 */
	public Segments() {
		segmentMap = new LinkedHashMap<Segment, Segment>();
	}

	/**
	 * Gets the respeaking segment associated with the supplied original
	 * segment.
	 *
	 * @param	originalSegment	A Segment object representing a segment of the original audio.
	 * @return	A respeaking Segment object corresponding to the
	 * originalSegment
	 */
	public Segment getRespeakingSegment(Segment originalSegment) {
		return segmentMap.get(originalSegment);
	}

	/**
	 * Adds a pair of segments to the Segments; analogous to Map.add().
	 *
	 * @param	originalSegment	A segment of an original recording.
	 * @param	respeakingSegment	A respeaking segment corresponding to
	 * originalSegment.
	 */
	public void put(Segment originalSegment,
					Segment respeakingSegment) {
		segmentMap.put(originalSegment, respeakingSegment);
	}
	
	/**
	 * Removes a pair of segments; analogous to Map.remove()
	 * 
	 * @param originalSegment	A segment of an original recording.
	 */
	public void remove(Segment originalSegment) {
		segmentMap.remove(originalSegment);
	}

	/**
	 * Returns an iterator over the segments of the original recording.
	 *
	 * @return	An iterator over the segments of the original recording.
	 */
	public Iterator<Segment> getOriginalSegmentIterator() {
		return segmentMap.keySet().iterator();
	}

	/**
	 * Reads the segments from a file.
	 *
	 * @param	path	The path to the file.
	 * @param	IOException	In case there is an issue reading from the segments
	 * file
	 */
	private void readSegments(File path) throws IOException {
		String mapString = FileIO.read(path);
		String[] lines = mapString.split("\n");
		segmentMap = 
				new LinkedHashMap<Segment, Segment>();
		for (String line : lines) {
			String[] segmentMatch = line.split(":");
			if (segmentMatch.length != 2) {
				throw new RuntimeException(
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
	
	private void readJSONSegments(File path) throws IOException {
		JSONObject jsonObj = FileIO.readJSONObject(path);
		JSONArray jsonSegmentArray = (JSONArray) jsonObj.get(JSON_MAPPING_KEY);
		segmentMap = new LinkedHashMap<Segment, Segment>();
		
		for (Object jsonElem : jsonSegmentArray) {
			JSONObject jsonSegmentPair = (JSONObject) jsonElem;
			
			JSONArray jsonSourceSegment = 
					(JSONArray) jsonSegmentPair.get(JSON_ORIGINAL_KEY);
			JSONArray jsonTargetSegment = 
					(JSONArray) jsonSegmentPair.get(JSON_RESPEAKING_KEY);
			
			if (jsonSourceSegment.size() + jsonTargetSegment.size() != 4) {
				throw new RuntimeException("Segment format is corrupted");
			}
			
			segmentMap.put(
					new Segment((Long) jsonSourceSegment.get(0), 
							(Long) jsonSourceSegment.get(1)),
					new Segment((Long) jsonTargetSegment.get(0), 
							(Long) jsonTargetSegment.get(1)));
			
		}
	}

	/**
	 * Writes the segment mapping to file.
	 *
	 * @param	path	The path to the file.
	 * @throws	IOException	If the segments cannot be written to file.
	 */
	public void write(File path) throws IOException {
		//FileIO.write(path, toString());
		FileIO.writeJSONObject(path, toJSONObject());
	}

	/**
	 * Represents a segment.
	 */
	public static class Segment {
		private Pair<Long, Long> pair;

		/**
		 * Creates a segment given the sample at which the segment starts, and
		 * the sample at which it ends.
		 *
		 * @param	startSample	The sample at which the segment starts.
		 * @param	endSample	The sample at which the segment ends.
		 */
		public Segment(Long startSample, Long endSample) {
			if (startSample == null) {
				throw new IllegalArgumentException("Null start of sample");
			}
			if (endSample == null) {
				throw new IllegalArgumentException("Null end of sample");
			}
			this.pair = new Pair<Long, Long>(startSample, endSample);
		}

		public Long getStartSample() {
			return this.pair.first;
		}

		public Long getEndSample() {
			return this.pair.second;
		}

		/**
		 * Returns the duration of the segment in samples
		 *
		 * @return	The duration of the segment in samples.
		 */
		public Long getDuration() {
			return this.pair.second - this.pair.first;
		}

		@Override
		public String toString() {
			return getStartSample() + "," + getEndSample();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) { return false; }
			if (obj == this) { return true; }
			if (obj.getClass() != getClass()) {
				return false;
			}
			Segment rhs = (Segment) obj;
			return new EqualsBuilder()
					.append(getStartSample(), rhs.getStartSample())
					.append(getEndSample(), rhs.getEndSample())
					.isEquals();
		}

		@Override
		public int hashCode() {
			return pair.hashCode();
		}
	}

	@Override
	public String toString() {
		String mapString = new String();
		Segment respeakingSegment;
		for (Segment originalSegment : segmentMap.keySet()) {
			respeakingSegment = getRespeakingSegment(originalSegment);
			mapString +=
					originalSegment.getStartSample() + "," +
					originalSegment.getEndSample() + ":" 
					+ respeakingSegment.getStartSample() + "," +
					respeakingSegment.getEndSample() + "\n";
		}
		return mapString;
	}
	
	/**
	 * Get JSON representation of the segments structure
	 * 
	 * @return	JSON object of the segments
	 */
	private JSONObject toJSONObject() {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonSegmentArray = new JSONArray();
		Segment respeakingSegment;
		for (Segment originalSegment : segmentMap.keySet()) {
			respeakingSegment = getRespeakingSegment(originalSegment);
			
			JSONObject jsonSegmentPair = new JSONObject();
			JSONArray jsonSourceSegment = new JSONArray();
			JSONArray jsonTargetSegment = new JSONArray();
			
			jsonSourceSegment.add(originalSegment.getStartSample());
			jsonSourceSegment.add(originalSegment.getEndSample());
			jsonTargetSegment.add(respeakingSegment.getStartSample());
			jsonTargetSegment.add(respeakingSegment.getEndSample());
			
			jsonSegmentPair.put(JSON_ORIGINAL_KEY, jsonSourceSegment);
			jsonSegmentPair.put(JSON_RESPEAKING_KEY, jsonTargetSegment);
			
			jsonSegmentArray.add(jsonSegmentPair);
		}
		
		jsonObj.put(JSON_MAPPING_KEY, jsonSegmentArray);
		
		return jsonObj;
	}

	/**
	 * An exception thrown if the segment mapping file is incorrectly
	 * formatted.
	 */
	public static class SegmentException extends Exception {
		/**
		 * Constructor
		 *
		 * @param	message	The message to be contained by the exception
		 */
		public SegmentException(String message) {
			super(message);
		}
	}

	private LinkedHashMap<Segment, Segment> segmentMap;
	private Segment finalOriginalSegment;
	private Recording respeaking;
	
	private static final String JSON_MAPPING_KEY = "mapping";
	private static final String JSON_ORIGINAL_KEY = "source";
	private static final String JSON_RESPEAKING_KEY = "target";
}
