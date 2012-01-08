package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.Recorder;

@RunWith(CustomTestRunner.class)
public class RecorderTest {
	
	Recorder recorder;
	
	@Before
	public void setUp() throws Exception {
		recorder = new Recorder();
	}
	
	@Test
	public void prepareFile() {
		assertEquals(
		  "/Users/admin/temp/eclipse_workspace/BOLDApp/relative/filename.3gp", // TODO Make independent.
		  Recorder.prepareFile("relative/filename")
		);
	}
	
}