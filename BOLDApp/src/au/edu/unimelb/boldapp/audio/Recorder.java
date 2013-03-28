package au.edu.unimelb.aikuma.audio;

import java.util.Arrays;
import java.util.Set;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.util.Log;

import au.edu.unimelb.aikuma.R;
import au.edu.unimelb.aikuma.audio.analyzers.Analyzer;
import au.edu.unimelb.aikuma.audio.analyzers.SimpleAnalyzer;

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
public class Recorder implements AudioHandler, MicrophoneListener {

	/** File to write to. */
	protected PCMWriter file;
	
	/** Microphone input */
	Microphone microphone;

	/** Analyzer that analyzes the incoming data. */
	Analyzer analyzer;

	/**
	 * Plays beeps when recording starts
	 */
	protected MediaPlayer beepPlayer;

	/** Default constructor.
	 *
	 *Note: Uses an analyzer which tells the recorder to always record.
	 */
	public Recorder() {
		this(new SimpleAnalyzer());
	}

	public Recorder(Context appContext) {
		this(new SimpleAnalyzer(), appContext);
	}

	/** Default constructor.
	 *
	 * @param Pass in an analyzer which decides whether
	 *        the recorder should record or ignore the input.
	 *
	 * Note: Uses default recording parameters.
	 */
	public Recorder(Analyzer analyzer) {
		this.analyzer = analyzer;

		setUpMicrophone();
		setUpFile();
	}

	public Recorder(Analyzer analyzer, Context appContext) {
		this.analyzer = analyzer;

		setUpMicrophone();
		setUpFile();

		beepPlayer = MediaPlayer.create(appContext, R.raw.beeps);
		beepPlayer.setVolume(.10f, .10f);
	}

	protected void setUpMicrophone() {
		this.microphone = new Microphone();
	}

	/** Sets the file up for writing. */
	protected void setUpFile() {
		file = PCMWriter.getInstance(
				microphone.getSampleRate(),
				microphone.getChannelConfiguration(),
				microphone.getAudioFormat()
		);
  }

	/**
	 * Prepares the recorder for recording.
	 */
	public void prepare(String targetFilename) {
		file.prepare(targetFilename);
	}

	/** Start listening. */
	public void listen() {
		microphone.listen(this);
	}

	/** Stop listening to the microphone and close the file.
	 *
	 * Note: Once stopped you cannot restart the recorder.
	 */
	public void stop() {
		microphone.stop();
		file.close();
	}

	/** Pause listening to the microphone. */
	public void pause() {
		microphone.stop();
	}

	/** Callback for the microphone */
	public void onBufferFull(short[] buffer) {
		// This will call back the methods:
		//  * silenceTriggered
		//  * audioTriggered
		//
		analyzer.analyze(this, buffer);
	}

	//The following two methods handle silences/speech
	// discovered in the input data.
	//
	// If you need a different behaviour, override.
	//

	/** By default simply writes the buffer to the file. */
	public void audioTriggered(short[] buffer, boolean justChanged) {
		file.write(buffer);
	}

	/** Does nothing by default if silence is triggered. */
	public void silenceTriggered(short[] buffer, boolean justChanged) {
		// Intentionally empty.
	}
}
