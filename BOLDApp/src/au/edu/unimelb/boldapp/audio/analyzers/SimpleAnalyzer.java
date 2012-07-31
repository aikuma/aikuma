package au.edu.unimelb.boldapp.audio.analyzers;

import au.edu.unimelb.boldapp.audio.Recorder;

public class SimpleAnalyzer extends Analyzer {

	/** Simply always calls audioTriggered. */
	public void analyze(Recorder recorder, short[] buffer) {
		recorder.audioTriggered(buffer, false);
	}
}
