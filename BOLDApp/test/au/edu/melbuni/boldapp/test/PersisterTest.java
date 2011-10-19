package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.BoldActivity;
import au.edu.melbuni.boldapp.Timeline;
import au.edu.melbuni.boldapp.Timelines;
import au.edu.melbuni.boldapp.User;
import au.edu.melbuni.boldapp.Users;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;

@RunWith(CustomTestRunner.class)
public class PersisterTest {
	
	Persister persister;
	
	@Before
	public void setUp() throws Exception {
		persister = new JSONPersister();
	}
	
	@Test
	public void pathForWithUsers() {
		assertEquals("users/list.json", persister.pathFor(new Users()));
	}
	
	@Test
	public void pathForWithUser() {
		assertEquals(
		  "users/00000000-0000-0001-0000-000000000002.json",
		  persister.pathFor(new User(new UUID(1, 2)))
		);
	}
	
	@Test
	public void pathForWithTimelines() {
		assertEquals(
		  "timelines/list.json",
		  persister.pathFor(new Timelines())
		);
	}
	
	@Test
	public void pathForWithTimeline() {
		assertEquals(
		  "timelines/identifier.json",
		  persister.pathFor(new Timeline(null, "identifier"))
		);
	}
	
	@Test
	public void pathForUsers() {
		assertEquals("users/list.json", persister.pathForUsers());
	}
	
	@Test
	public void pathForUser() {
		assertEquals("users/identifier.json", persister.pathForUser("identifier"));
	}

	@Test
	public void pathForTimelines() {
		assertEquals("timelines/list.json", persister.pathForTimelines());
	}
	
	@Test
	public void pathForTimeline() {
		assertEquals("timelines/identifier.json", persister.pathForTimeline("identifier"));
	}
}
