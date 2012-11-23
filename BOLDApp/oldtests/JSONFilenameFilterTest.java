//package au.edu.unimelb.boldapp.test;

import java.util.UUID;
import java.io.File;
import java.io.FilenameFilter;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.runner.RunWith;

import au.edu.unimelb.boldapp.JSONFilenameFilter;

//@RunWith(CustomTestRunner.class)
public class JSONFilenameFilterTest {
	private File dir;
	private FilenameFilter filenameFilter;

	@Before
	public void setUp() throws Exception {
		this.dir = new File("/");
		this.filenameFilter = new JSONFilenameFilter();
	}

	@Test
	public void regexAccept() {
		assertTrue(filenameFilter.accept(dir, "blah.json"));
		assertFalse(filenameFilter.accept(dir, "blah.wav"));
	}
}
