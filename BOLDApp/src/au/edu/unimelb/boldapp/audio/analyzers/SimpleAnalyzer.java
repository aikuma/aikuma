package au.edu.melbuni.boldapp.audio.analyzers;

import au.edu.melbuni.boldapp.audio.Recorder;

public class SimpleAnalyzer extends Analyzer {

  /** Simply always calls audioTriggered. */
	public void analyze(Recorder recorder, short[] buffer) {
		recorder.audioTriggered(buffer, false);
	}
  
}
