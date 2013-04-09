package org.lp20.aikuma.audio.analyzers;

import org.lp20.aikuma.audio.AudioHandler;

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
