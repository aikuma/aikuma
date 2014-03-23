/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record;

import android.util.Log;
import java.io.IOException;
import java.io.File;
import java.util.UUID;

import org.lp20.aikuma.audio.Sampler;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;
import org.lp20.aikuma.model.Recording;

/**
 * Facilitates creation of segment mappings (Segments). To be used when
 * recording a respeaking.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Mapper {
	
	/** The segment mapping between the original and the respeaking. */
	private Segments segments;

	/**
	 * Temporarily store the boundaries of segments before being put in
	 * segments */
	private Long originalStartOfSegment = 0L;
	private Long originalEndOfSegment;
	private Long respeakingStartOfSegment = 0L;
	private Long respeakingEndOfSegment;

	/** The mapping file */
	private File mappingFile;

	/**
	 * Constructor
	 *
	 * @param	uuid	The UUID of the respeaking.
	 */
	public Mapper(UUID uuid) {
		this.segments = new Segments();
		this.mappingFile = new File(Recording.getNoSyncRecordingsPath(), uuid + ".map");
	}


	/**
	 * Stops and writes the segments to file.
	 *
	 * @throws	IOException	If the segments couldn't be written.
	 */
	public void stop() throws IOException {
		segments.write(mappingFile);
	}

	/**
	 * Gets the first sample of the original segment.
	 *
	 * @return	The start of the original segment; or 0L if there is none.
	 */
	public Long getOriginalStartSample() {
		if (originalStartOfSegment != null) {
			return originalStartOfSegment;
		} else {
			return 0L;
		}
	}

	/**
	 * Marks the start of an original segment.
	 *
	 * @param	original	The source of the original segments.
	 */
	public void markOriginal(Sampler original) {
		// If we have already specified an end of the segment then we're
		// starting a new one. Otherwise just continue with the old
		// originalStartOfSegment
		if (originalEndOfSegment != null) {
			originalStartOfSegment = original.getCurrentSample();
		}
	}

	/**
	 * Marks the end of an original segment and the start of a respeaking segment.
	 *
	 * @param	original	The source of the original segments.
	 * @param	respoken	The source of the respoken segments.
	 */
	public void markRespeaking(Sampler original, Sampler respoken) {
		originalEndOfSegment = original.getCurrentSample();
		respeakingStartOfSegment = respoken.getCurrentSample();
	}
	
	/**
	 * Stores a segment determined by the samplers current locations.
	 *
	 * A segment may not be stored if there hasn't been an end to the current
	 * original segment.
	 *
	 * @param	original	The source of the original segments.
	 * @param	respoken	The source of the respoken segments.
	 * @return	Returns true if a segment gets stored; false otherwise.
	 */
	public boolean store(Sampler original, Sampler respoken) {
		//If we're not respeaking and still playing an original segment, do
		//nothing
		if (originalEndOfSegment == null) {
			return false;
		}
		//Otherwise lets end this respeaking segment
		respeakingEndOfSegment = respoken.getCurrentSample();
		//And store these two segments
		Segment originalSegment;
		try {
			originalSegment = new Segment(originalStartOfSegment,
					originalEndOfSegment);
		} catch (IllegalArgumentException e) {
			// This could only have happened if no original had been recorded at all.
			originalSegment = new Segment(0l, 0l);
		}
		Segment respeakingSegment = new Segment(respeakingStartOfSegment,
				respeakingEndOfSegment);
		segments.put(originalSegment, respeakingSegment);
		//Now we say we're marking the start of the new original and respekaing
		//segments
		originalStartOfSegment = original.getCurrentSample();
		respeakingStartOfSegment = respoken.getCurrentSample();
		//We currently have no end for these segments.
		originalEndOfSegment = null;
		respeakingEndOfSegment = null;
		return true;
	}
	
}
