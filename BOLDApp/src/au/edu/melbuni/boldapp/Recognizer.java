package au.edu.melbuni.boldapp;

public abstract class Recognizer {

	public abstract boolean isSilence(byte[] buffer);

	public abstract boolean isSpeech(short[] buffer);
	
}
