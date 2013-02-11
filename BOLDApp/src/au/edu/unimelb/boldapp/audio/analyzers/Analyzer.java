package au.edu.unimelb.aikuma.audio.analyzers;

import au.edu.unimelb.aikuma.audio.AudioHandler;

/** Base class for analyzers.
 *
 */
public abstract class Analyzer {

	/** A recorder calls this to analyze the incoming
	 *  audio data.

	 * 
	 *  Depending on the outcome, it either calls back
	 *   * audioTriggered
	 *   * silenceTriggered
	 *  where the first should be called if there is
	 *  something worthwhile to record and the second
	 *  if we detected only silence.
	 *
	 *  @param handler Any AudioHandler.
   *  @param buffer An array of audio samples.
	 *
	 */
	public abstract void analyze(AudioHandler handler, short[] buffer);

	/**
	 * Resets the analyzer to default values.
	 */
	public abstract void reset();
}
