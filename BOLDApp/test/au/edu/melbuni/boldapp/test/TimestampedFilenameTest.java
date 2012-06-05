package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.TimestampedFilename;

@RunWith(CustomTestRunner.class)
public class TimestampedFilenameTest {
	
	@Test
	public void worksWithExtensionsWithDigits() {
		assertTrue(TimestampedFilename.getFilenameFor("hello.123").matches(
				"hello-\\d{10,}\\.123"));
	}
	
	@Test
	public void hasTenOrMoreDigits() {
		assertTrue(TimestampedFilename.getFilenameFor("hello.wav").matches(
				"hello-\\d{10,}\\.wav"));
	}

}
