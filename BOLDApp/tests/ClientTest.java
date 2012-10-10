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
		client.setServerWorkingDir("/part0/share/bold");

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

		// Make the directory
		File boldDir = new File("tests/bold");
		boldDir.mkdir();

		// Write files to the directory
		try {
			BufferedWriter out = new BufferedWriter(
					new FileWriter("tests/bold/test_file"));
			out.write("ok");
			out.close();
		} catch (Exception e) {
		}

		assertEquals(true, client.login("192.168.1.1", "admin", "admin"));

		// Push files to server
		try {
			client.push();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Clear all the files in the directory
		for (File file : boldDir.listFiles()) {
			file.delete();
		}

		// Pull files from server
		try {
			client.pull();
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(true, client.logout());

	}

}
