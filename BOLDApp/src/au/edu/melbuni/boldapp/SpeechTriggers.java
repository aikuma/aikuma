package au.edu.melbuni.boldapp;

public interface SpeechTriggers {

	public void silenceTriggered(byte[] buffer, boolean justChanged);
	public void speechTriggered(byte[] buffer, boolean justChanged);
	public void neitherTriggered(byte[] buffer);
	
}
