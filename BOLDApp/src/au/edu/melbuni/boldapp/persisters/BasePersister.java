package au.edu.melbuni.boldapp.persisters;

import au.edu.melbuni.boldapp.Segments;
import au.edu.melbuni.boldapp.Timeline;
import au.edu.melbuni.boldapp.Timelines;
import au.edu.melbuni.boldapp.User;
import au.edu.melbuni.boldapp.Users;

// A persister defines in what format
// the data is saved, and where.
//
// Not the order though.
// Because that is something else entirely.
//
public class BasePersister {
	
	public static Users loadUsers() {
		return new Users(); // TODO Actually load.
	}

	public static Segments loadSegments(Timeline timeline) {
		return new Segments(timeline);
	}
	
	public static Timelines loadTimelines() {
		return new Timelines(); // TODO Load
	}
	
	// Save method for the metadata.
	//
	public void save(Timeline timeline) {
		String data = timeline.toJSON();
	}
	
	public static User loadUser(String identifier) {
		// Open File and load with identifier, from JSON.
		//
		return new User();
	}
	
	// Save method for the metadata.
	//
	// Called by the instance.
	//
	public static void save(User user) {
		String data = user.toJSON();
		// TODO Save to File.
	}

}
