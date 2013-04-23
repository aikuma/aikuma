package au.edu.unimelb.aikuma.audio;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.util.Log;

import au.edu.unimelb.aikuma.R;

/** A Recorder is used to get input from eg. a microphone and
 *  output into a file.
 * 
 *  Usage:
 *    Recorder recorder = new Recorder();
 *    recorder.prepare("/mnt/sdcard/bold/recordings/target_file.wav")
 *    recorder.listen();
 *    recorder.pause();
 *    recorder.listen();
 *    recorder.stop();
 *
 *  Note that stopping the recorder closes and finalizes the WAV file.
 */
public class Recorder implements Sampler {

	/** File to write to. */
	protected PCMFile file;
	
	/** Microphone input */
	Microphone microphone;

	public long getCurrentSample() {
		return file.getCurrentSample();
	}
	
	private Beeper beeper;

	private boolean recording = false;
	
	public Recorder() {
		setUpMicrophone();
		setUpFile();
	}
	
	public Recorder(Context context) {
		setUpMicrophone();
		setUpFile();

		beeper = new Beeper(context);
	}

	public boolean isRecording() {
		return recording;
	}

	protected void setUpMicrophone() {
		this.microphone = new Microphone();
	}

	/** Sets the file up for writing. */
	protected void setUpFile() {
		file = PCMFile.getInstance(microphone);
  }

	/**
	 * Prepares the recorder for recording.
	 */
	public void prepare(String targetFilename) {
		file.prepare(new File(targetFilename));
	}

	/** Start listening. */
	public void listen() {
		recording = true;
		if (beeper != null) {
			beeper.beepBeep(new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer _) {
					microphone.listen(file);
				}
			});
		} else {
			microphone.listen(file);
		}
	}

	/** Stop listening to the microphone and close the file.
	 *
	 * Note: Once stopped you cannot restart the recorder.
	 */
	public void stop() {
		recording = false;
		microphone.stop();
		file.close();
	}

	/** Pause listening to the microphone. */
	public void pause() {
		recording = false;
		microphone.stop();
		if (beeper != null) {
			beeper.beep();
		}
	}
}
