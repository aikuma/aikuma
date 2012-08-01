package au.edu.unimelb.boldapp.audio.recognizers;

public abstract class Recognizer {

	public abstract boolean isSilence(short[] buffer);

	public abstract boolean isSpeech(short[] buffer);
	
}
