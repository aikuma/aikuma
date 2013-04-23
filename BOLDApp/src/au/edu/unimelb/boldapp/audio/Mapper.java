package au.edu.unimelb.aikuma.audio;

import android.util.Log;
import au.edu.unimelb.aikuma.audio.NewSegments.Segment;
import java.io.IOException;

public class Mapper {
	
	/** The segment mapping between the original and the respeaking. */
	private NewSegments segments;

	/**
	 * Temporarily store the boundaries of segments before being put in
	 * segments */
	private Long originalStartOfSegment;
	private Long originalEndOfSegment;
	private Long respeakingStartOfSegment;
	private Long respeakingEndOfSegment;
	
	/** The name of the mapping file */
	private String mappingFilename;
	
	public Mapper() {
		this.segments = new NewSegments();
	}
	
	public void prepare(String mappingFilename) {
		this.mappingFilename = mappingFilename;
	}
	
	public void stop() {
		try {
			segments.write(new File(mappingFilename));
		} catch (IOException e) {
			// Couldn't write mapping. Oh well!
		}
	}
	
	public Long getOriginalStartSample() {
		return originalStartOfSegment;
	}
	
	public void markOriginal(Sampler original) {
		// If we have already specified an end of the segment then we're
		// starting a new one. Otherwise just continue with the old
		// originalStartOfSegment
		if (originalStartOfSegment == null) {
			originalStartOfSegment = original.getCurrentSample();
		} else if (originalEndOfSegment != null) {
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
		originalEndOfSegment = null;
		respeakingStartOfSegment = respoken.getCurrentSample();
		respeakingEndOfSegment = null;
	}
	
}