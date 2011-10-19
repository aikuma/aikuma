package au.edu.melbuni.boldapp.persisters;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;

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
public abstract class Persister {

	// Public API.
	//

	public abstract void save(User user);

	public abstract User loadUser(String identifier);

	public abstract String fileExtension();

	// User(s).
	//

	public static Users loadUsers() {
		return new Users(); // TODO Actually load.
	}

	public void write(User user, String data) {
		write(pathFor(user), data);
	}

	public String readUser(String identifier) {
		return read(pathForUser(identifier));
	}

	// Segment(s).
	//

	public static Segments loadSegments(Timeline timeline) {
		return new Segments(timeline);
	}

	// Timeline(s).
	//

	public static Timelines loadTimelines() {
		return new Timelines(); // TODO Load
	}

	// Save method for the metadata.
	//
	public void save(Timeline timeline) {
		String data = timeline.toJSON();
		// TODO Should the timeline know where to save itself?
		// No.
		write("timelines/" + timeline.identifier + fileExtension(), data);
	}

	// Helper methods.
	//

	//
	//
	public String pathFor(User user) {
		return pathForUser(user.getIdentifierString());
	}

	public String pathForUser(String identifier) {
		return "users/" + identifier + fileExtension();
	}

	// Write the data to the file.
	//
	// TODO Buffer?
	//
	public void write(String fileName, String data) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(fileName);
			fileWriter.write(data);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Read the content of the file.
	//
	public String read(String fileName) {
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(fileName);
			CharBuffer target = CharBuffer.allocate(1024); // TODO 1024?
			fileReader.read(target);
			return target.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

}
