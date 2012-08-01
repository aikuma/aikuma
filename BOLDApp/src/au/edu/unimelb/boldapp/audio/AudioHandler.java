package au.edu.unimelb.boldapp.audio;

/** Interface for anything that handles (saves/processes) audio.
 */
public interface AudioHandler {
  
  /** The caller has detected non-silent audio.
   *
   * @param buffer An array of samples.
   * @param justChanged Whether the caller has just changed
   *        from silence to audio.
   */
	public void audioTriggered(short[] buffer, boolean justChanged);

  /** The caller has detected silent audio.
   *
   * @param buffer An array of samples.
   * @param justChanged Whether the caller has just changed
   *        from audio to silence.
   */
	public void silenceTriggered(short[] buffer, boolean justChanged);
}
