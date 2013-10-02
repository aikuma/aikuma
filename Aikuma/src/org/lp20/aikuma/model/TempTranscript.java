package org.lp20.aikuma.model;

import java.util.LinkedHashMap;
import java.util.Iterator;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;
import org.lp20.aikuma.model.Recording;

public class Transcript {
	private LinkedHashMap<Segment, String> transcriptMap;

	public Transcript(Recording original) {
		transcriptMap = new LinkedHashMap<Segment, String>();
		//readTranscript(new File(Recording.getRecordingsPath(),
		//		original.getUUID() + ".trans"));
		genDummyMap();
	}

	private void genDummyMap() {
		transcriptMap.put(new Segment(0l, 49264l), "first shhh");
		transcriptMap.put(new Segment(49264l, 109728l), "second shhh");
		transcriptMap.put(new Segment(109728l, 142496l), "third shhh");
	}

	public Iterator<Segment> getSegmentIterator() {
		return transcriptMap.keySet().iterator();
	}

	public String getTranscriptSegment(Segment segment) {
		return transcriptMap.get(segment);
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
