package au.edu.unimelb.aikuma.audio.analyzers;

import java.util.Arrays;

import android.util.Log;

import au.edu.unimelb.aikuma.audio.AudioHandler;
import au.edu.unimelb.aikuma.audio.analyzers.Analyzer;
import au.edu.unimelb.aikuma.audio.recognizers.Recognizer;
import au.edu.unimelb.aikuma.audio.recognizers.AverageRecognizer;

/** Analyzes the samples given to it.
 *
 *  It also remembers what it had heard before.
 *  
 *  Silence: After speech, it listens a bit longer
 *           and if no speech is forthcoming, it will
 *           switch to silence mode, waiting for speech.
 *           It does not instantly call silenceTriggered
 *           Because the speaker might just have a quick
 *           pause in between words.
 *           (Recommended is 2 seconds of consecutive
 *           silence data)
 *
 *  Speech: After silence, it listens to whether speech
 *          is occurring. If it does it will not instantly
 *          call back the audioTriggered method.
 *          Instead it waits if it wasn't just a noise, but
 *          listens for further audio to occur.
 *          (Recommended is 0.08 seconds of consecutive
 *          speech data)
 *
 * A diagram of how this Analyzer works:
 *               _----_/\_-_
 *             _/           \_
 * ___________/               \____/\________________
 *   |<- pre ->|<-  speech  ->|<- after ->|<- silence
 *
 * 1. The Analyzer remembers PREAMBLE_BUFFERS buffers to
 *    remember before speech.
 * 2. As soon as speech occurs (speechTriggerAmount speech buffers
 *    have occurred), it saves EMPTY_SPEECH_PREAMBLE_BUFFERS buffers
 *    followed by the PREAMBLE_BUFFERS buffers.
 * 3. Then it continues to recognize speech until silenceTriggerAmount
 *    buffers have been recognized as silence.
 * 4. It then saves AFTER_SPEECH_BUFFERS buffers and starts recognizing
 *    silence, until 1. occurs again.
 */
public class ThresholdSpeechAnalyzer extends Analyzer {
  
  /** How many times silence has been triggered by samples */
	int silenceTriggers = 0;
  
  /** How many times speech has been triggered by samples */
	int speechTriggers = 0;
  
  /** How many times silence needs to be triggered by a slice until actual silence is detected */
	int silenceTriggerAmount;
  
  /** How many times speech needs to be triggered by a slice until actual speech is detected */
	int speechTriggerAmount;

	// Buffer used for the part before speech is detected
	// (the ramp up of speech).
	//
	short[] onsetBuffer;
  
  /** Amount of buffers to remember and insert before speech started */
	int ONSET_BUFFERS = 8;
  
  /** Amount of (empty) preamble buffers to insert before speech */
	int EMPTY_SPEECH_PREAMBLE_BUFFERS = 1;

	/** Buffer for buffering after speech (when silence has begun). */
	short[] afterBuffer;
  
  /** Amount of buffers to add after speech (to smooth) */
	int AFTER_SPEECH_BUFFERS = 3;

	boolean speech = false;

	Recognizer recognizer;
  
  /** Default constructor. */
	public ThresholdSpeechAnalyzer() {
		this(88, 3);
	}
  
  /** Constructor that takes speech/silence sensitivities.
   *
   * @param silenceTriggerAmount How many times does the recognizer
   *        need to recognize silence to actually trigger a change
   *        to silence in this Analyzer?
   * @param speechTriggerAmount How many times does the recognizer
   *        need to recognize speech to actually trigger a change
   *        to speech in this Analyzer?
   */
	public ThresholdSpeechAnalyzer(int silenceTriggerAmount,
			int speechTriggerAmount) {
        this(silenceTriggerAmount, speechTriggerAmount, new AverageRecognizer(32, 32));
	}

  /** Constructor that takes speech/silence sensitivities and a
   *  recognizer.
   *
   * @param silenceTriggerAmount How many times does the recognizer
   *        need to recognize silence to actually trigger a change
   *        to silence in this Analyzer?
   * @param speechTriggerAmount How many times does the recognizer
   *        need to recognize speech to actually trigger a change
   *        to speech in this Analyzer?
   * @param recognizer A silence/audio recognizer.
   */
	public ThresholdSpeechAnalyzer(int silenceTriggerAmount,
			int speechTriggerAmount, Recognizer recognizer) {
		this.silenceTriggerAmount = silenceTriggerAmount;
		this.speechTriggerAmount = speechTriggerAmount;

		onsetBuffer = new short[] {};
		afterBuffer = new short[] {};

		this.recognizer = recognizer;
	}
  
  /** Does the given buffer trigger silence? */
	protected boolean doesTriggerSilence(short[] buffer) {
		if (recognizer.isSilence(buffer)) {
			silenceTriggers++;
		} else {
			silenceTriggers = 0;
		}
		return silenceTriggers > silenceTriggerAmount;
	}
  
  /** Does the given buffer trigger speech? */
	protected boolean doesTriggerSpeech(short[] buffer) {
		if (recognizer.isSpeech(buffer)) {
			speechTriggers++;
		} else {
			speechTriggers = 0;
		}
		return speechTriggers > speechTriggerAmount;
	}
  
  /** Shifts off a piece of the onset buffer and adds the given buffer to the end. */
	protected void shiftOnsetBufferWith(short[] buffer) {
		onsetBuffer = Arrays.copyOfRange(onsetBuffer, buffer.length,
				onsetBuffer.length);

		addToOnsetBuffer(buffer);
	}

  /** Replaces the onset buffer with the given buffer. */
	protected void replaceOnsetBufferWith(short[] buffer) {
		onsetBuffer = Arrays.copyOf(buffer, buffer.length);
	}

	/** Adds the given buffer to the after buffer.
   *
   * TODO Duplicate method. See addToOnsetBuffer.
   */
	protected void addToAfterBuffer(short[] buffer) {
		int offset = afterBuffer.length;

		// Create a new buffer.
		//
		afterBuffer = Arrays.copyOf(afterBuffer, afterBuffer.length
				+ buffer.length);

		// Copy the buffer to avoid reference problems.
		//
		short[] copiedBuffer = Arrays.copyOf(buffer, buffer.length);

		// Add the copied current buffer onto the end.
		//
		for (int i = 0; i < buffer.length; i++) {
			afterBuffer[i + offset] = copiedBuffer[i];
		}
	}

	/** Adds the given buffer to the onset buffer.
   *
   * TODO Duplicate method. See addToAfterBuffer.
   */
	public void addToOnsetBuffer(short[] buffer) {
		int offset = onsetBuffer.length;

		// Create a new buffer.
		//
		onsetBuffer = Arrays.copyOf(onsetBuffer, onsetBuffer.length
				+ buffer.length);

		// Copy the buffer to avoid reference problems.
		//
		short[] copiedBuffer = Arrays.copyOf(buffer, buffer.length);

		// Add the copied current buffer onto the end.
		//
		for (int i = 0; i < buffer.length; i++) {
			onsetBuffer[i + offset] = copiedBuffer[i];
		}
	}

  /** Clear the before speech buffer. */
	protected void clearOnsetBuffer() {
		onsetBuffer = new short[] {};
	}
  
  /** Clear the after speech buffer. */
	protected void clearAfterBuffer() {
		afterBuffer = new short[] {};
	}
  
  /** Analyzes if there's speech.
   *
   *  Switches back and forth between modes:
   *  If in silent mode (speech == false), it will
   *  wait until speech occurs.
   *  If speech occurs, it will hand over a combined buffer.
   *
   *  @param handler An AudioHandler that handles audio/silence.
   *  @param buffer The buffer with audio samples.
   */
	public void analyze(AudioHandler handler, short[] buffer) {
		if (buffer == null) { return; }
		
		// Check if we need to callback.
		//
		if (speech) {
			// We are in speech mode, we do not care whether there's
			// speech.
			if (doesTriggerSilence(buffer)) {
				speech = false;
				handler.silenceTriggered(buffer, true);
			} else { // Still in speech mode.
				handler.audioTriggered(buffer, false);
			}
		} else { // We are in silence mode. Wait for enough speech.
			if (doesTriggerSpeech(buffer)) {
				speech = true;

				// Empty preamble.
				//
				handler.audioTriggered(new short[onsetBuffer.length
						* EMPTY_SPEECH_PREAMBLE_BUFFERS], true);
				
				// Add the buffer to the speech.
				//
				addToOnsetBuffer(buffer);

				// Hand in the totally collected speech.
				//
				handler.audioTriggered(onsetBuffer, false);

				// Clear onset buffer.
				//
				clearOnsetBuffer();
			} else { // Still in silence mode.
				// Remember n "silent" buffers to add before speech, always.
				//
				if (onsetBuffer.length < (buffer.length * ONSET_BUFFERS)) {
					addToOnsetBuffer(buffer);
				} else {
					shiftOnsetBufferWith(buffer);
				}
			}
		}
	}

	/** Mainly exposed for testing */
	public short[] getOnsetBuffer() {
		return onsetBuffer;
	}

	/**
	 * Resets this analyzer to defaults.
	 */
	public void reset() {
		silenceTriggers = 0;
		speechTriggers = 0;
		speech = false;
	}
  
}
