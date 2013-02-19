package au.edu.unimelb.aikuma.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import junit.framework.TestCase;

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
				user.encodeUser().toString());
	}

	/**
	 * Test when the user has no associated data
	 */
	/*
	public void testWriteUser1() {
		User user = new User();
	}
	*/

	/**
	 * Test when the user just has a name
	 */
	 /*
	public void testWriteUser2() {
		User user = new User();
		String name = "TestUser";
		user.setName(name);
	}
	*/

	/*
	public void testWriteUser3() {
		User user = new User();
		UUID uuid = UUID.randomUUID();
		user.setUUID(uuid);
	}
	*/

	/*
	public void testWriteUser4() {
		User user = new User();
		String name = "TestUser";
		UUID uuid = UUID.randomUUID();
		user.setName(name);
		user.setUUID(uuid);
	}
	*/
}
