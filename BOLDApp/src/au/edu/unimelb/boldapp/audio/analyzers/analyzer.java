package au.edu.melbuni.boldapp.audio.analyzers;

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
   *  something worthwhile to record and the second,
   *  if we detected only silence.
   *
   *  @param recorder A recorder
   *
   */
	public abstract void analyze(Recorder recorder, short[] buffer) {

}