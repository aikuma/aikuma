import au.edu.unimelb.boldapp.sync.client;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FTPClientTest {
	FTPClient client;

	@Before
	public void setUp() {
		client = new FTPClient();
	}

	@Test
	public void loginLogout() {
		assertEquals(true, client.login("192.168.1.1", "admin", "boldbold");
		assertEquals(true, client.logout();

		assertEquals(false, client.login("192.168.1.1", "admin", "wrongpass");
		assertEquals(false, client.logout();
	}
}
