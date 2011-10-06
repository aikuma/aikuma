package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.BoldActivity;
import au.edu.melbuni.boldapp.Timeline;
import au.edu.melbuni.boldapp.User;

@RunWith(CustomTestRunner.class)
public class TimelineTest {
	
	Timeline timeline;
	
	@Before
	public void setUp() throws Exception {
		timeline = new Timeline(new BoldActivity(), "some_identifier");
	}
	
	@Test
	public void getUserDefault() {
		assertNull(timeline.getUser());
	}
	
	@Test
	public void getUser() {
		User user = new User();
		
		timeline.setUser(user);
		
		assertEquals(user, timeline.getUser());
	}
}
