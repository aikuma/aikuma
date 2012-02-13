package au.edu.melbuni.boldapp.persisters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Environment;
import au.edu.melbuni.boldapp.BoldApplication;
import au.edu.melbuni.boldapp.filefilters.NumberedFileFilter;
import au.edu.melbuni.boldapp.filefilters.UUIDFileFilter;
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

	public Persister() {
		// Create the necessary directories.
		//
		new File(dirForUsers()).mkdir();
		new File(dirForTimelines()).mkdir();
	}

	// Public API.
	//

	public abstract String toJSON(Object object);

	public abstract Map<String, Object> fromJSON(String jsonData);

	public abstract void save(Users users);

	public abstract Users loadUsers();

	public abstract void save(User user);

	public abstract User loadUser(String identifier);

	public abstract void save(Timelines timelines);

	public abstract Timelines loadTimelines(Users users);

	public abstract void save(Timeline timeline);

	public abstract Timeline loadTimeline(Users users, String identifier);

	public abstract void save(String timelineIdentifier, Segments segments);

	public abstract Segments loadSegments(String timelineIdentifier);

	public abstract void save(String timelineIdentifier, Segment segment);

	public abstract Segment loadSegment(String timelineIdentifier,
			String identifier);

	public abstract String fileExtension();

	public static String getBasePath() {
		File external = Environment.getExternalStorageDirectory();
		if (external != null) {
			// On the phone.
			//
			return external.getAbsolutePath() + "/bold/";
		}
		// FIXME Make this dynamic.
		//
		return "./mnt/sdcard/bold/"; // During e.g. tests.
	}

	// Current
	//

	public void deleteAll() {
		deleteUsers();
		deleteTimelines();
	}

	public void deleteUsers() {
		File[] files = new File(dirForUsers()).listFiles();
		for (File file : files) {
			file.delete();
		}
	}

	public void deleteTimelines() {
		File[] files = new File(dirForTimelines()).listFiles();
		for (File file : files) {
			file.delete();
		}
	}

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

	public void saveCurrentUser(BoldApplication application) {
		write(pathForCurrentUser(), application.getCurrentUser()
				.getIdentifier());
	}

	// User(s).
	//

	public List<String> readUsers() throws IOException {
		return extractIdsFrom(new File(dirForUsers()));
	}

	public void write(User user, String data) {
		write(pathFor(user), data);
	}

	public String readUser(String identifier) throws IOException {
		return read(pathForUser(identifier));
	}

	// Segment(s).
	//
	
	// TODO Fix: Use AnyFileFilter and fix problem with synchronizer.
	//
	public List<String> readSegments(String timelineIdentifier)
			throws IOException {
		return extractIdsFrom(new File(dirForSegments(timelineIdentifier)), new NumberedFileFilter());
	}

	public void write(String timelineIdentifier, Segment segment, String data) {
		write(pathFor(timelineIdentifier, segment), data);
	}

	public String readSegment(String timelineIdentifier, String identifier)
			throws IOException {
		return read(pathForSegment(timelineIdentifier, identifier));
	}

	// Timeline(s).
	//

	// Reads all the JSON files from a directory and
	// returns the name part.
	//
	public List<String> readTimelines() throws IOException {
		return extractIdsFrom(new File(dirForTimelines()));
	}

	public void write(Timeline timeline, String data) {
		write(pathFor(timeline), data);
	}

	public String readTimeline(String identifier) throws IOException {
		return read(pathForTimeline(identifier));
	}

	// Likes.
	//
	public List<String> readLikes(String timelineIdentifier) throws IOException {
		return extractIdsFrom(new File(dirForLikes(timelineIdentifier)), new UUIDFileFilter(""));
	}
	
	// Helper methods.
	//
	
	public List<String> extractIdsFrom(File directory) {
		return extractIdsFrom(directory, new UUIDFileFilter());
	}
	public List<String> extractIdsFrom(File directory, FileFilter fileFilter) {
		return extractIdsFrom(directory, fileFilter, "\\.json$");
	}
	public List<String> extractIdsFrom(File directory, FileFilter filter, String replacementPattern) {
		List<String> ids = new ArrayList<String>();

		if (directory.exists()) {
			File[] files = directory.listFiles(filter);
			for (File file : files) {
				ids.add(file.getName().replaceAll(replacementPattern, ""));
			}
		}

		return ids;
	}

	// public String pathFor(Users users) {
	// return pathForUsers();
	// }

	public String pathFor(User user) {
		return pathForUser(user.getIdentifier());
	}

	// public String pathFor(Timelines timelines) {
	// return pathForTimelines();
	// }

	public String pathFor(Timeline timeline) {
		return pathForTimeline(timeline.getIdentifier());
	}

	// public String pathFor(Segments segments) {
	// return pathForSegments();
	// }

	public String pathFor(String timelineIdentifier, Segment segment) {
		return pathForSegment(timelineIdentifier, segment.getIdentifier());
	}

	// Paths
	//

	public String pathForCurrentUser() {
		return getBasePath() + "users/current.txt";
	}

	public String dirForUsers() {
		return getBasePath() + "users/";
	}

	// public String pathForUsers() {
	// return dirForUsers() + "list" + fileExtension();
	// }

	public String pathForUser(String identifier) {
		return dirForUsers() + identifier + fileExtension();
	}

	public String dirForTimelines() {
		return getBasePath() + "timelines/";
	}

	// public String pathForTimelines() {
	// return dirForTimelines() + "list" + fileExtension();
	// }

	public String pathForTimeline(String identifier) {
		return dirForTimelines() + identifier + fileExtension();
	}

	public String dirForSegments(String timelineIdentifier) {
		return dirForTimelines() + timelineIdentifier + "/segments/";
	}

	// public String pathForSegments() {
	// return dirForSegments() + "list" + fileExtension(); // TODO Really?
	// }

	public String pathForSegment(String timelineIdentifier, String identifier) {
		return dirForSegments(timelineIdentifier) + identifier
				+ fileExtension();
	}
	
	public String dirForLikes(String timelineIdentifier) {
		return dirForTimelines() + timelineIdentifier + "/likes/";
	}

	public String pathForLike(String timelineIdentifier, String identifier) {
		return dirForLikes(timelineIdentifier) + identifier;
	}

	// Write the data to the file.
	//
	public void write(String fileName, String data) {
		try {
			File file = new File(fileName);
			file.getParentFile().mkdirs();
			file.createNewFile();

			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
					file, false));
			bufferedWriter.write(data);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(fileName);
		}
	}

	// Read the content of the file.
	//
	public String read(String fileName) throws IOException {
		StringBuffer buffer = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			buffer.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return buffer.toString();
	}

}
