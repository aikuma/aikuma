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
		BoldApplicationTest.timeline    = new Timeline("test_");
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
		
		User specificUser;
		Timeline specificTimeline;
		
		@Before
		public void setUp() throws Exception {
			super.setUp();
			
			// Users.
			//
			specificUser = new User();
			User unspecifiedUser = new User();
			
			application.addUser(specificUser);
			application.addUser(unspecifiedUser);
			
			application.setCurrentUser(specificUser);
			
			// Timelines.
			//
			specificTimeline = new Timeline("test_");
			Timeline unspecifiedTimeline = new Timeline("test_");
			
			specificTimeline.setUser(specificUser);
			unspecifiedTimeline.setUser(unspecifiedUser);
			
			application.addTimeline(specificTimeline);
			application.addTimeline(unspecifiedTimeline);
		}
		
		@Test
		public void addUser() {
			assertEquals(false, application.addUser(specificUser));
		}
		
		@Test
		public void saveAndLoad() {
			application.save();
			application.load();
			
			// Basics.
			//
			assertNotNull(application.getCurrentUser());
			assertEquals(specificUser.getIdentifier(), application.getCurrentUser().getIdentifier());
			assertNotNull(application.getUsers());
			assertEquals(2, application.getUsers().size());
			
			assertNotNull(application.getTimelines());
			assertEquals(2, application.getTimelines().size());
			
			// Relations.
			//
			assertEquals(specificUser, application.getTimelines().get(0).getUser());
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
			
			current = new Timeline("current_");
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