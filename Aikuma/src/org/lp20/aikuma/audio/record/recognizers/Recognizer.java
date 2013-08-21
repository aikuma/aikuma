package org.lp20.aikuma.audio.record.recognizers;

public abstract class Recognizer {

	public abstract boolean isSilence(short[] buffer);

	public abstract boolean isSpeech(short[] buffer);
	
}
