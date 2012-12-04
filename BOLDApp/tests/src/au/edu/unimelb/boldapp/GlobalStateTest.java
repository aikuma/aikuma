package au.edu.unimelb.boldapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.util.Log;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

public class GlobalStateTest extends TestCase {

	public void testLoadUsers() throws Exception {
		User testUser = new User(UUID.randomUUID(), "test user");
		FileIO.writeUser(testUser);
		GlobalState.loadUsers();
		List<User> users = GlobalState.getUsers();
		boolean inTheList = false;
		for (User user : users) {
			if (user.getUUID().equals(testUser.getUUID())) {
				if (user.getName().equals(testUser.getName())) {
					inTheList = true;
				}
			}
		}
		assertTrue(inTheList);
		try {
			FileUtils.deleteDirectory(new File(FileIO.getUsersPath(),
					testUser.getUUID().toString()));
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void testSortRecordingsAlphabetical() {
		List<Recording> recordings = new ArrayList<Recording>();
		recordings.add(new Recording(UUID.randomUUID(), UUID.randomUUID(),
				"Alpha", new Date()));
		recordings.add(new Recording(UUID.randomUUID(), UUID.randomUUID(),
				"Epsilon", new Date()));
		recordings.add(new Recording(UUID.randomUUID(), UUID.randomUUID(),
				"Delta", new Date()));
		recordings.add(new Recording(UUID.randomUUID(), UUID.randomUUID(),
				"Beta", new Date()));
		GlobalState.setRecordings(recordings);
		// Just assert that setters and getters work
		assertEquals(recordings, GlobalState.getRecordings());
		recordings = GlobalState.getRecordings("alphabetical");
		assertEquals("Alpha", recordings.get(0).getName());
		assertEquals("Beta", recordings.get(1).getName());
		assertEquals("Delta", recordings.get(2).getName());
		assertEquals("Epsilon", recordings.get(3).getName());
	}

	public void testSortRecordingsDate() {
		List<Recording> recordings = new ArrayList<Recording>();
		recordings.add(new Recording(UUID.randomUUID(), UUID.randomUUID(),
				"Alpha", new Date(1l), UUID.randomUUID()));
		recordings.add(new Recording(UUID.randomUUID(), UUID.randomUUID(),
				"Epsilon", new Date(3l), UUID.randomUUID()));
		recordings.add(new Recording(UUID.randomUUID(), UUID.randomUUID(),
				"Delta", new Date(2l), UUID.randomUUID()));
		recordings.add(new Recording(UUID.randomUUID(), UUID.randomUUID(),
				"Beta", new Date(4l), UUID.randomUUID()));
		GlobalState.setRecordings(recordings);
		// Just assert that getters and setters work.
		assertEquals(recordings, GlobalState.getRecordings());
		recordings = GlobalState.getRecordings("date");
		assertEquals("Alpha", recordings.get(0).getName());
		assertEquals("Delta", recordings.get(1).getName());
		assertEquals("Epsilon", recordings.get(2).getName());
		assertEquals("Beta", recordings.get(3).getName());
	}

	public void testLoadRecordings() throws Exception {
		Recording testRecording = new Recording(UUID.randomUUID(), UUID.randomUUID(),
				"test recording", new Date(), UUID.randomUUID());
		FileIO.writeRecording(testRecording);
		GlobalState.loadRecordings();
		List<Recording> recordings = GlobalState.getRecordings();
		boolean inTheList = false;
		for (Recording recording : recordings) {
			if (recording.getUUID().equals(testRecording.getUUID())) {
				if (recording.getName().equals(testRecording.getName())) {
					if (recording.getOriginalUUID().equals(
							testRecording.getOriginalUUID())) {
						inTheList = true;
					}
				}
			}
		}
		assertTrue(inTheList);
		assertTrue(new File(FileIO.getRecordingsPath(),
					testRecording.getUUID().toString() + ".json").delete());
	}

}
