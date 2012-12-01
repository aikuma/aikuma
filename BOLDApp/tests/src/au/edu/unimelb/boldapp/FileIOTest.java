package au.edu.unimelb.boldapp;

import java.io.File;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

public class FileIOTest extends TestCase {

	public void testGetAppRootPath() throws Exception {
		assertEquals(new File("/mnt/sdcard/bold"), FileIO.getAppRootPath());
	}

	public void testGetUsersPath() throws Exception {
		assertEquals(new File("/mnt/sdcard/bold/users"),
				FileIO.getUsersPath());
	}

	public void testGetImagesPath() throws Exception {
		assertEquals(new File("/mnt/sdcard/bold/images"),
				FileIO.getImagesPath());
	}

	public void testGetRecordingsPath() throws Exception {
		assertEquals(new File("/mnt/sdcard/bold/recordings"),
				FileIO.getRecordingsPath());
	}

	public void testWriteRead1() {
		assertTrue(FileIO.write("testdir/test1/test1", "hallo"));
		assertEquals("hallo", FileIO.read("/mnt/sdcard/bold/testdir/test1/test1"));
	}

	public void testWriteRead2() {
		assertTrue(FileIO.write("/mnt/sdcard/bold/testdir/test1/", "okies"));
		assertEquals("okies", FileIO.read("testdir/test1/"));
	}

	public void testWriteRead3() {
		assertTrue(FileIO.write("testdir/test3", "once upon\n a time\n"));
		assertEquals("once upon\n a time\n", FileIO.read("testdir/test3"));
	}

	public void testWriteRead4() {
		assertTrue(FileIO.write("testdir/test4", "once upon\n a time"));
		assertTrue(!"once upon\n a time\n"
				.equals(FileIO.read("testdir/test4")));
	}

	public void testWriteRead5() {
		assertTrue(FileIO.write(
				new File(FileIO.getAppRootPath(), "testdir/test1/test1"),
				"hallo"));
		assertEquals("hallo", FileIO.read(
				new File(FileIO.getAppRootPath(), "testdir/test1/test1")));
	}

	public void testLoadUsers() {
		assertTrue(FileIO.loadUsers() != null);
	}

	@Override
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(new File(FileIO.getAppRootPath(),
				"testdir"));
	}

}
