package au.edu.unimelb.aikuma.audio;

/**
 * Interface for anything that listens to microphone data.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public interface MicrophoneListener {

	/**
	 * The microphone has picked up something.
	 *
	 * @param buffer An array of samples.
	 */
	public void onBufferFull(short[] buffer);

}
