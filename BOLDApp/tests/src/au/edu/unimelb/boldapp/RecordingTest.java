package au.edu.unimelb.aikuma;

import java.util.Date;
import java.util.UUID;
import junit.framework.TestCase;

public class RecordingTest extends TestCase {

	Recording recording;

	@Override
	public void setUp() {
		recording = new Recording();
	}

	public void testHasUUID() {
		assertTrue(!recording.hasUUID());
		recording.setUUID(UUID.randomUUID());
		assertTrue(recording.hasUUID());
	}

	public void testHasName() {
		assertTrue(!recording.hasName());
		recording.setName("douggie");
		assertTrue(recording.hasName());
	}

	public void testHasCreatorUUID() {
		assertTrue(!recording.hasCreatorUUID());
		recording.setCreatorUUID(UUID.randomUUID());
		assertTrue(recording.hasCreatorUUID());
	}
	public void testHasOriginalUUID() {
		assertTrue(!recording.hasOriginalUUID());
		recording.setOriginalUUID(UUID.randomUUID());
		assertTrue(recording.hasOriginalUUID());
	}

	public void testHasDate() {
		assertTrue(!recording.hasDate());
		recording.setDate(new Date());
		assertTrue(recording.hasDate());
	}

	public void testHasLanguages() {
		assertTrue(!recording.hasLanguages());
		recording.addLanguage(new Language("Usarufa", "usa"));
		assertTrue(recording.hasLanguages());
	}

}
