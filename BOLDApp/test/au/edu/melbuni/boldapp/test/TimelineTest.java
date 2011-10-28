package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.BoldActivity;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;

@RunWith(CustomTestRunner.class)
public class TimelineTest {
	
	Timeline timeline;
	User user;
	
	@Before
	public void setUp() throws Exception {
		user = new User("Some Name", UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		timeline = new Timeline(new BoldActivity(), "some_identifier");
		timeline.setUser(user);
		timeline.setDate(new Date(0));
	}
	
	@Test
	public void getUser() {
		assertEquals(user, timeline.getUser());
	}
	
	@Test
	public void fromJSON() {
		Timeline loaded = Timeline.fromJSON("{\"identifier\":\"some_identifier\",\"date\":\"Thu Jan 01 01:00:00 CET 1970\",\"user_reference\":\"f47ac10b-58cc-4372-a567-0e02b2c3d479\"}");
		assertEquals("some_identifier", loaded.identifier);
		assertEquals(new Date(0), loaded.getDate());
	}
//	@Test
//	public void fromJSONWithoutData() {
//		User loaded = User.fromJSON("{}");
//		assertEquals("", loaded.name);
//		assertNotNull(loaded.uuid);
//	}
	
	@Test
	public void toJSON() {
		assertEquals(
		  "{\"identifier\":\"some_identifier\",\"date\":\"Thu Jan 01 01:00:00 CET 1970\",\"user_reference\":\"f47ac10b-58cc-4372-a567-0e02b2c3d479\"}",
		  timeline.toJSON()
		);
	}
}
