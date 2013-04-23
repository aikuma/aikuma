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

	/**
	 * Plays beeps when recording starts
	 */
	private MediaPlayer startBeepPlayer;

	/**
	 * Plays beeps when recording starts
	 */
	private MediaPlayer endBeepPlayer;

	private boolean recording = false;
	
	public Recorder() {
		setUpMicrophone();
		setUpFile();
	}
	
	public Recorder(Context appContext) {
		setUpMicrophone();
		setUpFile();

		startBeepPlayer = MediaPlayer.create(appContext, R.raw.beeps);
		startBeepPlayer.setOnCompletionListener(
				new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer _) {
						Log.i("beeps", "completed!");
						microphone.listen(file);
					}
				});
		startBeepPlayer.setVolume(.10f, .10f);
		endBeepPlayer = MediaPlayer.create(appContext, R.raw.beep);
		endBeepPlayer.setVolume(.10f, .10f);
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
		if (startBeepPlayer != null) {
			// microphone.listen will get called by the startBeepPlayer
			// OnCompletionListener
			//
			// Florian: I'd pass in the OnCompletionListener here,
			// so it's obvious what is happening. If you need a
			// comment, the code is not clear.
			//
			startBeepPlayer.start();
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
		if (endBeepPlayer != null) {
			endBeepPlayer.start();
		}
	}
}
