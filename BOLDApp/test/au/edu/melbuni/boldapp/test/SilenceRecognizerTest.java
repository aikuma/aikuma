package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.MaximumRecognizer;

@RunWith(CustomTestRunner.class)
public class SilenceRecognizerTest {
	
	private MaximumRecognizer defaultSilenceRecognizer;
	private MaximumRecognizer specificSilenceRecognizer;

	@Before
	public void setUp() throws Exception {
		this.defaultSilenceRecognizer = new MaximumRecognizer();
		this.specificSilenceRecognizer = new MaximumRecognizer(2);
	}
	
	@Test
	public void isSilence1() {
		assertTrue(defaultSilenceRecognizer.isSilence(0));
		assertTrue(specificSilenceRecognizer.isSilence(0));
	}
	
	@Test
	public void isSilence2() {
		assertFalse(defaultSilenceRecognizer.isSilence(10000));
		assertTrue(specificSilenceRecognizer.isSilence(10000));
	}
	
	@Test
	public void isSilence3() {
		assertFalse(defaultSilenceRecognizer.isSilence(17000));
		assertFalse(specificSilenceRecognizer.isSilence(17000));
	}

}
