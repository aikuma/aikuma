package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.filefilters.UUIDFileFilter;

@RunWith(CustomTestRunner.class)
public class UUIDFileFilterTest {
	
	FileFilter filter;
	
	@Before
	public void setUp() throws Exception {
		filter = new UUIDFileFilter();
	}
	
	@Test
	public void acceptWithCorrectName() {
		assertTrue(filter.accept(new File("12345678-1234-1234-1234-123456789012.json")));
	}
	
	@Test
	public void acceptWithQuiteWrongName() {
		assertFalse(filter.accept(new File("12345678-1234-134-1234-123456789012.json")));
	}
	
	@Test
	public void acceptWithTotallyWrongName() {
		assertFalse(filter.accept(new File("totally-wrong.txt")));
	}
	
}
