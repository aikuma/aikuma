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
				result = client.login("bold", "bold");
				loggedIn = result;
			}
			// client.enterRemotePassiveMode();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public String getBaseDir() {
		File external = Environment.getExternalStorageDirectory();
		if (external != null) {
			// On the phone.
			//
			return "production";
		}
		// During e.g. tests.
		//
		return "test";
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
	public boolean doesSegmentExist(String timelineId, String segmentId) {
		cdToSegments(timelineId, false);
		String remoteName = segmentId + new JSONPersister().fileExtension();
		return exists(remoteName);
	}

	@Override
	public boolean doesExist(Timeline timeline) {
		cdToTimelines();
		return exists(timeline.getIdentifier()
				+ new JSONPersister().fileExtension());
	}

	@Override
	public boolean doesExist(User user) {
		cdToUsers();
		return exists(user.getIdentifier()
				+ new JSONPersister().fileExtension());
	}

	@Override
	public boolean post(User user) {
		boolean result = false;
		if (cdToUsers()) {
			Persister persister = new JSONPersister();
			result = postFileWithSameName(persister.pathFor(user));
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
			result = postFileWithSameName(persister
					.pathFor(timelineId, segment));
		}
		return result;
	}

	@Override
	public Segment getSegment(String segmentId, String timelineId) {
		cdToSegments(timelineId);
		getFileWithSameName(segmentId + ".json");
		return Segment.load(new JSONPersister(), timelineId, segmentId);
	}

	@Override
	public List<String> getSegmentIds(String timelineId) {
		cdToSegments(timelineId);
		return getIds();
	}

	@Override
	public Timeline getTimeline(String timelineId, String userId, Users users) {
		cdToTimelines();
		getFileWithSameName(timelineId + ".json");
		return Timeline.load(users, new JSONPersister(), timelineId);
	}

	@Override
	public List<String> getTimelineIds() {
		cdToTimelines();
		return getIds();
	}

	@Override
	public User getUser(String userId) {
		cdToUsers();
		getFileWithSameName(userId + ".json");
		return User.load(new JSONPersister(), userId);
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
		File file = new File(path);
		InputStream stream = null;
		boolean result = false;
		login();
		try {
			client.type(FTP.ASCII_FILE_TYPE);
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
	
	public boolean getFileWithSameName(String filename) {
		OutputStream stream = null;
		boolean result = false;
		login();
		try {
			client.type(FTP.ASCII_FILE_TYPE);
			stream = new FileOutputStream(filename);
			result = client.retrieveFile(filename, stream);
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

	public List<String> getIds() {
		String[] strings = null;
		login();
		try {
			strings = client.listNames("*"
					+ new JSONPersister().fileExtension());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> results = new ArrayList<String>();
		for (String string : strings) {
			results.add(string.replace(new JSONPersister().fileExtension(), ""));
		}
		return results;
	}
}
