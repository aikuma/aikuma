package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;

@RunWith(CustomTestRunner.class)
public class TimelineTest {
	
	Timeline timeline;
	User user;
	Users users;
	
	@Before
	public void setUp() throws Exception {
		users = new Users();
		user = new User("Some Name");
		users.add(user);
		users.save(new JSONPersister());
		
		timeline = new Timeline();
		timeline.setUser(user);
		timeline.setDate(new Date(0));
	}
	
	@Test
	public void getUser() {
		assertEquals(user, timeline.getUser());
	}
	
	@Test
	@SuppressWarnings("deprecation")
	public void fromJSON() {
		Persister persister = new JSONPersister();
		Map<String, Object> hash = persister.fromJSON("{\"id\":\"" + timeline.getIdentifier() + "\"\"date\":\"1 Jan 1970 01:00:00 GMT\",\"user_id\":\"" + user.getIdentifier() + "\"}");
		Timeline loaded = Timeline.fromHash(users, hash);
		assertEquals(timeline.getIdentifier(), loaded.getIdentifier());
		assertEquals("1 Jan 1970 01:00:00 GMT", loaded.getDate().toGMTString());
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
		  "{\"id\":\"" + timeline.getIdentifier() + "\",\"date\":\"1 Jan 1970 00:00:00 GMT\",\"user_id\":\"" + user.getIdentifier() + "\"}",
		  new JSONPersister().toJSON(timeline.toHash())
		);
	}
}
