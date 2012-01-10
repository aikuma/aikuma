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
	Segment segment;

	@Before
	public void setUp() throws Exception {
		ftp = new FTPClient("localhost");

		// ftp.login();
		// ftp.deleteAll();
		// ftp.logout();

		if (user == null) {
			user = new User("Test User",
					UUID.fromString("12345678-1234-1234-1234-123456789012"));
			user.save(new JSONPersister());
		}

		if (timeline == null) {
			timeline = new Timeline(
					UUID.fromString("12345678-1234-1234-1234-123456789012"));
			timeline.setUser(user);
			
			if (segment == null) {
				segment = new Segment("test");
			}
			timeline.getSegments().add(segment);

			timeline.save(new JSONPersister());
		}
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
		assertEquals("test", ftp.getBaseDir());
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
		ftp.login();
		assertTrue(ftp.cdToBase());
		ftp.logout();
	}

	@Test
	public void cdToUsers() {
		ftp.login();
		assertTrue(ftp.cdToUsers());
		ftp.logout();
	}

	@Test
	public void postUser() {
		ftp.login();
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
		ftp.login();
		assertTrue(ftp.cdToTimelines());
		ftp.logout();
	}

	@Test
	public void postTimeline() {
		ftp.login();
		assertTrue(ftp.post(timeline));
		ftp.logout();
	}

	@Test
	public void postSegments() {
		ftp.login();
		for (Segment segment : timeline.getSegments()) {
			assertTrue(ftp.post(segment, timeline.getIdentifier()));
		}
		ftp.logout();
	}

	@Test
	public void cdToExistingSegments() {
		ftp.login();
		assertTrue(ftp.cdToSegments(timeline.getIdentifier()));
		ftp.logout();
	}

	@Test
	public void cdToNonexistingSegments() {
		ftp.login();
		assertFalse(ftp.cdToSegments("nonExistingtimelineId"));
		ftp.logout();
	}
	
	@Test
	public void getUser() {
		ftp.login();
		assertEquals(
			user,
			ftp.getUser(user.getIdentifier())
		);
		ftp.logout();
	}
	
	@Test
	public void getTimeline() {
		ftp.login();
		Users users = new Users();
		users.add(user);
		assertEquals(
			timeline,
			ftp.getTimeline(
				timeline.getIdentifier(),
				timeline.getUser().getIdentifier(),
				users
			)
		);
		ftp.logout();
	}
	
	@Test
	public void getSegment() {
		ftp.login();
		assertEquals(
			segment,
			ftp.getSegment(segment.getIdentifier(), timeline.getIdentifier())
		);
		ftp.logout();
	}

	@Test
	public void getSegmentIds() {
		ftp.login();
		List<String> expected = new ArrayList<String>();
		expected.add(segment.getIdentifier());
		assertEquals(
			expected,
			ftp.getSegmentIds(timeline.getIdentifier())
		);
		ftp.logout();
	}
	
	@Test
	public void doesExistUser() {
		ftp.login();
		assertTrue(ftp.doesExist(user));
		ftp.logout();
	}
	
	@Test
	public void doesExistNonExistingUser() {
		ftp.login();
		assertFalse(ftp.doesExist(new User()));
		ftp.logout();
	}
	
	@Test
	public void doesExistTimeline() {
		ftp.login();
		assertTrue(ftp.doesExist(timeline));
		ftp.logout();
	}
	
	@Test
	public void doesExistNonExistingTimeline() {
		ftp.login();
		assertFalse(ftp.doesExist(new Timeline()));
		ftp.logout();
	}
	
	@Test
	public void doesSegmentExist() {
		ftp.login();
		assertTrue(ftp.doesSegmentExist(timeline.getIdentifier(), segment.getIdentifier()));
		ftp.logout();
	}
	
	@Test
	public void getUserIds() {
		ftp.login();
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
		ftp.login();
		List<String> expected = new ArrayList<String>();
		expected.add(timeline.getIdentifier());
		assertEquals(
			expected,	
			ftp.getTimelineIds()
		);
		ftp.logout();
	}
	
}
