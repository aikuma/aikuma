package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.Iterator;

public class Timelines {

	public ArrayList<Timeline> timelines;

	public Timelines() {
		this.timelines = new ArrayList<Timeline>();
	}
	
	// Persistence.
	//
	
	// For each file, create a User.
	//
	public static Timelines load() {
		return Persister.loadTimelines();
	}

	// Delegation.
	//
	public Timeline get(int index) {
		return timelines.get(index);
	}

	public int size() {
		return timelines.size();
	}

	public void add(Timeline object) {
		timelines.add(object);
	}

	public boolean contains(Timeline object) {
		return timelines.contains(object);
	}

	public void clear() {
		timelines.clear();
	}
	public Iterator<Timeline> iterator() {
		return timelines.iterator();
	}
	
}
