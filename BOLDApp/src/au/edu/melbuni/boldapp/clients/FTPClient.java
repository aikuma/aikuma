package au.edu.melbuni.boldapp.clients;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;

import android.os.Environment;
import au.edu.melbuni.boldapp.Sounder;
import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;

public class FTPClient extends Client {

	org.apache.commons.net.ftp.FTPClient client;
	boolean loggedIn = false;

	public FTPClient(String serverURI) {
		super(serverURI);
		this.client = null;
	}

	@Override
	public Object getClient() {
		return client;
	}

	@Override
	public URI getServerURI(String ignored) {
		URI server = null;

		try {
			server = new URI(serverURI);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return server;
	}

	@Override
	public void lazilyInitializeClient() {
		if (client == null) {
			client = new org.apache.commons.net.ftp.FTPClient();
		}
	}

	// Deletes all files on the FTP server.
	//
	// Note: Only use this for testing.
	//
	public boolean deleteAll() {
		boolean result = false;
		cdToBase();
		try {
			result = client.deleteFile("data");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public boolean login() {
		lazilyInitializeClient();
		boolean result = false;
		try {
			if (!client.isConnected()) {
				client.connect(serverURI);
			}
			if (!loggedIn) {
				result = client.login("admin", "bold2016");
				loggedIn = result;
			}
			// client.enterRemotePassiveMode();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e.getMessage());
		}
		return result;
	}

	public boolean logout() {
		lazilyInitializeClient();
		try {
			if (loggedIn) {
				boolean result = client.logout();
				loggedIn = result;
				return result;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean maybeMakeAndCd(String pathname) {
		boolean reply = false;

		try {
			reply = client.changeWorkingDirectory(pathname);
			if (!reply) {
				client.makeDirectory(pathname);
				client.changeWorkingDirectory(pathname);
				reply = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return reply;
	}

	public String getSpecificRouterDir() {
		return "part0/share/";
	}

	public String getBaseDir() {
		File external = Environment.getExternalStorageDirectory();
		if (external != null) {
			// On the phone.
			//
			return getSpecificRouterDir() + "production/";
		}
		// During e.g. tests.
		//
		return "test/";
	}

	public boolean cdToBase() {
		login();
		maybeMakeAndCd("/");
		maybeMakeAndCd(getBaseDir());
		return maybeMakeAndCd("data");
	}

	public boolean cdToUsers() {
		cdToBase();
		return maybeMakeAndCd("users");
	}

	public boolean cdToTimelines() {
		cdToBase();
		return maybeMakeAndCd("timelines");
	}

	public boolean cdToSegments(String timelineId) {
		return cdToSegments(timelineId, false);
	}

	public boolean cdToSegments(String timelineId, boolean makeIfNotExists) {
		boolean result = false;
		cdToTimelines();
		try {
			if (makeIfNotExists) {
				maybeMakeAndCd(timelineId);
				result = maybeMakeAndCd("segments");
			} else {
				client.changeWorkingDirectory(timelineId);
				result = client.changeWorkingDirectory("segments");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public boolean doesUserExist(String userId) {
		cdToUsers();
		String remoteName = userId + new JSONPersister().fileExtension();
		return exists(remoteName);
	}
	
	@Override
	public boolean doesTimelineExist(String timelineId) {
		cdToTimelines();
		String remoteName = timelineId + new JSONPersister().fileExtension();
		return exists(remoteName);
	}

	@Override
	public boolean doesSegmentExist(String timelineId, String segmentId) {
		cdToSegments(timelineId, false);
		String remoteName = segmentId + new JSONPersister().fileExtension();
		return exists(remoteName);
	}

//	@Override
//	public boolean doesExist(Timeline timeline) {
//		cdToTimelines();
//		return exists(timeline.getIdentifier()
//				+ new JSONPersister().fileExtension());
//	}
//
//	@Override
//	public boolean doesExist(User user) {
//		cdToUsers();
//		return exists(user.getIdentifier()
//				+ new JSONPersister().fileExtension());
//	}

	@Override
	public boolean post(User user) {
		boolean result = false;
		if (cdToUsers()) {
			Persister persister = new JSONPersister();
			result = postFileWithSameName(persister.pathFor(user));
			maybeMakeAndCd(user.getIdentifier());
			postFileWithSameName(user.getProfileImagePath(), true, true);
			postFileWithSameName(user.getProfileAudioPath(), true, true);
		}
		return result;
	}

	@Override
	public boolean post(Timeline timeline) {
		boolean result = false;
		if (cdToTimelines()) {
			Persister persister = new JSONPersister();
			result = postFileWithSameName(persister.pathFor(timeline));
		}
		return result;
	}

	@Override
	public boolean post(Segment segment, String timelineId) {
		boolean result = false;
		if (cdToSegments(timelineId, true)) {
			Persister persister = new JSONPersister();

			// The recording.
			//
			postFileWithSameName(segment.getSoundfilePath(timelineId), false,
					true);

			// The segment info.
			//
			result = postFileWithSameName(persister
					.pathFor(timelineId, segment));
		}
		return result;
	}
	
	// FIXME Fix the fact that the wrong ids are uploaded to the server!
	//
	@Override
	public List<String> getSegmentIds(String timelineId) {
		cdToSegments(timelineId);
		return getIds();
	}

	@Override
	public User getUser(String userId) {
		cdToUsers();
		String fileName = Persister.getBasePath() + "users/" + userId + ".json";
		if (getFile(fileName)) {
			User user = User.load(new JSONPersister(), userId);
			getAssociatedFiles(user);
			return user;
		} else {
			return null;
		}
	}

	public void getAssociatedFiles(User user) {
		cdToUsers();
		maybeMakeAndCd(user.getIdentifier());
		getFile(user.getProfileImagePath(), true);
		getFile(user.getProfileAudioPath(), true);
	}

	@Override
	public Timeline getTimeline(String timelineId, Users users) {
		cdToTimelines();
		// TODO
		String fileName = Persister.getBasePath() + "timelines/" + timelineId
				+ ".json";
		if (getFile(fileName)) {
			Timeline timeline = Timeline.load(users, new JSONPersister(),
					timelineId);
			return timeline;
		} else {
			return null;
		}
	}
	
	@Override
	public void getSegments(String timelineId) {
		cdToSegments(timelineId);
		
		// Get the segments.
		//
		List<String> segmentIds = getIds();
		for (String segmentId : segmentIds) {
			getFile(segmentId + new JSONPersister().fileExtension());
			getFile(segmentId + Sounder.getFileExtension());
		}
	}

	@Override
	public List<String> getTimelineIds() {
		cdToTimelines();
		return getIds();
	}

	@Override
	public List<String> getUserIds() {
		cdToUsers();
		return getIds();
	}

	public boolean exists(String remoteName) {
		boolean result = false;
		login();
		try {
			return client.getModificationTime(remoteName) != null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public boolean postFileWithSameName(String path) {
		return postFileWithSameName(path, false);
	}

	public boolean postFileWithSameName(String path, boolean optional) {
		return postFileWithSameName(path, optional, false);
	}

	// You need to be in the right directory before you call this method.
	//
	public boolean postFileWithSameName(String path, boolean optional,
			boolean binary) {
		File file = new File(path); // TODO Problem??? Does this create a new file?
		if (optional && !file.exists()) {
			return false;
		}
		InputStream stream = null;
		boolean result = false;
		login();
		try {
			if (binary) {
				client.setFileType(FTP.BINARY_FILE_TYPE);
			} else {
				client.setFileType(FTP.ASCII_FILE_TYPE);
			}
			stream = new FileInputStream(file);
			result = client.storeFile(file.getName(), stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}

	public boolean getFile(String filename) {
		return getFile(filename, false);
	}

	public boolean getFile(String filename, boolean binary) {
		login();
		OutputStream stream = null;
		boolean result = false;
		try {
			if (binary) {
				client.setFileType(FTP.BINARY_FILE_TYPE);
			} else {
				client.setFileType(FTP.ASCII_FILE_TYPE);
			}

			// TODO Dry.
			//
			File file = new File(filename);
			file.getParentFile().mkdirs();
			file.createNewFile();

			stream = new FileOutputStream(filename);
			File remoteFile = new File(filename);
			result = client.retrieveFile(remoteFile.getName(), stream);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return result;
	}

	public List<String> getIds() {
		return getIds("*");
	}

	public List<String> getIds(String pattern) {
		String[] strings = null;
		login();
		try {
			strings = client.listNames(pattern
					+ new JSONPersister().fileExtension());
		} catch (IOException e) {
			strings = new String[0];
		}
		List<String> results = new ArrayList<String>();
		for (String string : strings) {
			results.add(string.replace(new JSONPersister().fileExtension(), ""));
		}
		return results;
	}
}
