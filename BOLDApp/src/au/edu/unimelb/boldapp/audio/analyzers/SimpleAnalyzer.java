package au.edu.unimelb.aikuma.audio.analyzers;

import au.edu.unimelb.aikuma.audio.AudioHandler;

public class SimpleAnalyzer extends Analyzer {

	/** Simply always calls audioTriggered. */
	public void analyze(AudioHandler handler, short[] buffer) {
		handler.audioTriggered(buffer, false);
	}

	/**
	 * Resets the analyzer to default values.
	 */
	public void reset() {
	}
}
