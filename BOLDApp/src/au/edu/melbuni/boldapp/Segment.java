package au.edu.melbuni.boldapp;

import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;

public class Segment {

	Timeline timeline = null;
	String identifier = null;

	protected boolean selected = false;
	protected boolean playing = false;
	protected boolean recording = false;

	public Segment(final Timeline timeLine, int id) {
		this.timeline = timeLine;
		this.identifier = timeLine.identifier + new Integer(id).toString();
	}

	public void startPlaying(Player player) {
		playing = true;
		player.startPlaying(identifier);
	}

	public void stopPlaying(Player player) {
		playing = false;
		player.stopPlaying();
	}

	public void startRecording(Recorder recorder) {
		recording = true;
		recorder.startRecording(identifier);
	}

	public void stopRecording(Recorder recorder) {
		recording = false;
		recorder.stopRecording();
	}

	public void select() {
		this.selected = true;
	}

	public void deselect() {
		this.selected = false;
	}
	
	public void remove() {
		timeline.remove(this);
	}
	
	public void colorize(Button button) {
		if (recording) {
			button.setBackgroundColor(Color.RED);
			return;
		}
		if (playing) {
			button.setBackgroundColor(Color.GREEN);
			return;
		}
		if (selected) {
			button.setBackgroundColor(Color.LTGRAY);
		} else {
			button.setBackgroundColor(Color.GRAY);
		}
	}

}
