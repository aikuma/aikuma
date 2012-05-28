package au.edu.melbuni.boldapp;

public abstract class Recognizer {

	public abstract boolean isSilence(short[] buffer);

	public abstract boolean isSpeech(short[] buffer);
	
}
