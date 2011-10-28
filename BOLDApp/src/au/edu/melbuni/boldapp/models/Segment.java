package au.edu.melbuni.boldapp.models;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.json.simple.JSONValue;

import android.graphics.Color;
import android.view.View;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.Recorder;

public class Segment extends Observable {

	Timeline timeline = null;
	public String identifier = null;

	protected boolean selected = false;
	protected boolean playing = false;
	protected boolean recording = false;

	public Segment(String identifier) {
		this.identifier = identifier;
	}
	
	@SuppressWarnings("rawtypes")
	public static Segment fromJSON(String data) {
		Map segment = (Map) JSONValue.parse(data);
		String identifier = segment.get("identifier") == null ? "" : (String) segment.get("identifier");
		return new Segment(identifier);
	}
	@SuppressWarnings("rawtypes")
	public String toJSON() {
		Map<String, Comparable> segment = new LinkedHashMap<String, Comparable>();
		segment.put("identifier", this.identifier);
		return JSONValue.toJSONString(segment);
	}
	
	public void setPlaying(boolean playing) {
		this.playing = playing;
		setChanged();
		notifyObservers();
	}
	public boolean isPlaying() {
		return this.playing;
	}
	public void setRecording(boolean recording) {
		this.recording = recording;
		setChanged();
		notifyObservers();
	}
	public boolean isRecording() {
		return this.recording;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
		setChanged();
		notifyObservers();
	}
	public boolean isSelected() {
		return this.selected;
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

	public String getIdentifierString() {
		return identifier;
	}

}
