package au.edu.melbuni.boldapp;

import java.util.ArrayList;

public class Segments {
	
	ArrayList<Segment> segments;
	
	public Segments() {
		segments = new ArrayList<Segment>();
	}
	
	public void select(int position) {
		Segment segment = get(position);
		setSelectedForPlaying(segment);
		setSelectedForRecording(segment);
	}
	
	protected Segment get(int position) {
		return segments.get(position);
	}
	
}
