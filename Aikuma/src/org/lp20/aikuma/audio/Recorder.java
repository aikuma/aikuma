package org.lp20.aikuma.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.util.Log;
import java.util.Arrays;
import java.util.Set;
import org.lp20.aikuma.audio.analyzers.Analyzer;
import org.lp20.aikuma.audio.analyzers.SimpleAnalyzer;
import org.lp20.aikuma.R;
import static org.lp20.aikuma.audio.Microphone.MicException;

/**
 *  A Recorder used to get input from a microphone and output into a file.
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

	/**
	 * Creates a Recorder that uses an analyzer which tells the recorder to
	 * always record regardless of input.
	 */
	public Recorder(int sampleRate) throws MicException {
		this(sampleRate, new SimpleAnalyzer());
	}

	/**
	 * Accepts and analyzer that decides whether the recorder should record or
	 * ignore the input
	 *
	 * @param Pass in an analyzer which decides whether
	 *        the recorder should record or ignore the input.
	 */
	public Recorder(int sampleRate, Analyzer analyzer) throws MicException {
		this.analyzer = analyzer;
		Log.i("Recorder", "about to set up microphone");
		setUpMicrophone(sampleRate);
		Log.i("Recorder", "finished seting up microphone");
		Log.i("Recorder", "about to set up file");
		setUpFile();
		Log.i("Recorder", "finished seting up file");
	}

	/** Start listening. */
	public void listen() {
		microphone.listen(this);
	}

	/**
	 * Returns the point in the recording at which the Recorder is up to, in
	 * samples.
	 *
	 * @return	The point in the recording that the Recorder is up to, in
	 * samples
	 */
	public long getCurrentSample() {
		return file.getCurrentSample();
	}

	/**
	 * Prepares the recorder for recording.
	 */
	public void prepare(String targetFilename) {
		file.prepare(targetFilename);
	}

	/** Sets up the micrphone for recording */
	private void setUpMicrophone(int sampleRate) throws MicException {
		this.microphone = new Microphone(sampleRate);
	}

	/** Sets the file up for writing. */
	private void setUpFile() {
		file = PCMWriter.getInstance(
				microphone.getSampleRate(),
				microphone.getChannelConfiguration(),
				microphone.getAudioFormat()
		);
	}

	/** Stop listening to the microphone and close the file.
	 *
	 * Note: Once stopped you cannot restart the recorder.
	 */
	public void stop() throws MicException {
		microphone.stop();
		file.close();
	}

	/** Pause listening to the microphone. */
	public void pause() throws MicException {
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

	/** File to write to. */
	private PCMWriter file;

	/** Microphone input */
	private Microphone microphone;

	/** Analyzer that analyzes the incoming data. */
	private Analyzer analyzer;
}
