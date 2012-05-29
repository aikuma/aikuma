package au.edu.melbuni.boldapp;

import java.util.Arrays;

public class BufferingThresholdSpeechAnalyzer {

	int silenceTriggers = 0;
	int speechTriggers = 0;
	int silenceTriggerAmount;
	int speechTriggerAmount;

	// Buffer used for the part before speech is detected
	// (the ramp up of speech).
	//
	short[] onsetBuffer; 
	int ONSET_BUFFERS = 8;
	
	int EMPTY_SPEECH_PREAMBLE_BUFFERS = 5;

	// Buffer for buffering after speech (when
	// silence has begun).
	//
	short[] afterBuffer;
	int AFTER_SPEECH_BUFFERS = 3;

	boolean speech = false;

	Recognizer recognizer;

	public BufferingThresholdSpeechAnalyzer() {
		this(88, 3);
	}

	public BufferingThresholdSpeechAnalyzer(int silenceTriggerAmount,
			int speechTriggerAmount) {
		this.silenceTriggerAmount = silenceTriggerAmount;
		this.speechTriggerAmount = speechTriggerAmount;

		onsetBuffer = new short[] {};
		afterBuffer = new short[] {};

		this.recognizer = new AverageRecognizer(32, 32);
	}

	protected boolean doesTriggerSilence(short[] buffer) {
		if (recognizer.isSilence(buffer)) {
			silenceTriggers++;
		} else {
			silenceTriggers = 0;
		}
		return silenceTriggers > silenceTriggerAmount;
	}

	protected boolean doesTriggerSpeech(short[] buffer) {
		if (recognizer.isSpeech(buffer)) {
			speechTriggers++;

			addToOnsetBuffer(buffer);
		} else {
			speechTriggers = 0;
		}
		return speechTriggers > speechTriggerAmount;
	}

	protected void shiftOnsetBufferWith(short[] buffer) {
		onsetBuffer = Arrays.copyOfRange(onsetBuffer, buffer.length,
				onsetBuffer.length);
		
		int offset = onsetBuffer.length - 1;
		
//		addToOnsetBuffer(buffer);
		
		// Copy the buffer to avoid reference problems.
		//
		short[] copiedBuffer = Arrays.copyOf(buffer, buffer.length);
		
		// Add the copied current buffer onto the end.
		//
		for (int i = 0; i < buffer.length; i++) {
			onsetBuffer[i + offset] = copiedBuffer[i];
		}
	}

	protected void replaceOnsetBufferWith(short[] buffer) {
		onsetBuffer = Arrays.copyOf(buffer, buffer.length);
	}

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
	
	// TODO Duplicate method.
	//
	protected void addToOnsetBuffer(short[] buffer) {
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

	protected void clearOnsetBuffer() {
		onsetBuffer = new short[] {};
	}

	protected void clearAfterBuffer() {
		afterBuffer = new short[] {};
	}
	
	// Switches back and forth between modes:
	// If in silent mode (speech == false), it will
	// wait until speech occurs.
	// If speech occurs, it will hand over a combined buffer.
	//
	public void analyze(SpeechTriggers trigger, short[] buffer) {
		// Check if we need to callback.
		//
		if (speech) {
			// We are in speech mode, we do not care whether there's
			// speech.
			if (doesTriggerSilence(buffer)) {
				speech = false;
				trigger.silenceTriggered(buffer, true);
			} else { // Still in speech mode.
				trigger.speechTriggered(buffer, false);
			}
		} else { // We are in silence mode. Wait for enough speech.
			if (doesTriggerSpeech(buffer)) {
				speech = true;
				
				// Empty preamble.
				//
				trigger.speechTriggered(new short[onsetBuffer.length * EMPTY_SPEECH_PREAMBLE_BUFFERS],
						true);
				
				// Hand in the totally collected speech.
				//
				trigger.speechTriggered(onsetBuffer, false);
				
				// Clear onset buffer.
				//
				clearOnsetBuffer();
			} else { // Still in silence mode.
				
				// TODO See what's going wrong here.
				//
//				// Remember n "silent" buffers to add after speech, always.
//				//
//				if (afterBuffer.length <= (buffer.length * AFTER_SPEECH_BUFFERS)) {
//					addToAfterBuffer(buffer);
//				} else {
//					// The buffers are ready to be written.
//					//
//					trigger.speechTriggered(afterBuffer, false);
//					
//					// Reset after speech buffering.
//					//
//					clearAfterBuffer();
//				}

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
}
