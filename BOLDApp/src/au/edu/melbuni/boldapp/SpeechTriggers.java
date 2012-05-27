package au.edu.melbuni.boldapp;

public interface SpeechTriggers {

	public void silenceTriggered(short[] buffer);
	public void speechTriggered(short[] buffer);
	
}
