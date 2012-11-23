//package au.edu.unimelb.boldapp.test;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;

import au.edu.unimelb.boldapp.User;

//@RunWith(CustomTestRunner.class)
public class UserTest {
	private User user;

	@Before
	public void setUp() throws Exception {
		this.user = new User(
				UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
				"Some name");
	}

	@Test
	public void getName() {
		assertEquals("Some name", user.getName());
	}
}
