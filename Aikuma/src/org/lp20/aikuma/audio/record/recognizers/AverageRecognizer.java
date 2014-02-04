/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record.recognizers;

import android.util.Log;
import org.lp20.aikuma.audio.Processor;
import org.lp20.aikuma.audio.record.recognizers.Recognizer;

/** 
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class AverageRecognizer extends Recognizer {

	/** The threshold below which silence is detected.*/
	protected int silenceThreshold;
	/** The threshold above which sound is detected.*/
	protected int speechThreshold;

	/**
	 * Sets up the processor that tells us the average amplitude.
	 */
	protected Processor processor = new Processor();

	/**
	* Default Constructor.
	*
	*  Default silence is less than 1/32 of the maximum.
	*  Default speech is more than 1/32 of the maximum.
	*/
	public AverageRecognizer() {
		this(32768/32, 32768/32); // MediaRecorder.getAudioSourceMax();
	}

	/**
	 * Constructor.
	 * @param silenceThreshold Silence is less than 1/silenceDivisor of the maximum.
	 * @param speechThreshold  Speech is more than 1/speechDivisor of the maximum.
	 */
	public AverageRecognizer(int silenceThreshold, int speechThreshold) {
		// Silence is less than
		// 1/n of max amplitude.
		//
		this.silenceThreshold = silenceThreshold;

		// Speech is more than 1/m of max amplitude.
		//
		this.speechThreshold = speechThreshold;
	}

	@Override
	public boolean isSilence(short[] buffer) {
		int reading = processor.getAverage(buffer);
		Log.i("sound", "reading: " + reading + ", silenceThreshold: " +
				silenceThreshold);
		return reading < silenceThreshold;
	}

	@Override
	public boolean isSpeech(short[] buffer) {
		int reading = processor.getAverage(buffer);

		Log.i("sound", "silent reading: " + reading + ", silenceThreshold: " +
				silenceThreshold);
		return reading > speechThreshold;
	}
}
