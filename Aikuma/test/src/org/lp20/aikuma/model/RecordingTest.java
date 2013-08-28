package org.lp20.aikuma.model;

import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.Aikuma;
import android.util.Log;
import android.test.AndroidTestCase;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;

/**
 * A class to test model.Recording, the representation of a recording's
 * metadata
 */
public class RecordingTest extends AndroidTestCase {

	/**
	 * Tests the minimal constructor
	 */
	public void testConstruction1() {
		Recording recording = new Recording();
		assertTrue(recording.getUUID() != null);
		assertEquals("", recording.getName());
		assertTrue(recording.getDate() != null);
		assertEquals(new ArrayList<Language>(), recording.getLanguages());
		assertEquals(Aikuma.getAndroidID(), recording.getAndroidID());
		boolean caught = false;
		try {
			recording.getOriginalUUID();
		} catch (Exception e) {
			assertEquals("Cannot call getOriginalUUID when originalUUID is " +
					"null. Call isOriginal().", e.getMessage());
			caught = true;
		}
		assertTrue(caught);
	}

	/**
	 * Ensures constructors won't take null values for UUID.
	 */
	public void testConstruction2() {
		UUID uuid = null;
		String name = null;
		Date date = null;
		boolean caught = false;
		try {
			Recording recording = new Recording(uuid, name, date);
		} catch (IllegalArgumentException e) {
			caught = true;
			assertEquals("Recording UUID cannot be null.", e.getMessage());
		}
		assertTrue(caught);
	}

	/**
	 * Ensures constructors won't take null values for recording date.
	 */
	public void testConstruction3() {
		UUID uuid = UUID.randomUUID();
		String name = null;
		Date date = null;
		boolean caught = false;
		try {
			Recording recording = new Recording(uuid, name, date);
		} catch (IllegalArgumentException e) {
			caught = true;
			assertEquals("Recording date cannot be null.", e.getMessage());
		}
		assertTrue(caught);
	}

	/**
	 * Ensures constructors won't take null values for recording languages.
	 */
	public void testConstruction4() {
		UUID uuid = UUID.randomUUID();
		String name = null;
		Date date = new Date();
		List<UUID> speakersUUIDs = new ArrayList<UUID>();
		boolean caught = false;
		try {
			Recording recording = new Recording(uuid, name, date, null,
					speakersUUIDs, Aikuma.getAndroidID(), 16000);
		} catch (IllegalArgumentException e) {
			caught = true;
			assertEquals("Recording languages cannot be null. " +
					"Set as an empty List<Language> instead.", e.getMessage());
		}
		assertTrue(caught);
	}

	/**
	 * Ensures constructors won't take null values for recording android ID.
	 */
	public void testConstruction5() {
		UUID uuid = UUID.randomUUID();
		String name = null;
		Date date = new Date();
		List<Language> languages = new ArrayList<Language>();
		List<UUID> speakersUUIDs = new ArrayList<UUID>();
		boolean caught = false;
		try {
			Recording recording = new Recording(uuid, name, date, languages,
					speakersUUIDs, null, 16000);
		} catch (IllegalArgumentException e) {
			caught = true;
			assertEquals("The androidID for the recording cannot be null",
					e.getMessage());
		}
		assertTrue(caught);
	}

	/**
	 * Ensures Recording.write() and Recording.read() are inverse functions.
	 */
	public void testWriteRead1() throws IOException {
		UUID uuid = UUID.randomUUID();
		Date date = new Date();
		List<Language> languages = new ArrayList<Language>();
		List<UUID> speakersUUIDs = new ArrayList<UUID>();
		String androidID = Aikuma.getAndroidID();
		Recording recording =
				new Recording(uuid, null, date, languages, speakersUUIDs,
						androidID, null, 16000);
		recording.write();
		assertEquals(recording, Recording.read(uuid));
		new File(FileIO.getAppRootPath(), "recordings/" + uuid +
				".json").delete();
	}

	/**
	 * Ensures Recording.write() and Recording.read() are inverse functions.
	 */
	public void testWriteRead2() throws IOException {
		UUID uuid = UUID.randomUUID();
		Date date = new Date();
		List<Language> languages = new ArrayList<Language>();
		List<UUID> speakersUUIDs = new ArrayList<UUID>();
		Language lang1 = new Language("Usarufa", "usa");
		Language lang2 = new Language("English", "eng");
		languages.add(lang1);
		languages.add(lang2);
		String androidID = Aikuma.getAndroidID();
		Recording recording =
				new Recording(uuid, null, date, languages, speakersUUIDs,
						androidID, null, 16000);
		recording.write();
		assertEquals(recording, Recording.read(uuid));
		new File(FileIO.getAppRootPath(), "recordings/" + uuid +
				".json").delete();
	}

	/**
	 * Ensures Recording.write() and Recording.read() are inverse functions.
	 */
	public void testWriteRead3() throws IOException {
		UUID uuid = UUID.randomUUID();
		Date date = new Date();
		List<Language> languages = new ArrayList<Language>();
		List<UUID> speakersUUIDs = new ArrayList<UUID>();
		Language lang1 = new Language("Usarufa", "usa");
		Language lang2 = new Language("English", "eng");
		languages.add(lang1);
		languages.add(lang2);
		String androidID = Aikuma.getAndroidID();
		Recording recording = new Recording(
				uuid, "A name", date, languages, speakersUUIDs, androidID,
				UUID.randomUUID(), 16000);
		new File(FileIO.getAppRootPath(), "recordings/" + uuid +
				".json").delete();
	}

	/**
	 * Ensures Recording.readAll() functions correctly.
	 */
	public void testReadAll() throws IOException {
		// Create the first Recording.
		UUID uuid = UUID.randomUUID();
		Date date = new Date();
		List<Language> languages = new ArrayList<Language>();
		List<UUID> speakersUUIDs = new ArrayList<UUID>();
		String androidID = Aikuma.getAndroidID();
		Recording recording1 =
				new Recording(uuid, null, date, languages, speakersUUIDs,
						androidID, null, 16000);
		recording1.write();

		// Create the second Recording.
		uuid = UUID.randomUUID();
		date = new Date();
		languages = new ArrayList<Language>();
		Language lang1 = new Language("Usarufa", "usa");
		Language lang2 = new Language("English", "eng");
		languages.add(lang1);
		languages.add(lang2);
		androidID = Aikuma.getAndroidID();
		Recording recording2 = new Recording(
				uuid, "A name", date, languages, speakersUUIDs, androidID,
						UUID.randomUUID(), 16000);
		recording2.write();

		List<Recording> recordings = Recording.readAll();
		assertTrue(recordings.contains(recording1));
		assertTrue(recordings.contains(recording2));

		new File(FileIO.getAppRootPath(), "recordings/" +
				recording1.getUUID() + ".json").delete();
		new File(FileIO.getAppRootPath(), "recordings/" +
				recording2.getUUID() + ".json").delete();
	}

	public void testEquals() throws Exception {
		UUID uuid = UUID.randomUUID();
		Recording respeaking = new Recording(uuid, "respeaking1",
				new Date(), new ArrayList<Language>(), new ArrayList<UUID>(),
				"androidID", UUID.randomUUID(), 16000l);
		respeaking.write();
		Recording readRespeaking = Recording.read(uuid);
		assertEquals(respeaking, readRespeaking);
	}

	public void testGetRespeakings() throws Exception {
		UUID originalUUID = UUID.randomUUID();
		Recording recording = new Recording(originalUUID, "original",
				new Date(), new ArrayList<Language>(), new ArrayList<UUID>(),
				"androidID", 16000l);
		Recording respeaking1 = new Recording(UUID.randomUUID(), "respeaking1",
				new Date(), new ArrayList<Language>(), new ArrayList<UUID>(),
				"androidID", originalUUID, 16000l);
		Recording respeaking2 = new Recording(UUID.randomUUID(), "respeaking2",
				new Date(), new ArrayList<Language>(), new ArrayList<UUID>(),
				"androidID", originalUUID, 16000l);
		Recording respeakingNot = new Recording(UUID.randomUUID(),
				"respeakingNot", new Date(), new ArrayList<Language>(),
				new ArrayList<UUID>(), "androidID", UUID.randomUUID(), 16000l);
		recording.write();
		respeaking1.write();
		respeaking2.write();
		respeakingNot.write();

		List<Recording> actualRespeakings = new ArrayList<Recording>();
		actualRespeakings.add(respeaking1);
		actualRespeakings.add(respeaking2);

		Log.i("issue55", "originalUUID: " + originalUUID);
		List<Recording> returnedRespeakings = recording.getRespeakings();
		Log.i("issue55", "alleged respeaking name: " +
				returnedRespeakings.get(0).getName());
		Log.i("issue55", "alleged respeaking name: " +
				returnedRespeakings.get(1).getName());
		assertEquals(new HashSet(actualRespeakings),
				new HashSet(returnedRespeakings));

		recording.delete();
		respeaking1.delete();
		respeaking2.delete();
		respeakingNot.delete();
	}

}
