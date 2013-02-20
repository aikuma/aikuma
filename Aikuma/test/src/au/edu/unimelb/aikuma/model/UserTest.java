package au.edu.unimelb.aikuma.model;

import au.edu.unimelb.aikuma.util.FileIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

public class UserTest extends TestCase {

	public void testConstructor1() {
		Language usarufa = new Language("Usarufa", "usa");
		User user = new User(UUID.randomUUID(), "TestUser", usarufa);
		assertTrue(user.hasALanguage());
		List<Language> languages = new ArrayList<Language>();
		languages.add(usarufa);
		assertTrue(languages.equals(user.getLanguages()));
	}

	public void testConstructor2() {
		Language usarufa = new Language("Usarufa", "usa");
		User user = new User(UUID.randomUUID(), "TestUser");
		assertTrue(!user.hasALanguage());
		List<Language> languages = new ArrayList<Language>();
		languages.add(usarufa);
		user.addLanguage(usarufa);
		assertTrue(user.hasALanguage());
		assertTrue(languages.equals(user.getLanguages()));
	}

	public void testConstructor3() {
		Language usarufa = new Language("Usarufa", "usa");
		Language english = new Language("English", "eng");
		List<Language> languages = new ArrayList<Language>();
		languages.add(usarufa);
		languages.add(english);
		User user = new User(UUID.randomUUID(), "TestUser", languages);
		assertTrue(user.hasALanguage());
		assertTrue(languages.equals(user.getLanguages()));
	}

	public void testEncodeUser() {
		Language usarufa = new Language("Usarufa", "usa");
		Language english = new Language("English", "eng");
		List<Language> languages = new ArrayList<Language>();
		languages.add(usarufa);
		languages.add(english);
		User user = new User(UUID.randomUUID(), "TestUser", languages);
		assertEquals(
				"{\"languages\":[{\"code\":\"usa\",\"name\":\"Usarufa\"}," +
				"{\"code\":\"eng\",\"name\":\"English\"}],\"uuid\":\"" +
				user.getUUID() + "\",\"name\":\"TestUser\"}",
				user.encode().toString());
	}

	public void testWriteUser() throws IOException {
		User user = new User(UUID.randomUUID(), "TestUser",
				new Language("Usarufa", "usa"));
		user.write();
		File file = new File(FileIO.getAppRootPath(),
				"users/" + user.getUUID() + "/metadata.json");
		String jsonStr = FileIO.read(file);
		assertEquals(
				"{\"languages\":[{\"code\":\"usa\",\"name\":\"Usarufa\"}" +
				"],\"uuid\":\"" + user.getUUID() + "\",\"name\":\"TestUser\"}",
				user.encode().toString());
		FileUtils.deleteDirectory(file.getParentFile());
	}

	/**
	 * Reading when the file doesn't exist
	 */
	public void testRead1() {
		boolean caught = false;
		try {
			User.read(UUID.randomUUID());
		} catch (IOException e) {
			caught = true;
		}
		assertTrue(caught);
	}

	public void testRead2() throws IOException {
		UUID uuid = UUID.randomUUID();
		String jsonStr = "{\"languages\":[{\"code\":\"usa\",\"name\":\"Usarufa\"}" +
				"],\"uuid\":\"" + uuid + "\",\"name\":\"TestUser\"}";
		File file = new File(FileIO.getAppRootPath(),
				"users/" + uuid + "/metadata.json");
		FileIO.write(file, jsonStr);
		User user = User.read(uuid);
		List<Language> languages = new ArrayList<Language>();
		languages.add(new Language("Usarufa", "usa"));
		assertTrue(languages.equals(user.getLanguages()));
		assertEquals("TestUser", user.getName());
		assertEquals(uuid, user.getUUID());
		FileUtils.deleteDirectory(file.getParentFile());
	}

	public void testRead3() throws IOException {
		UUID uuid = UUID.randomUUID();
		String jsonStr = "{\"uuid\":\"" + uuid + "\",\"name\":\"TestUser\"}";
		File file = new File(FileIO.getAppRootPath(),
				"users/" + uuid + "/metadata.json");
		FileIO.write(file, jsonStr);
		User user = User.read(uuid);
		List<Language> languages = new ArrayList<Language>();
		languages.add(new Language("Usarufa", "usa"));
		assertEquals(false, user.hasALanguage());
		assertEquals("TestUser", user.getName());
		assertEquals(uuid, user.getUUID());
		FileUtils.deleteDirectory(file.getParentFile());
	}

	public void testRead4() throws IOException {
		UUID uuid = UUID.randomUUID();
		//Missing a finishing curly brace
		String jsonStr = "{\"uuid\":\"" + uuid + "\",\"name\":\"TestUser\"";
		File file = new File(FileIO.getAppRootPath(),
				"users/" + uuid + "/metadata.json");
		FileIO.write(file, jsonStr);
		boolean caught = false;
		try {
			User user = User.read(uuid);
		} catch (IOException e) {
			caught = true;
		}
		assertTrue(caught);
		FileUtils.deleteDirectory(file.getParentFile());
	}

	public void testRead5() throws IOException {
		UUID uuid = UUID.randomUUID();
		//Missing a finishing curly brace
		String jsonStr = "{\"name\":\"TestUser\"}";
		File file = new File(FileIO.getAppRootPath(),
				"users/" + uuid + "/metadata.json");
		FileIO.write(file, jsonStr);
		boolean caught = false;
		String message = "";
		try {
			User user = User.read(uuid);
		} catch (IOException e) {
			caught = true;
			message = e.getMessage();
		}
		assertEquals("No UUID in the JSON file.", message);
		assertTrue(caught);
		FileUtils.deleteDirectory(file.getParentFile());
	}

	public void testReadAll() throws IOException {
		// Write an inappropriate metadata file (no UUID in the file)
		UUID uuid = UUID.randomUUID();
		String jsonStr = "{\"name\":\"TestUser\"}";
		File file = new File(FileIO.getAppRootPath(),
				"users/" + uuid + "/metadata.json");
		FileIO.write(file, jsonStr);
		// Write a user with a name and uuid
		User user1 = new User(UUID.randomUUID(), "TestUser1");
		user1.write();
		// Write a user with a name, uuid, and language.
		User user2 = new User(UUID.randomUUID(), "TestUser2",
				new Language("Usarufa", "usa"));
		user2.write();

		List<User> userList = User.readAll();
		// Ensure that the poorly formed user doesn't somehow get read.
		assertEquals(2, userList.size());
		// Ensure the other two are actually the corresponding users.
		assertTrue(userList.contains(user1));
		assertTrue(userList.contains(user2));

		FileUtils.deleteDirectory(new File(FileIO.getAppRootPath(), "users/" + 
				uuid + "/metadata.json").getParentFile());
		FileUtils.deleteDirectory(new File(FileIO.getAppRootPath(), "users/" + 
				user1.getUUID() + "/metadata.json").getParentFile());
		FileUtils.deleteDirectory(new File(FileIO.getAppRootPath(), "users/" + 
				user2.getUUID() + "/metadata.json").getParentFile());

	}
}
