package org.lp20.aikuma.model;

import android.test.AndroidTestCase;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.lp20.aikuma.util.FileIO;

/**
 * Tests for model.Speaker, the class that represents the speaker of a
 * Recording.
 */

public class SpeakerTest extends AndroidTestCase {

	/**
	 * Tests the minimal constructor and accessors.
	 */
	public void testConstruction1() {
		Speaker speaker = new Speaker();
		assertTrue(speaker.getUUID() != null);
		assertEquals("", speaker.getName());
		assertEquals(new ArrayList<Language>(), speaker.getLanguages());
		assertTrue(!speaker.hasALanguage());
	}

	/**
	 * Tests the complete constructor and accessors.
	 */
	public void testConstruction2() {
		UUID uuid = UUID.randomUUID();
		String name = "Some silly name";
		Language usarufa = new Language("Usarufa", "usa");
		List<Language> languages = new ArrayList<Language>();
		languages.add(usarufa);
		Speaker speaker = new Speaker(uuid, name, languages);

		assertEquals(uuid, speaker.getUUID());
		assertEquals(name, speaker.getName());
		assertEquals(languages, speaker.getLanguages());
		assertTrue(speaker.hasALanguage());
	}

	/**
	 * Tests Speaker encoding.
	 */
	public void testEncode() {
		Language usarufa = new Language("Usarufa", "usa");
		Language english = new Language("English", "eng");
		List<Language> languages = new ArrayList<Language>();
		languages.add(usarufa);
		languages.add(english);
		Speaker speaker = new Speaker(UUID.randomUUID(), "ASpeaker", languages);
		assertEquals(
				"{\"languages\":[{\"code\":\"usa\",\"name\":\"Usarufa\"}," +
				"{\"code\":\"eng\",\"name\":\"English\"}],\"uuid\":\"" +
				speaker.getUUID() + "\",\"name\":\"ASpeaker\"}",
				speaker.encode().toString());
	}

	/**
	 * Tests reading a nonexistent Speaker's JSON file.
	 */
	public void testRead1() {
		boolean caught = false;
		try {
			Speaker.read(UUID.randomUUID());
		} catch (IOException e) {
			caught = true;
		}
		assertTrue(caught);
	}

	public void testRead2() throws IOException {
		UUID uuid = UUID.randomUUID();
		String name = "TestSpeaker";
		List<Language> languages = new ArrayList<Language>();
		languages.add(new Language("Usarufa", "usa"));
		Speaker speaker = new Speaker(uuid, name, languages);
		String jsonStr = "{\"uuid\":\"" + uuid + "\",\"name\":\"TestSpeaker\"" +
				"\"languages\":[{\"code\":\"usa\",\"name\":\"Usarufa\"}]}";
		File file = new File(FileIO.getAppRootPath(), "speakers/" + uuid +
				"/metadata.json");
		FileIO.write(file, jsonStr);
		Speaker readSpeaker = Speaker.read(uuid);
		assertEquals(speaker.getUUID(), readSpeaker.getUUID());
		assertEquals(speaker.getName(), readSpeaker.getName());
		assertEquals(speaker.getLanguages(), readSpeaker.getLanguages());
		FileUtils.deleteDirectory(file.getParentFile());
	}

	public void testRead3() throws IOException {
		UUID uuid = UUID.randomUUID();
		String name = null;
		List<Language> languages = new ArrayList<Language>();
		languages.add(new Language("Usarufa", "usa"));
		Speaker speaker = new Speaker(uuid, name, languages);
		String jsonStr = "{\"uuid\":\"" + uuid + "\",\"name\":null" +
				"\"languages\":[{\"code\":\"usa\",\"name\":\"Usarufa\"}]}";
		File file = new File(FileIO.getAppRootPath(), "speakers/" + uuid +
				"/metadata.json");
		FileIO.write(file, jsonStr);
		Speaker readSpeaker;
		readSpeaker = Speaker.read(uuid);
		assertEquals(speaker.getUUID(), readSpeaker.getUUID());
		assertEquals(speaker.getName(), readSpeaker.getName());
		assertEquals(speaker.getLanguages(), readSpeaker.getLanguages());
		FileUtils.deleteDirectory(file.getParentFile());
	}

	public void testRead4() throws IOException {
		UUID uuid = UUID.randomUUID();
		String name = null;
		List<Language> languages = new ArrayList<Language>();
		languages.add(new Language("Usarufa", "usa"));
		Speaker speaker = new Speaker(uuid, name, languages);
		String jsonStr = "{\"languages\":[{\"code\":\"usa\",\"name\":\"Usarufa\"}]}";
		File file = new File(FileIO.getAppRootPath(), "speakers/" + uuid +
				"/metadata.json");
		FileIO.write(file, jsonStr);
		boolean caught = false;
		try {
			Speaker readSpeaker = Speaker.read(uuid);
		} catch (IOException e) {
			caught = true;
			assertEquals("Null UUID in the JSON file.", e.getMessage());
		}
		assertTrue(caught);
		FileUtils.deleteDirectory(file.getParentFile());
	}

	public void testRead5() throws IOException {
		UUID uuid = UUID.randomUUID();
		String name = null;
		List<Language> languages = new ArrayList<Language>();
		languages.add(new Language("Usarufa", "usa"));
		Speaker speaker = new Speaker(uuid, name, languages);
		String jsonStr = "{\"uuid\":\"" + uuid + "\",\"name\":null" +
				"\"languages\":null}";
		File file = new File(FileIO.getAppRootPath(), "speakers/" + uuid +
				"/metadata.json");
		FileIO.write(file, jsonStr);
		boolean caught = false;
		try {
			Speaker readSpeaker = Speaker.read(uuid);
		} catch (IOException e) {
			caught = true;
			assertEquals("Null languages in the JSON file.", e.getMessage());
		}
		assertTrue(caught);
		FileUtils.deleteDirectory(file.getParentFile());
	}

	public void testWriteRead() throws IOException {
		UUID uuid = UUID.randomUUID();
		String name = "TestSpeaker";
		List<Language> languages = new ArrayList<Language>();
		languages.add(new Language("Usarufa", "usa"));
		Speaker speaker = new Speaker(uuid, name, languages);
		speaker.write();
		Speaker readSpeaker = Speaker.read(speaker.getUUID());
		assertEquals(speaker, readSpeaker);
		FileUtils.deleteDirectory(new File(FileIO.getAppRootPath(), "speakers/" +
				speaker.getUUID()));
	}

	/**
	 * Ensure readAll functions when there is an invalid Speaker JSON file amongst
	 * valid Speaker JSON files - No exception should be thrown, but the invalid
	 * Speaker should not be read.
	 */
	public void testReadAll() throws IOException {
		// Write an inappropriate metadata file (no UUID in the file)
		UUID uuid = UUID.randomUUID();
		String jsonStr = "{\"name\":\"TestSpeaker\"}";
		File file = new File(FileIO.getAppRootPath(),
				"speakers/" + uuid + "/metadata.json");
		FileIO.write(file, jsonStr);
		// Write a user with a name and uuid
		Speaker speaker1 = new Speaker(UUID.randomUUID(), "TestSpeaker1", new
				ArrayList<Language>());
		speaker1.write();
		List<Language> languages = new ArrayList<Language>();
		languages.add(new Language("Usarufa", "usa"));
		// Write a user with a name, uuid, and language.
		Speaker speaker2 = new Speaker(UUID.randomUUID(), "TestSpeaker1",
				languages);
		speaker2.write();

		List<Speaker> speakerList = Speaker.readAll();
		// Ensure the other two are actually the corresponding users.
		assertTrue(speakerList.contains(speaker1));
		assertTrue(speakerList.contains(speaker2));

		FileUtils.deleteDirectory(new File(FileIO.getAppRootPath(), "speakers/" + 
				uuid + "/metadata.json").getParentFile());
		FileUtils.deleteDirectory(new File(FileIO.getAppRootPath(), "speakers/" + 
				speaker1.getUUID() + "/metadata.json").getParentFile());
		FileUtils.deleteDirectory(new File(FileIO.getAppRootPath(), "speakers/" + 
				speaker2.getUUID() + "/metadata.json").getParentFile());
	}

	public void testDecodeJSONArray() throws Exception {
		UUID uuid1 = UUID.randomUUID();
		UUID uuid2 = UUID.randomUUID();
		String jsonStr = "{\"uuid_array\": [ \""+uuid1+"\", \""+uuid2+"\" ]}";
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) parser.parse(jsonStr);
		JSONArray speakerArray = (JSONArray) jsonObj.get("uuid_array");
		List<UUID> speakerUUIDs = Speaker.decodeJSONArray(speakerArray);
		assertEquals(2, speakerUUIDs.size());
		assertEquals(uuid1, speakerUUIDs.get(0));
		assertEquals(uuid2, speakerUUIDs.get(1));
	}
}
