import au.edu.unimelb.boldapp.sync.Client;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClientTest {
	Client client;

	@Before
	public void setUp() {
		client = new Client();
		client.setClientWorkingDir("tests/bold");
		client.setServerWorkingDir("part0/share");

		File boldDir = new File("tests/bold");
		boldDir.mkdir();

		try {
			BufferedWriter out = new BufferedWriter(
					new FileWriter("tests/bold/test_file"));
			out.write("ok");
			out.close();
		} catch (Exception e) {
		}
	}

	@Test
	public void loginLogout() {
		assertEquals(true, client.login("192.168.1.1", "admin", "admin"));
		assertEquals(true, client.logout());

		assertEquals(false, client.login("192.168.1.1", "admin", "wrongpass"));
		assertEquals(false, client.logout());
	}

	@Test
	public void pushPull() {
		assertEquals(true, client.login("192.168.1.1", "admin", "admin"));

		try {
			client.push();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(true, client.logout());

	}

}
