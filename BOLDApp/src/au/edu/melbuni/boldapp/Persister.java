package au.edu.melbuni.boldapp;

public class Persister {
	
	public static Users loadUsers() {
		return new Users(); // TODO Actually load.
	}
	
	public static Timelines loadTimelines() {
		return new Timelines(); // TODO Load
	}

	public static Segments loadSegments(Timeline timeline) {
		return new Segments(timeline);
	}

}
