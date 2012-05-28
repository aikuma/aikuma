package au.edu.melbuni.boldapp;

public interface SpeechTriggers {

	public void silenceTriggered(short[] buffer, boolean justChanged);
	public void speechTriggered(short[] buffer, boolean justChanged);
	public void neitherTriggered(short[] buffer);
	
}
