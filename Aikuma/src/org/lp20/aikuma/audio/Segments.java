/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio;

import android.util.Log;
import android.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.model.Recording;

/**
 * A class to represent the alignment between segments in an original recording
 * and a respeaking.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Segments {

	private LinkedHashMap<Segment, Segment> segmentMap;
	private Segment finalOriginalSegment;
	private UUID respeakingUUID;

	/**
	 * Creates a Segment mapping based on what it finds in the mapping file
	 * corresponding to the supplied UUID.
	 *
	 * @param	respeakingUUID	The UUID of the respeaking whose mapping is to
	 * be read.
	 */
	public Segments(UUID respeakingUUID) {
		this();
		this.respeakingUUID = respeakingUUID;
		try {
			readSegments(new File(
					Recording.getRecordingsPath(), respeakingUUID + ".map"));
		} catch (Exception e) {
			//Get rid of pokemon exception handing.
			//Issue with reading mapping. Maybe throw an exception?
		}
	}

	/**
	 * Constructor; creates an empty Segments.
	 */
	public Segments() {
		segmentMap = new LinkedHashMap<Segment, Segment>();
	}

	/**
	 * Gets the respeaking segment that corresponds to the given original
	 * segment.
	 *
	 * @param	originalSegment	The segment of the original whose corresponding
	 * respeaking segment is required.
	 * @return	The corresponding respeaking segment.
	 */
	public Segment getRespeakingSegment(Segment originalSegment) {
		return segmentMap.get(originalSegment);
	}

	/**
	 * Adds a segment pair to the segments.
	 *
	 * @param	originalSegment	A segment from the original audio source.
	 * @param	respeakingSegment	A segment from the respeaking audio source.
	 */
	public void put(Segment originalSegment,
					Segment respeakingSegment) {
		segmentMap.put(originalSegment, respeakingSegment);
	}

	public Iterator<Segment> getOriginalSegmentIterator() {
		return segmentMap.keySet().iterator();
	}

	/**
	 * Reads the segments from file.
	 *
	 * @param	path	The file to read the segments from.
	 * @throws	IOException	If there is an issue reading the segments from
	 * file.
	 */
	public void readSegments(File path) throws IOException {
		String mapString = FileIO.read(path);
		String[] lines = mapString.split("\n");
		segmentMap = 
				new LinkedHashMap<Segment, Segment>();
		for (String line : lines) {
			String[] segmentMatch = line.split(":");
			if (segmentMatch.length != 2) {
				throw new IOException (
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

	/**
	 * Writes the segments to file.
	 *
	 * @param	path	The file to write the segments to
	 * @throws	IOException	If there is an issue writing the segments.
	 */
	public void write(File path) throws IOException {
		FileIO.write(path, toString());
	}

	/**
	 * Represents a segment.
	 */
	public static class Segment {
		private Pair<Long, Long> pair;

		/**
		 * Constructor; creates a Segment bounded by a start and end sample.
		 *
		 * @param	startSample	The first sample of the segment
		 * @param	endSample	The last sample of the segment
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

		/**
		 * Returns the initial sample of this segment.
		 *
		 * @return	The first sample of the segment.
		 */
		public Long getStartSample() {
			return this.pair.first;
		}

		/**
		 * Returns the final sample of this segment.
		 *
		 * @return	The last sample of the segment.
		 */
		public Long getEndSample() {
			return this.pair.second;
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
}
