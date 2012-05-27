package au.edu.melbuni.boldapp;

public interface SpeechTriggers {

	public void silenceTriggered(short[] buffer, int reading, boolean justChanged);
	public void speechTriggered(short[] buffer, int reading, boolean justChanged);
	
}
