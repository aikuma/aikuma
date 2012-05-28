package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.MaximumRecognizer;

@RunWith(CustomTestRunner.class)
public class MaximumSilenceRecognizerTest {
	
	private MaximumRecognizer defaultSilenceRecognizer;
	private MaximumRecognizer specificSilenceRecognizer;

	@Before
	public void setUp() throws Exception {
		this.defaultSilenceRecognizer = new MaximumRecognizer();
		this.specificSilenceRecognizer = new MaximumRecognizer(2, 2);
	}
	
	@Test
	public void isSilence1() {
		assertTrue(defaultSilenceRecognizer.isSilence(new short[] {0}));
		assertTrue(specificSilenceRecognizer.isSilence(new short[] {0}));
	}
	
	@Test
	public void isSilence2() {
		assertFalse(defaultSilenceRecognizer.isSilence(new short[] {10000}));
		assertTrue(specificSilenceRecognizer.isSilence(new short[] {10000}));
	}
	
	@Test
	public void isSilence3() {
		assertFalse(defaultSilenceRecognizer.isSilence(new short[] {17000}));
		assertFalse(specificSilenceRecognizer.isSilence(new short[] {17000}));
	}

}
