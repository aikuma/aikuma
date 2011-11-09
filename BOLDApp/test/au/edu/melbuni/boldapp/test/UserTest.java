package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.persisters.JSONPersister;

@RunWith(CustomTestRunner.class)
public class UserTest {
	
	private User user;

	@Before
	public void setUp() throws Exception {
		this.user = new User("Some Name", UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
	}
	
	@Test
	public void fromJSON() {
		User loaded = User.fromHash(new JSONPersister().fromJSON("{\"name\":\"Some Name\",\"uuid\":\"f47ac10b-58cc-4372-a567-0e02b2c3d479\"}"));
		assertEquals("Some Name", loaded.name);
		assertEquals("f47ac10b-58cc-4372-a567-0e02b2c3d479", loaded.getIdentifier());
	}
	@Test
	public void fromJSONWithoutData() {
		User loaded = User.fromHash(new JSONPersister().fromJSON("{}"));
		assertEquals("", loaded.name);
		assertNotNull(loaded.getIdentifier());
	}
	
	@Test
	public void toJSON() {
		assertEquals("{\"name\":\"Some Name\",\"uuid\":\"f47ac10b-58cc-4372-a567-0e02b2c3d479\"}", user.toJSON());
	}
	
	@Test
	public void getIndentifierString() {
		assertEquals("f47ac10b-58cc-4372-a567-0e02b2c3d479", user.getIdentifier());
	}

	@Test
	public void getNameKey() {
		assertEquals("users/f47ac10b-58cc-4372-a567-0e02b2c3d479/name", user.getNameKey());
	}
	
	@Test
	public void getProfileImagePath() {
		assertEquals("./mnt/sdcard/bold/users/profile_f47ac10b-58cc-4372-a567-0e02b2c3d479.png", user.getProfileImagePath());
	}
	
	@Test
	public void getUUIDString() {
		assertEquals("f47ac10b-58cc-4372-a567-0e02b2c3d479", user.getIdentifier());
	}
	
	@Test
	public void hasProfileImageWithoutHavingOne() {
		assertEquals(false, user.hasProfileImage());
	}
	
//	@Test
//	public void hasProfileImageWithHavingOne() {
//		String fakePath = new File(".").getAbsolutePath();
//		File profileImage = new File(user.getProfileImagePath());
//		try {
//			profileImage.createNewFile();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		assertEquals(true, user.hasProfileImage());
//		
//		profileImage.delete();
//	}

}
