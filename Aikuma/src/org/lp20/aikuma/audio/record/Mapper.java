package org.lp20.aikuma.audio.record;

import android.util.Log;
import java.io.IOException;
import java.io.File;
import java.util.UUID;

import org.lp20.aikuma.audio.Sampler;
import org.lp20.aikuma.audio.Segments;
import org.lp20.aikuma.audio.Segments.Segment;
import org.lp20.aikuma.model.Recording;

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
	
	public Mapper(UUID uuid) {
		this.segments = new Segments();
		this.mappingFile = new File(Recording.getRecordingsPath(), uuid + ".map");
	}
	
	// TODO Does this need to store, also?
	public void stop() {
		try {
			segments.write(mappingFile);
		} catch (IOException e) {
			// Couldn't write mapping. Oh well!
		}
	}
	
	public Long getOriginalStartSample() {
		if (originalStartOfSegment != null) {
			return originalStartOfSegment;
		} else {
			return 0L;
		}
	}
	
	public void markOriginal(Sampler original) {
		// If we have already specified an end of the segment then we're
		// starting a new one. Otherwise just continue with the old
		// originalStartOfSegment
		if (originalEndOfSegment != null) {
			originalStartOfSegment = original.getCurrentSample();
		}
	}
	
	public void markRespeaking(Sampler original, Sampler respoken) {
		originalEndOfSegment = original.getCurrentSample();
		respeakingStartOfSegment = respoken.getCurrentSample();
	}
	
	public void store(Sampler original, Sampler respoken) {
		respeakingEndOfSegment = respoken.getCurrentSample();
		
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
		originalStartOfSegment = original.getCurrentSample();
		respeakingStartOfSegment = respoken.getCurrentSample();
		originalEndOfSegment = null;
		respeakingEndOfSegment = null;
	}
	
}
