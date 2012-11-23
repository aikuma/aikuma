import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import au.edu.unimelb.boldapp.User;
import au.edu.unimelb.boldapp.GlobalState;

public class GlobalStateTest {
	User user;
	@Before
	public void setUp() throws Exception {
		this.user = new User(
				UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
				"This user's name");
	}

	@Test
	public void currentUserTest() {
		GlobalState.setCurrentUser(user);
		assertEquals(user, GlobalState.getCurrentUser());
	}
}
