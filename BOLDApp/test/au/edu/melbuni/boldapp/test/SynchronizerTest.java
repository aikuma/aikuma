package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.Synchronizer;
import au.edu.melbuni.boldapp.clients.Client;
import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;

@RunWith(CustomTestRunner.class)
public class SynchronizerTest {
	
	Synchronizer synchronizer;
	
	@Before
	public void setUp() throws Exception {
//		synchronizer = new Synchronizer("http://some.server:1234");
		
		Client client = new SynchronizerMockServer("http://some.server:1234");
		synchronizer = new Synchronizer(client);
	}
	
	@Test
	public void difference() {
		List<String> larger = new ArrayList<String>();
		larger.add("thing1");
		larger.add("thing2");
		larger.add("thing3");
		
		List<String> smaller = new ArrayList<String>();
		smaller.add("thing2");
		
		assertFalse(synchronizer.difference(larger, smaller).isEmpty());
		
		List<String> expected = new ArrayList<String>();
		expected.add("thing1");
		expected.add("thing3");
		
		assertEquals(
		  expected,
		  synchronizer.difference(larger, smaller)
		);
	}
	
	@Test
	public void intersection() {
		List<String> larger = new ArrayList<String>();
		larger.add("thing1");
		larger.add("thing2");
		larger.add("thing3");
		larger.add("thing4");
		larger.add("thing5");
		
		List<String> smaller = new ArrayList<String>();
		smaller.add("thing2");
		smaller.add("thing3");
		
		assertFalse(synchronizer.difference(larger, smaller).isEmpty());
		
		List<String> expected = new ArrayList<String>();
		expected.add("thing2");
		expected.add("thing3");
		
		assertEquals(
		  expected,
		  synchronizer.intersection(larger, smaller)
		);
	}
	
	public class SynchronizerMockServer extends Client {

		public SynchronizerMockServer(String serverURI) {
			super(serverURI);
		}

		@Override
		public Object getClient() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URI getServerURI(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void lazilyInitializeClient() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean doesSegmentExist(String timelineId, String segmentId) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean doesTimelineExist(String timelineId) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean doesUserExist(String userId) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean post(Segment segment, String timelineId) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean post(Timeline timeline) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean post(User user) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void getSegments(String timelineId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public List<String> getSegmentIds(String timelineId) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Timeline getTimeline(String timelineId, Users users) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<String> getTimelineIds() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public User getUser(String userId) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<String> getUserIds() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}