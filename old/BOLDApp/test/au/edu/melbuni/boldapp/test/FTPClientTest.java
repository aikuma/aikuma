package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.clients.FTPClient;
import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;
import au.edu.melbuni.boldapp.persisters.JSONPersister;

// These tests actually need a local FTP server
// with anonymous access running.
//
// Note: These tests are dirty insofar as
// they depend on each other.
//
//
@RunWith(CustomTestRunner.class)
public class FTPClientTest {

	FTPClient ftp;
	User user;
	Timeline timeline;
	Segment segment1;
	Segment segment2;

	@Before
	public void setUp() throws Exception {
		ftp = new FTPClient("localhost");

		if (user == null) {
			user = new User("Test User",
					UUID.fromString("12345678-1234-1234-1234-123456789012"));
			user.setProfileImage("fakeimagedata");
			user.save(new JSONPersister());
		}

		if (timeline == null) {
			timeline = new Timeline(
					UUID.fromString("12345678-1234-1234-1234-123456789012"));
			timeline.setUser(user);
			
			if (segment1 == null) {
				segment1 = new Segment("test1");
			}
			timeline.getSegments().add(segment1);
			
			if (segment2 == null) {
				segment2 = new Segment("test2");
			}
			timeline.getSegments().add(segment2);

			timeline.save(new JSONPersister());
		}
	}
	
//	boolean deletedAll = false;
//	public void deleteAll() {
//		if (!deletedAll) {
//			ftp.login();
//			ftp.deleteAll();
//			ftp.logout();
//			deletedAll = true;
//		}
//	}
	
	@Test
	public void integrationPostRetrieveUser() {
		// No login needed.
		User user = new User("Test User",
				UUID.fromString("12345678-1234-1234-1234-123456789012"));
		user.setProfileImage("some_image_data");
		user.save(new JSONPersister());
		
		assertTrue(ftp.post(user));
		
		new JSONPersister().deleteAll();
		
		assertEquals(null, new JSONPersister().loadUser(user.getIdentifier()));
		
		User ftpUser = ftp.getUser(user.getIdentifier());
		assertEquals(user, ftpUser);
		assertEquals("some_image_data", ftpUser.getProfileImageData());
		
		User loadedUser = new JSONPersister().loadUser(user.getIdentifier());
		assertEquals(user, loadedUser);
		assertEquals("some_image_data", loadedUser.getProfileImageData());
		
		ftp.logout();
	}
	
	@Test
	public void integrationPostRetrieveTimelineAndSegments() {
		// No login needed.
		Timeline timeline = new Timeline(UUID.fromString("12345678-1234-1234-1234-123456789012"));
		Segment segment = new Segment("integrationPostRetrieveTimeline");
		timeline.getSegments().add(segment);
		timeline.setUser(user);
		
		timeline.save(new JSONPersister());
		assertTrue(ftp.post(timeline));
		
		new JSONPersister().deleteAll();
		
		ArrayList<User> users = new ArrayList<User>();
		users.add(user);
		
		assertEquals(null, new JSONPersister().loadTimeline(new Users(users), timeline.getIdentifier()));
		
		assertEquals(timeline, ftp.getTimeline(timeline.getIdentifier(), new Users(users)));
		assertEquals(segment, timeline.getSegments().get(0));
		
		assertEquals(timeline, new JSONPersister().loadTimeline(new Users(users), timeline.getIdentifier()));
		assertEquals(segment, timeline.getSegments().get(0));
		
		ftp.logout();
	}
	
	@Test
	public void getServerURI() {
		assertEquals("localhost", ftp.getServerURI("whatever").toString());
	}
	
	@Test
	public void lazilyInitializeClient() {
		assertNull(ftp.getClient());
		ftp.lazilyInitializeClient();
		assertNotNull(ftp.getClient());
	}

	@Test
	public void getBaseDir() {
		// Make sure that we're in the right directory.
		//
		assertEquals("test/", ftp.getBaseDir());
	}

	@Test
	public void login() {
		assertTrue(ftp.login());
		ftp.logout();
	}

	@Test
	public void logout() {
		ftp.login();
		assertTrue(ftp.logout());
	}

	@Test
	public void cdToBase() {
		// No login needed.
		assertTrue(ftp.cdToBase());
		ftp.logout();
	}

	@Test
	public void cdToUsers() {
		// No login needed.
		assertTrue(ftp.cdToUsers());
		ftp.logout();
	}

	@Test
	public void postUser() {
		// No login needed.
		User user = new User("Test User",
				UUID.fromString("12345678-1234-1234-1234-123456789012"));
		user.save(new JSONPersister());
		assertTrue(ftp.post(user));
		ftp.logout();
	}

	// @Test
	// public void doesExistUserWithUser() {
	// ftp.login();
	// assertTrue(ftp.doesExist(user));
	// ftp.logout();
	// }
	//
	// @Test
	// public void doesExistUserWithoutUser() {
	// ftp.login();
	// User notExisting = new User("Not Existing",
	// UUID.fromString("00000000-0000-0000-0000-000000000000"));
	// assertFalse(ftp.doesExist(notExisting));
	// ftp.logout();
	// }

	@Test
	public void cdToTimelines() {
		// No login needed.
		assertTrue(ftp.cdToTimelines());
		ftp.logout();
	}

	@Test
	public void postTimeline() {
		// No login needed.
		assertTrue(ftp.post(timeline));
		ftp.logout();
	}

	@Test
	public void postSegments() {
		// No login needed.
		for (Segment segment : timeline.getSegments()) {
			assertTrue(ftp.post(segment, timeline.getIdentifier()));
		}
		ftp.logout();
	}

	@Test
	public void cdToExistingSegments() {
		// No login needed.
		assertTrue(ftp.cdToSegments(timeline.getIdentifier()));
		ftp.logout();
	}

	@Test
	public void cdToNonexistingSegments() {
		// No login needed.
		assertFalse(ftp.cdToSegments("nonExistingtimelineId"));
		ftp.logout();
	}
	
	@Test
	public void getUser() {
		// No login needed.
		assertEquals(
			user,
			ftp.getUser(user.getIdentifier())
		);
		ftp.logout();
	}
	
	@Test
	public void getNonExistingUser() {
		// No login needed.
		assertEquals(
			null,
			ftp.getUser(new User().getIdentifier())
		);
		ftp.logout();
	}
	
	@Test
	public void getTimeline() {
		// No login needed.
		Users users = new Users();
		users.add(user);
		assertEquals(
			timeline,
			ftp.getTimeline(
				timeline.getIdentifier(),
				users
			)
		);
		ftp.logout();
	}
	
	@Test
	public void getNonExistingTimeline() {
		// No login needed.
		Users users = new Users();
		assertEquals(
			null,
			ftp.getTimeline(
				new Timeline().getIdentifier(),
				users
			)
		);
		ftp.logout();
	}
	
//	@Test
//	public void getSegment() {
//		// No login needed.
//		assertEquals(
//			segment1,
//			ftp.getSegment(segment1.getIdentifier(), timeline.getIdentifier())
//		);
//		ftp.logout();
//	}

//	@Test
//	public void getSegments() {
//		// No login needed.
//		assertEquals(
//			timeline.getSegments(),
//			ftp.getSegments(timeline.getIdentifier())
//		);
//		ftp.logout();
//	}
	
	@Test
	public void getSegmentIds() {
		// No login needed.
		List<String> expected = new ArrayList<String>();
		expected.add(segment1.getIdentifier());
		expected.add(segment2.getIdentifier());
		assertEquals(
			expected,
			ftp.getSegmentIds(timeline.getIdentifier())
		);
		ftp.logout();
	}
	
	@Test
	public void doesExistUser() {
		// No login needed.
		assertTrue(ftp.doesUserExist(user.getIdentifier()));
		ftp.logout();
	}
	
	@Test
	public void doesExistNonExistingUser() {
		// No login needed.
		assertFalse(ftp.doesUserExist(new User().getIdentifier()));
		ftp.logout();
	}
	
	@Test
	public void doesExistTimeline() {
		// No login needed.
		assertTrue(ftp.doesTimelineExist(timeline.getIdentifier()));
		ftp.logout();
	}
	
	@Test
	public void doesExistNonExistingTimeline() {
		// No login needed.
		assertFalse(ftp.doesTimelineExist(new Timeline().getIdentifier()));
		ftp.logout();
	}
	
	@Test
	public void doesSegmentExist() {
		// No login needed.
		assertTrue(ftp.doesSegmentExist(timeline.getIdentifier(), segment1.getIdentifier()));
		ftp.logout();
	}
	
	@Test
	public void getUserIds() {
		// No login needed.
		List<String> expected = new ArrayList<String>();
		expected.add(user.getIdentifier());
		assertEquals(
			expected,	
			ftp.getUserIds()
		);
		ftp.logout();
	}
	
	@Test
	public void getTimelineIds() {
		// No login needed.
		List<String> expected = new ArrayList<String>();
		expected.add(timeline.getIdentifier());
		assertEquals(
			expected,	
			ftp.getTimelineIds()
		);
		ftp.logout();
	}
	
}
