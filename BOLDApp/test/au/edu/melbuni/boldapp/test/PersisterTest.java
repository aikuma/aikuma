package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.models.Segment;
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
	
	@Test
	public void readSegments() throws IOException {
		persister.save("someTimelineIdentifier", new Segment("0"));
		persister.save("someTimelineIdentifier", new Segment("1"));
		persister.save("someTimelineIdentifier", new Segment("2"));
		
		// Note: This one should not be read!
		//
		persister.save("someTimelineIdentifier", new Segment("100aa48f-1cbc-4c19-a79b-bc084e8606fb"));
		
		List<String> expected = new ArrayList<String>();
		expected.add("0");
		expected.add("1");
		expected.add("2");
		
		assertEquals(expected, persister.readSegments("someTimelineIdentifier"));
	}
	
}
