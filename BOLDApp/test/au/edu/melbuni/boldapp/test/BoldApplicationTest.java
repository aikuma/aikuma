package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.BoldApplication;
import au.edu.melbuni.boldapp.models.AllUsers;
import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;
import au.edu.melbuni.boldapp.persisters.JSONPersister;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BoldApplicationTest {

	static Timeline timeline;
	static BoldApplication application;
	
	@Before
	public void setUp() throws Exception {
		timeline    = new Timeline();
		application = new BoldApplication();
		
		AllUsers.clear();
		application.getTimelines().clear();
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
		Segment specificSegment;
		
		@Before
		public void setUp() throws Exception {
			super.setUp();
			
			// Users.
			//
			specificUser         = new User();
			User unspecifiedUser = new User();
			
			application.addUser(specificUser);
			application.addUser(unspecifiedUser);
			
			application.setCurrentUser(specificUser);
			
			// Timelines.
			//
			specificTimeline             = new Timeline();
			Timeline unspecifiedTimeline = new Timeline();
			
			specificTimeline.setUser(specificUser);
			unspecifiedTimeline.setUser(unspecifiedUser);
			
			// Segments.
			//
			Segment unspecificSegment = new Segment("0");
			specificSegment           = new Segment("1");
			
			specificTimeline.getSegments().add(unspecificSegment);
			specificTimeline.getSegments().add(specificSegment);
		}
		
		@Test
		public void addUser() {
			assertEquals(false, application.addUser(specificUser));
		}
		
		@Test
		public void saveAndLoad() {
//			System.out.println(specificTimeline.getUser());
//			System.out.println(specificUser);

			new JSONPersister().deleteAll();
			
			application.save();
			try {
				Thread.sleep(1000); // TODO Why do I need to wait this long?
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			application.load();
			
//			System.out.println(application.getCurrentUser());
//			System.out.println(application.getTimelines().first().getUser());
			
			// Basics.
			//
			assertNotNull(application.getCurrentUser());
			assertEquals(specificUser.getIdentifier(), application.getCurrentUser().getIdentifier());
			assertNotNull(application.getUsers());
			assertEquals(2, application.getUsers().size());
			
			assertNotNull(application.getTimelines());
			assertEquals(2, application.getTimelines().size());
			
			Timeline loadedSpecificTimeline = application.getTimelines().first();
			assertEquals("0", loadedSpecificTimeline.getSegments().get(0).getIdentifier());
			assertEquals(specificSegment, loadedSpecificTimeline.getSegments().get(1));
			
			// Relations.
			//
			assertEquals(specificUser, application.getTimelines().first().getUser());
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
			
			application.getTimelines().clear();
			
			current = new Timeline();
			application.setCurrentTimeline(timeline);
			
			timelines = new Timelines();
			timelines.add(current);
			timelines.add(timeline);
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