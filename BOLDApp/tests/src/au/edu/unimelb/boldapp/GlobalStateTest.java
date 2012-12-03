package au.edu.unimelb.boldapp;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.util.Log;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

public class GlobalStateTest extends TestCase {

	public void testLoadUsers() throws Exception {
		User testUser = new User(UUID.randomUUID(), "test user");
		FileIO.writeUser(testUser);
		GlobalState.loadUsers();
		User[] users = GlobalState.getUsers();
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

}
