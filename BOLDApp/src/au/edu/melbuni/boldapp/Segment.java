package au.edu.melbuni.boldapp;

import java.util.Observable;
import java.util.Observer;

import android.graphics.Color;
import android.view.View;

public class Segment extends Observable {

	Timeline timeline = null;
	String identifier = null;

	protected boolean selected = false;
	protected boolean playing = false;
	protected boolean recording = false;

	public Segment(Segments segments, int id) {
		this.identifier = segments.timeline.identifier + new Integer(id).toString();
	}
	
	public void setPlaying(boolean playing) {
		this.playing = playing;
		setChanged();
		notifyObservers();
	}
	public void setRecording(boolean recording) {
		this.recording = recording;
		setChanged();
		notifyObservers();
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
		setChanged();
		notifyObservers();
	}

	public void startPlaying(Player player) {
		player.startPlaying(identifier);
		setPlaying(true);
	}

	public void stopPlaying(Player player) {
		player.stopPlaying();
		setPlaying(false);
	}

	public void startRecording(Recorder recorder) {
		recorder.startRecording(identifier);
		setRecording(true);
	}

	public void stopRecording(Recorder recorder) {
		recorder.stopRecording();
		setRecording(false);
	}

	public void select() {
		setSelected(true);
	}
	
	public void deselect() {
		setSelected(false);
	}
	
	public static class ViewHandler implements Observer {
		
		private View view;

		public ViewHandler(View view) {
			this.view = view;
		}

		@Override
		public void update(Observable observable, Object data) {
			if (view == null) {
				return;
			}
			
			Segment segment = (Segment) observable;
			
			System.out.println(">>>" + segment.recording + segment.playing + segment.selected);
			
			if (segment.recording) {
				view.setBackgroundColor(Color.RED);
				return;
			}
			if (segment.playing) {
				view.setBackgroundColor(Color.GREEN);
				return;
			}
			if (segment.selected) {
				view.setBackgroundColor(Color.LTGRAY);
			} else {
				view.setBackgroundColor(Color.GRAY);
			}
		}
	}

}
