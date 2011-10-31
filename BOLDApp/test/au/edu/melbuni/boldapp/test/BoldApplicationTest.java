package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.BoldApplication;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BoldApplicationTest {

	static Timeline timeline;
	static BoldApplication application;
	
	@Before
	public void setUp() throws Exception {
		BoldApplicationTest.timeline    = new Timeline();
		BoldApplicationTest.application = new BoldApplication();
	}
	
	@Test
	public void makeTestRunnerHappy() {}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithCurrentUser extends BoldApplicationTest {
		
		User currentUser;
		
		@Before
		public void setUp() throws Exception {
			super.setUp();
			
			currentUser = new User();
			
			application.setCurrentUser(currentUser);
		}
		
		@Test
		public void getCurrentUserWithCurrentUser() {
			assertEquals(currentUser, application.getCurrentUser());
		}
		
	}
	
	@RunWith(RobolectricTestRunner.class)
	public class WithoutCurrentUser extends BoldApplicationTest {
		
		@Test
		public void getCurrentUserWithoutCurrentUser() {
			assertNotNull(application.getCurrentUser());
		}
		
	}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithUsers extends BoldApplicationTest {
		
		Users users;
		User specificUser;
		
		Timelines timelines;
		Timeline specificTimeline;
		
		@Before
		public void setUp() throws Exception {
			super.setUp();
			
			// Users.
			//
			specificUser = new User();
			users = new Users();
			users.add(specificUser);
			users.add(new User());
			application.setCurrentUser(specificUser);
			Iterator<User> usersIterator = users.iterator();
			while (usersIterator.hasNext()) {
				application.addUser(usersIterator.next());
			}
			
			// Timelines.
			//
			timelines = new Timelines();
			specificTimeline = new Timeline("test_");
			timelines.add(specificTimeline);
			timelines.add(new Timeline("test_"));
			Iterator<Timeline> timelinesIterator = timelines.iterator();
			while (timelinesIterator.hasNext()) {
				application.addTimeline(timelinesIterator.next());
			}
		}
		
		@Test
		public void getUsers() {
			assertEquals(users.users, application.getUsers().users);
		}
		
		@Test
		public void addUser() {
			assertEquals(false, application.addUser(specificUser));
		}
		
		@Test
		public void saveAndLoad() {
			application.save();
			application.load();
			
			assertNotNull(application.getCurrentUser());
			assertEquals(specificUser.getIdentifierString(), application.getCurrentUser().getIdentifierString());
			assertNotNull(application.getUsers());
			assertEquals(2, application.getUsers().size());
			
			assertNotNull(application.getTimelines());
			assertEquals(2, application.getTimelines().size());
		}
		
	}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithoutUsers extends BoldApplicationTest {
		
		@Test
		public void getUsers() {
			assertEquals(new Users(new ArrayList<User>()).users, application.getUsers().users);
		}
		
		@Test
		public void addUser() {
			assertEquals(true, application.addUser(new User()));
		}
		
	}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithoutCurrentTimelineOrTimelines extends BoldApplicationTest {
		
		@Test
		public void getCurrentTimeline() {
			assertNull(application.getCurrentTimeline());
		}
		
		@Test
		public void getTimelines() {
			assertEquals(0, application.getTimelines().size());
		}
		
		@Test
		public void addTimelineWithItNotBeingAdded() {
			assertEquals(true, application.addTimeline(timeline));
		}
		
	}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithCurrentTimelineAndTimelines extends BoldApplicationTest {
		
		Timeline current;
		Timelines timelines;
		
		@Before
		public void setUp() throws Exception {
			super.setUp();
			
			current = new Timeline();
			application.setCurrentTimeline(timeline);
			
			timelines = new Timelines("current");
			timelines.add(current);
			timelines.add(timeline);
			
			Iterator<Timeline> timelinesIterator = timelines.iterator();
			while (timelinesIterator.hasNext()) {
				application.addTimeline(timelinesIterator.next());
			}
		}
		
		@Test
		public void getCurrentTimeline() {
			assertEquals(timeline, application.getCurrentTimeline());
		}
		
		@Test
		public void getTimelines() {
			assertEquals(timelines.size(), application.getTimelines().size());
		}
		
		@Test
		public void addTimeline() {
			assertEquals(false, application.addTimeline(timeline));
		}
		
	}
	
}