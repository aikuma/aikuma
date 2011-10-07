package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.Iterator;

import au.edu.melbuni.boldapp.persisters.BasePersister;

public class Timelines {

	public ArrayList<Timeline> timelines;

	public Timelines() {
		this.timelines = new ArrayList<Timeline>();
	}
	
	// Persistence.
	//
	
	// For each file, create a Timeline.
	//
	public static Timelines load() {
		return BasePersister.loadTimelines();
	}

	public void save(BasePersister persister) {
		// Save each user.
		for (Timeline timeline : timelines) {
			timeline.save(persister);
		}
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
