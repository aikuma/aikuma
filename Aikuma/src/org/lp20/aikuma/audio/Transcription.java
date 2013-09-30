public class Transcription {
	private LinkedHashMap<Segment, String> transcriptionMap;

	public Transcription(Recording original) {
		transcriptionMap = new LinkedHashMap<Segment, String>();
		readTranscription(new File(Recording.getRecordingsPath(),
				original.getUUID() + ".trans"));
		genDummyMap();
	}

	private void genDummyMap() {
		transcriptionMap.add(new Segment(0l, 49264l), "first shhh");
		transcriptionMap.add(new Segment(49264l, 109728l), "second shhh");
		transcriptionMap.add(new Segment(109728l, 142496l), "third shhh");
	}

	public Iterator<Segment> getSegmentIterator() {
		return transcriptionMap.keySet().iterator();
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
