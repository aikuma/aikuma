/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.File;


import org.lp20.aikuma.audio.record.analyzers.Analyzer;
import org.lp20.aikuma.audio.record.analyzers.SimpleAnalyzer;
import org.lp20.aikuma.audio.Beeper;
import org.lp20.aikuma.audio.Sampler;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.ui.RecordActivity;
import static org.lp20.aikuma.audio.record.Microphone.MicException;

/**
 *  A Recorder used to get input from a microphone and output into a file.
 *
 *  Usage:
 *    Recorder recorder = new Recorder(File, sampleRate [, Analyzer]);
 *    recorder.listen();
 *    recorder.pause();
 *    recorder.listen();
 *    recorder.stop();
 *
 *  Note that stopping the recorder closes and finalizes the WAV file.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Recorder implements AudioHandler, MicrophoneListener, Sampler {

	/**
	 * Creates a Recorder that uses an analyzer which tells the recorder to
	 * always record regardless of input.
	 * only used in ThumbRespeaker so Recorder-class only uses SimpleAnalyzer.
	 *
	 * @param	path	The path to the file where the recording will be stored
	 * @param	sampleRate	The sample rate that the recording should be taken
	 * at.
	 * @throws	MicException	If there is an issue setting up the microphone.
	 */
	public Recorder(File path, long sampleRate) throws MicException {
		this(path, sampleRate, new SimpleAnalyzer());
	}

	/**
	 * Creates a Recorder that uses an analyzer which tells the recorder to
	 * always record regardless of input. Contains a beeper.
	 */
	 /*
	public Recorder(File path, long sampleRate, RecordActivity recordActivity) throws MicException {
		this(path, sampleRate, new SimpleAnalyzer());
		beeper = new Beeper(recordActivity);
	}
	*/

	/**
	 * Constructor
	 *
	 * @param	path	The path to where the recording should be stored.
	 * @param	sampleRate	The sample rate the recording should be taken at.
	 * @param	analyzer	The analyzer that determines whether the recorder
	 * should record or ignore the input
	 * @throws	MicException	If there is an issue setting up the microphone.
	 */
	public Recorder(File path, long sampleRate, Analyzer analyzer) throws MicException {
		this.analyzer = analyzer;
		setUpMicrophone(sampleRate);
		setUpFile();
		this.prepare(path.getPath());
		
		audioBuffer = new short[10000];
		audioBufLength = 0;
	}

	/** Start listening. */
	public void listen() {
		/*
		if (beeper != null) {
			beeper.beepBeep(new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer _) {
					microphone.listen(Recorder.this);
					beeper.getRecordActivity().substituteRecordButton();
				}
			});
		} else {
		*/
		audioBufLength = 0;
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

	public int getCurrentMsec() {
		return sampleToMsec(getCurrentSample());
	}

	// Converts a sample value to milliseconds.
	private int sampleToMsec(long sample) {
		long msec = sample / (microphone.getSampleRate() / 1000);
		if (msec > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) msec;
	}

	/**
	 * Prepares the recorder for recording.
	 */
	private void prepare(String targetFilename) {
		file.prepare(targetFilename);
	}

	/** Sets up the micrphone for recording */
	private void setUpMicrophone(long sampleRate) throws MicException {
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

	/**
	 * Returns the number of channels of the WAV.
	 *
	 * @return	The number of channels of the WAV.
	 */
	public int getNumChannels() {
		if (microphone.getChannelConfiguration() ==
				AudioFormat.CHANNEL_IN_MONO) {
			return 1;
		}else {
			return 2;
		}
	}

	/**
	 * Returns the bits per sample of the WAV
	 *
	 * @return	The bits per sample of the WAV.
	 */
	public int getBitsPerSample() {
		if (microphone.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
			return 16;
		} else {
			return 8;
		}
	}

	/**
	 * Returns the audio mime type (but only the section after the forward
	 * slash)
	 *
	 * @return	The audio format
	 */
	public String getFormat() {
		return "vnd.wave";
	}

	/**
	 * Stop listening to the microphone and close the file.
	 *
	 * Note: Once stopped you cannot restart the recorder.
	 *
	 * @throws	MicException	If there was an issue stopping the microphone.
	 */
	public void stop() throws MicException {
		microphone.stop();
		file.close();
	}

	/**
	 * Release resources associated with this recorder.
	 */
	public void release() {
		if (microphone != null) {
			microphone.release();
		}
	}

	/**
	 * Pause listening to the microphone.
	 *
	 * @throws	MicException	If there was an issue stopping the microphone.
	 */
	public void pause() throws MicException {
		microphone.stop();
		/*
		beeper.beep();
		*/
	}
	
	/** 
	 * By default simply writes the audioBuffer to the file.
	 */
	public void save() {
		file.write(audioBuffer, audioBufLength);
		audioBufLength = 0;
	}

	/**
	 * Callback for the microphone.
	 *
	 * @param	buffer	The buffer containing audio data.
	 */
	public void onBufferFull(short[] buffer) {
		// This will call back the methods:
		//  * silenceTriggered
		//  * audioTriggered
		//
		
		// SimplieAnalyzer.analyze calls audioTriggered, which is just a writing function
		//analyzer.analyze(this, buffer); 
		
		addAll(buffer);
	}
	
	// Append all audio-values in srcBuffer to audioBuffer
	private void addAll(short[] srcBuffer) {
		if(audioBuffer.length < audioBufLength + srcBuffer.length) {
			int newBufLength = 2 * audioBuffer.length;
			short[] newBuffer = new short[newBufLength];
			System.arraycopy(audioBuffer, 0, newBuffer, 0, audioBufLength);
			audioBuffer = newBuffer;
		}
		
		System.arraycopy(srcBuffer, 0, audioBuffer, audioBufLength, srcBuffer.length);
		audioBufLength += srcBuffer.length;
	}
	

	//The following two methods handle silences/speech (called by Analyzer class)
	// discovered in the input data.
	//
	// If you need a different behaviour, override.
	//

	/** 
	 * By default simply writes the buffer to the file.
	 * @param	buffer	The buffer containing the audio data.
	 * @param	justChanged	Indicates whether audio has just been triggered
	 * after a bout of silence.
	 */
	public void audioTriggered(short[] buffer, boolean justChanged) {
		file.write(buffer);
	}

	/**
	 * Does nothing by default if silence is triggered.
	 *
	 * @param	buffer	The buffer containing the audio data.
	 * @param	justChanged	Indicates whether silence has just been triggered
	 * after a bout of audio.
	 */
	public void silenceTriggered(short[] buffer, boolean justChanged) {
		// Intentionally empty.
	}

	/** File to write to. */
	private PCMWriter file;

	/** Buffer to keep audio data temporarily **/
	private short[] audioBuffer;
	
	private int audioBufLength;
	
	/** Microphone input */
	private Microphone microphone;

	/** Analyzer that analyzes the incoming data. */
	private Analyzer analyzer;

	/** Facilitates beeping when recording starts and stops.*/
	//private Beeper beeper;
}
