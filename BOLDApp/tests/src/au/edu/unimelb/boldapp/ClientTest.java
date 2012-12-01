package au.edu.unimelb.boldapp;

import au.edu.unimelb.boldapp.sync.Client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

public class ClientTest extends TestCase {
	Client client;

	@Override
	public void setUp() {
		client = new Client();
		client.setClientBaseDir("/mnt/sdcard/");
		//client.setServerBaseDir("/part0/share/");
	}

	/*
	public void testLoginLogout() {
		assertEquals(true, client.login("192.168.1.1", "admin", "admin"));
		assertEquals(true, client.logout());

		assertEquals(false, client.login("192.168.1.1", "admin", "wrongpass"));
		assertEquals(false, client.logout());
	}
	*/

	/*
	public void testDeleteDirectory() {
		assertEquals(true, client.login("192.168.1.1", "admin", "admin"));
		assertTrue(client.pushDirectory("okidoke"));
		assertTrue(client.deleteServerDir("okidoke"));
		assertEquals(true, client.logout());
	}
	*/

	/*
	public void testPushPull() {
		// Make the directory
		File boldCopyDir = new File("/mnt/sdcard/bold_copy");

		// Clear all the files in the directory
		try {
			FileUtils.deleteDirectory(boldCopyDir);
		} catch (IOException e) {
			assertTrue(false);
		}
		boldCopyDir.mkdirs();

		File boldDir = new File("/mnt/sdcard/bold");

		// Copy bold data to bold_copy
		try {
			FileUtils.copyDirectory(boldDir, boldCopyDir);
		} catch (Exception e) {
			assertTrue(false);
		}

		assertEquals(true, client.login("192.168.1.1", "admin", "admin"));

		assertEquals(true, client.pushDirectory("bold_copy"));

		// Clear all the files in the directory
		try {
			FileUtils.deleteDirectory(boldCopyDir);
		} catch (IOException e) {
			assertTrue(false);
		}
		boldCopyDir.mkdirs();

		// Pull files from server
		assertEquals(true, client.pullDirectory("bold_copy"));

		client.deleteServerDir("bold_copy");

		assertEquals(true, client.logout());

	}
	*/

	public void testFindServerBaseDir() {
		assertEquals(true, client.login("192.168.1.1", "admin", "admin"));
		//Log.i("ftp", "answer: " + client.findServerBaseDir());
		assertEquals("/part0/share/bold", client.getServerBaseDir());
		assertEquals(true, client.logout());
	}

/*
	@Test
	public void recursivePushPull() {
		// Make the directory
		File boldDir = new File("tests/bold");
		boldDir.mkdir();

		File boldExampleDir = new File("tests/bold_example");

		try {
			FileUtils.copyDirectory(boldExampleDir, boldDir);
		} catch (Exception e) {
		}

		assertEquals(true, client.login("192.168.1.1", "admin", "admin"));
		assertEquals(true, client.push());
		// Clear all the files in the directory
		for (File file : boldDir.listFiles()) {
			file.delete();
		}
		System.out.println(" " + Arrays.asList(boldDir.listFiles()));
		assertEquals(true, client.pull());
		assertEquals(true, client.logout());
	}
*/

	//@Test
	//public void sync() {
	//	// Make the directory
	//	File boldDir = new File("tests/bold");
	//	boldDir.mkdir();

	//	File boldExampleDir = new File("tests/bold_example");

	//	assertEquals(true, client.login("192.168.1.1", "admin", "admin"));
	//	assertEquals(true, client.sync());
	//	assertEquals(true, client.logout());
	//}
}
