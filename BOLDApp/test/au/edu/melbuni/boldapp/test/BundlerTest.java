package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.persisters.Persister;

@RunWith(CustomTestRunner.class)
public class BundlerTest {

	@Test
	public void getBasePath() {
		assertEquals("./mnt/sdcard/bold/", Persister.getBasePath());
	}

}
