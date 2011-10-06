package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.BoldActivity;
import au.edu.melbuni.boldapp.BoldApplication;
import au.edu.melbuni.boldapp.Timeline;
import au.edu.melbuni.boldapp.Timelines;
import au.edu.melbuni.boldapp.User;
import au.edu.melbuni.boldapp.Users;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BoldApplicationTest {

	static Timeline timeline;
	static BoldApplication application;
	
	@Before
	public void setUp() throws Exception {
		BoldApplicationTest.timeline    = new Timeline(new BoldActivity(), "identifier");
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
		
		@Before
		public void setUp() throws Exception {
			super.setUp();
			
			users = new Users();
			
			specificUser = new User();
			
			users.add(specificUser);
			users.add(new User());
			
			Iterator<User> usersIterator = users.iterator();
			while (usersIterator.hasNext()) {
				application.addUser(usersIterator.next());
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
		
	}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithoutUsers extends BoldApplicationTest {
		
		@Test
		public void getUsers() {
			assertEquals(new Users().users, application.getUsers().users);
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
			
			current = new Timeline(new BoldActivity(), "current");
			application.setCurrentTimeline(timeline);
			
			timelines = new Timelines();
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