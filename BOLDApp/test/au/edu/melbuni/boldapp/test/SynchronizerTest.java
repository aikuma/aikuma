package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.Synchronizer;

@RunWith(CustomTestRunner.class)
public class SynchronizerTest {
	
	Synchronizer synchronizer;
	
	@Before
	public void setUp() throws Exception {
		synchronizer = new Synchronizer("http://some.server:1234");
	}
	
	@Test
	public void difference() {
		List<String> larger = new ArrayList<String>();
		larger.add("thing1");
		larger.add("thing2");
		larger.add("thing3");
		
		List<String> smaller = new ArrayList<String>();
		smaller.add("thing2");
		
		assertFalse(synchronizer.difference(larger, smaller).isEmpty());
		
		List<String> expected = new ArrayList<String>();
		expected.add("thing1");
		expected.add("thing3");
		
		assertEquals(
		  expected,
		  synchronizer.difference(larger, smaller)
		);
	}
	
}
