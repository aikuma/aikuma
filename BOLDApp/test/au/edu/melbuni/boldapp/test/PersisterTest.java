package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;

@RunWith(CustomTestRunner.class)
public class PersisterTest {
	
	Persister persister;
	
	@Before
	public void setUp() throws Exception {
		persister = new JSONPersister();
	}
	
//	@Test
//	public void pathForWithUsers() {
//		assertEquals("./mnt/sdcard/bold/users/list.json", persister.pathFor(new Users(new ArrayList<User>())));
//	}
	
	@Test
	public void pathForWithUser() {
		assertEquals(
		  "./mnt/sdcard/bold/users/00000000-0000-0001-0000-000000000002.json",
		  persister.pathFor(new User(new UUID(1, 2)))
		);
	}
	
//	@Test
//	public void pathForWithTimelines() {
//		assertEquals(
//		  "./mnt/sdcard/bold/timelines/list.json",
//		  persister.pathFor(new Timelines())
//		);
//	}
	
	@Test
	public void pathForWithTimeline() {
		UUID validUUID = UUID.randomUUID();
		assertEquals(
		  "./mnt/sdcard/bold/timelines/" + validUUID.toString() + ".json",
		  persister.pathFor(new Timeline(validUUID))
		);
	}
	
	@Test
	public void dirForUsers() {
		assertEquals("./mnt/sdcard/bold/users/", persister.dirForUsers());
	}
	
//	@Test
//	public void pathForUsers() {
//		assertEquals("./mnt/sdcard/bold/users/list.json", persister.pathForUsers());
//	}
	
	@Test
	public void pathForUser() {
		assertEquals("./mnt/sdcard/bold/users/identifier.json", persister.pathForUser("identifier"));
	}
	
	@Test
	public void dirForTimelines() {
		assertEquals("./mnt/sdcard/bold/timelines/", persister.dirForTimelines());
	}

//	@Test
//	public void pathForTimelines() {
//		assertEquals("./mnt/sdcard/bold/timelines/list.json", persister.pathForTimelines());
//	}
	
	@Test
	public void pathForTimeline() {
		assertEquals("./mnt/sdcard/bold/timelines/identifier.json", persister.pathForTimeline("identifier"));
	}
	
	@Test
	public void dirForSegments() {
		assertEquals("./mnt/sdcard/bold/timelines/timelineIdentifier/segments/", persister.dirForSegments("timelineIdentifier"));
	}
	
//	@Test
//	public void pathForSegments() {
//		assertEquals("./mnt/sdcard/bold/segments/list.json", persister.pathForSegments());
//	}
	
	@Test
	public void pathForSegment() {
		assertEquals("./mnt/sdcard/bold/timelines/timelineIdentifier/segments/identifier.json", persister.pathForSegment("timelineIdentifier", "identifier"));
	}
	
}
