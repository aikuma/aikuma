package au.edu.unimelb.boldapp.audio.analyzers;

import au.edu.unimelb.boldapp.audio.AudioHandler;

public class SimpleAnalyzer extends Analyzer {

	/** Simply always calls audioTriggered. */
	public void analyze(AudioHandler handler, short[] buffer) {
		handler.audioTriggered(buffer, false);
	}
}
