package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.Bundler;

@RunWith(CustomTestRunner.class)
public class BundlerTest {

	@Test
	public void getBasePath() {
		assertEquals("/mnt/sdcard/bold/", Bundler.getBasePath());
	}

}
