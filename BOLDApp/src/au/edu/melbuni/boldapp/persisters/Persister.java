package au.edu.melbuni.boldapp.persisters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Segments;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;

// A persister defines in what format
// the data is saved, and where.
//
// Not the order though.
// Because that is something else entirely.
//
public abstract class Persister {

	// Public API.
	//
	
	public abstract void save(Users users);
	
	public abstract Users loadUsers();

	public abstract void save(User user);

	public abstract User loadUser(String identifier);
	
	public abstract void save(Timelines timelines);
	
	public abstract Timelines loadTimelines();
	
	public abstract void save(Timeline timeline);
	
	public abstract Timeline loadTimeline(String identifier);

	public abstract void save(Segments segments);
	
	public abstract Segments loadSegments(String prefix);
	
	public abstract void save(Segment segment);
	
	public abstract Segment loadSegment(String identifier);
	
	public abstract String fileExtension();
	
	public static String getBasePath() {
		File external = Environment.getExternalStorageDirectory();
		if (external != null) {
			// On the phone.
			//
			return external.getAbsolutePath() + "/bold/";
		}
		return "./mnt/sdcard/bold/"; // During e.g. tests.
	}
	
	// Current
	//
	
	// Returns null if no current user has been saved.
	//
	public User loadCurrentUser(Users users) {
		String identifier = null;
		try {
			identifier = read(pathForCurrentUser());
		} catch (IOException e) {
			identifier = null;
		}
		
		User user = users.find(identifier);
		if (user != null) {
			return user;
		}
		
		return null;
	}
	
	public void saveCurrentUser(User user) {
		write(pathForCurrentUser(), user.getIdentifierString());
	}
	
//	// Returns null if no current timeline has been saved.
//	//
//	public Timeline loadCurrentTimeline(Timelines timelines) {
//		String identifier = read(pathForCurrentUser());
//		for (Timeline timeline : timelines) {
//			if (timeline.getIdentifierString() == identifier) {
//				return timeline;
//			}
//		}
//		return null;
//	}
//	
//	public void saveCurrentTimeline(Timeline timeline) {
//		write(pathForCurrentUser(), timeline.getIdentifierString());
//	}
//	
//	public String pathForCurrentTimeline() {
//		return "current/timeline.txt";
//	}

	// User(s).
	//
	
	public void write(Users users, String data) {
		write(pathFor(users), data);
	}
	
	public String readUsers() throws IOException {
		return read(pathForUsers());
	}

	public void write(User user, String data) {
		write(pathFor(user), data);
	}

	public String readUser(String identifier) throws IOException {
		return read(pathForUser(identifier));
	}

	// Segment(s).
	//
	
	public void write(Segments segments, String data) {
		write(pathFor(segments), data);
	}

	public String readSegments() throws IOException {
		return read(pathForSegments());
	}

	public void write(Segment segment, String data) {
		write(pathFor(segment), data);
	}

	public String readSegment(String identifier) throws IOException {
		return read(pathForSegment(identifier));
	}

	// Timeline(s).
	//
	
	public void write(Timelines timelines, String data) {
		write(pathFor(timelines), data);
	}

	public String readTimelines() throws IOException {
		return read(pathForTimelines());
	}

	public void write(Timeline timeline, String data) {
		write(pathFor(timeline), data);
	}

	public String readTimeline(String identifier) throws IOException {
		return read(pathForUser(identifier));
	}

	// Helper methods.
	//
	
	public String pathFor(Users users) {
		return pathForUsers();
	}

	public String pathFor(User user) {
		return pathForUser(user.getIdentifierString());
	}
	
	public String pathFor(Timelines timelines) {
		return pathForTimelines();
	}
	
	public String pathFor(Timeline timeline) {
		return pathForTimeline(timeline.getIdentifier());
	}
	
	public String pathFor(Segments segments) {
		return pathForSegments();
	}
	
	public String pathFor(Segment segment) {
		return pathForTimeline(segment.getIdentifierString());
	}
	
	
	// Paths
	//
	
	public String pathForCurrentUser() {
		return getBasePath() + "users/current.txt";
	}
	
	public String pathForUsers() {
		return getBasePath() + "users/list" + fileExtension();
	}
	
	public String pathForUser(String identifier) {
		return getBasePath() + "users/" + identifier + fileExtension();
	}
	
	public String pathForTimelines() {
		return getBasePath() + "timelines/list" + fileExtension();
	}
	
	public String pathForTimeline(String identifier) {
		return getBasePath() + "timelines/" + identifier + fileExtension();
	}
	
	public String pathForSegments() {
		return getBasePath() + "segments/list" + fileExtension(); // TODO Really?
	}

	public String pathForSegment(String identifier) {
		return getBasePath() + "segments/" + identifier + fileExtension();
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
	public String read(String fileName) throws IOException {
		StringBuffer buffer = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(fileName));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            buffer.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return buffer.toString();
	}

}
