package org.lp20.aikuma.model;

import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.util.FileIO;

/**
 * Represents a transcript of a recording
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class TempTranscript {
	private static final String TAG = "TempTranscript";
	private LinkedHashMap<Segment, TranscriptPair> transcriptMap;
	// The recording the transcript is of. Needed to obtain the sample rate.
	private Recording recording;


	/**
	 * Constructor for an empty transcript.
	 */
	public TempTranscript() {
		transcriptMap = new LinkedHashMap<Segment, TranscriptPair>();
	}

	/**
	 * Constructor
	 *
	 * @param	recording	The recording this is a transcript of
	 * @param	transcriptFile	The file the transcript data is contained in.
	 * @throws	IOException	If there is an issue reading the transcript file.
	 */
	public TempTranscript(Recording recording, File transcriptFile)
			throws IOException {
		this.recording = recording;
		transcriptMap = new LinkedHashMap<Segment, TranscriptPair>();
		parseFile(transcriptFile);
	}

	/**
	 * Parses the segments from the given file into the mapping.
	 *
	 * @param	transcriptFile	The file containing the transcript
	 */
	private void parseFile(File transcriptFile) throws IOException {
		boolean firstSegment = true;
		String data = FileIO.read(transcriptFile);
		String[] lines = data.split("\n");
		String[] splitLine;
		for (String line : lines) {
			if (!line.startsWith(";;")) {
				splitLine = line.split("\t");
				// If it's the first segment, put a blank segment between 0l
				// samples and the start of the first segment.
				if (firstSegment) {
					firstSegment = false;
					transcriptMap.put(new Segment(0l,
							secondsToSample(Float.parseFloat(splitLine[0]))),
							new TranscriptPair("",""));
				}
				Log.i(TAG, "start sample: " +
						secondsToSample(Float.parseFloat(splitLine[0])));
				Log.i(TAG, "end sample: " +
						secondsToSample(Float.parseFloat(splitLine[1])));
				Log.i(TAG, "text: " + splitLine[3]);
				if (splitLine.length > 4) {
					transcriptMap.put(new Segment(
							secondsToSample(Float.parseFloat(splitLine[0])),
							secondsToSample(Float.parseFloat(splitLine[1]))),
							new TranscriptPair(splitLine[3], splitLine[4]));
				} else {
					transcriptMap.put(new Segment(
							secondsToSample(Float.parseFloat(splitLine[0])),
							secondsToSample(Float.parseFloat(splitLine[1]))),
							new TranscriptPair(splitLine[3], ""));
				}
			}
		}
	}

	/**
	 * Converts seconds to samples given the recordings sample rate.
	 */
	private long secondsToSample(float seconds) {
		return (long) (seconds * recording.getSampleRate());
	}

	/*
	private void genDummyMap() {
		transcriptMap.put(new Segment(0l, 49264l), "first shhh");
		transcriptMap.put(new Segment(49264l, 109728l), "second shhh");
		transcriptMap.put(new Segment(109728l, 142496l), "third shhh");
	}
	*/

	/*
	private Iterator<Segment> getSegmentIterator() {
		return transcriptMap.keySet().iterator();
	}
	*/

	public List<Segment> getSegmentList() {
		return new ArrayList<Segment>(transcriptMap.keySet());
	}

	/**
	 * Gets the TranscriptPair that corresponds to a given segment
	 *
	 * @param	segment	The segment to obtain a transcription of
	 * @return	A TranscriptPair that contains a transcript and a translation.
	 */
	public TranscriptPair getTranscriptPair(Segment segment) {
		return transcriptMap.get(segment);
	}

	/** 
	 * Gets the segment that this sample is in. If there is no segment in
	 * the sample, we should grab the first segment.
	 *
	 * @param	sample	The sample whose corresponding segment is required.
	 * @return	The segment corresponding to the input sample; null if not
	 * present
	 */
	public Segment getSegmentOfSample(long sample) {
		for (Segment segment : getSegmentList()) {
			if (sample >= segment.getStartSample() &&
				sample <= segment.getEndSample()) {
				return segment;
			}
		}
		return getSegmentList().get(0);
	}

	/**
	 * Represents the text associated with a segment of the transcription,
	 * which includes a transcript and a translation.
	 */
	public class TranscriptPair {
		/** A segment of the transcript */
		public String transcript;
		/** A segment of the translation */
		public String translation;

		/**
		 * Constructor
		 *
		 * @param	transcript	The transcript segment
		 * @param	translation	The translation segment
		 */
		public TranscriptPair(String transcript, String translation) {
			this.transcript = transcript;
			this.translation = translation;
		}
	}

	/*
	public void readSegments(File path) {
		List<String> translation = new ArrayList<String>();
		try {
			CSVReader reader = new CSVReader(new FileReader(transFile), '\t');
			String[] nextLine;
			try {
				//Skip the header.
				reader.readNext();
				reader.readNext();
				while ((nextLine = reader.readNext()) != null) {
					Log.i("transcription", nextLine[0] + " " + nextLine[3]);
					translation.add(nextLine[4]);
				}
			} catch (IOException e) {
				//Translation doesn't exist maybe?
			}
		} catch (FileNotFoundException e) {
			//File couldn't be found. We just get an empty list.
		}
		Log.i("transcription", " " + translation);
		return translation;
	}
	*/

	/**
	 * Returns a list of Strings corresponding to the segment-by-segment
	 * translation of the recording.
	 */
	/*
	public List<String> getTranslation() {
		List<Recording> respeakings = getRespeakings();
		File transFile;
		for (Recording respeaking : respeakings) {
			transFile = new File(getRecordingsPath(),
					respeaking.getUUID() + ".trans");
			if (transFile.exists()) {
				return readTranslation(transFile);
			}
		}
		return null;
	}
	*/

}
