package au.edu.melbuni.boldapp.test;

import java.util.UUID;

import au.edu.melbuni.boldapp.User;
import junit.framework.TestCase;

public class UserTest extends TestCase {
	
	private User user;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.user = new User("Some Name", UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
	}

	public void testGetNameKey() {
		assertEquals("users/f47ac10b-58cc-4372-a567-0e02b2c3d479/name", this.user.getNameKey());
	}
	
	public void testGetProfileImagePath() {
		assertEquals("users/profile_f47ac10b-58cc-4372-a567-0e02b2c3d479.png", this.user.getProfileImagePath());
	}
	
	public void testGetUUIDString() {
		assertEquals("f47ac10b-58cc-4372-a567-0e02b2c3d479", this.user.getIdentifierString());
	}

}
