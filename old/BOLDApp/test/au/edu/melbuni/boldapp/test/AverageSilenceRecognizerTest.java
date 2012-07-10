package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.AverageRecognizer;

@RunWith(CustomTestRunner.class)
public class AverageSilenceRecognizerTest {
	
	private AverageRecognizer defaultSilenceRecognizer;
	private AverageRecognizer specificSilenceRecognizer;

	@Before
	public void setUp() throws Exception {
		this.defaultSilenceRecognizer = new AverageRecognizer();
		this.specificSilenceRecognizer = new AverageRecognizer(2, 2);
	}
	
	@Test
	public void isSilence1() {
		assertTrue(defaultSilenceRecognizer.isSilence(new short[] {0, 0}));
		assertTrue(specificSilenceRecognizer.isSilence(new short[] {0, 0}));
	}
	
	@Test
	public void isSilence2() {
		assertFalse(defaultSilenceRecognizer.isSilence(new short[] {5000, 15000, -10000}));
		assertTrue(specificSilenceRecognizer.isSilence(new short[] {5000, 15000, -10000}));
	}
	
	@Test
	public void isSilence3() {
		assertFalse(defaultSilenceRecognizer.isSilence(new short[] {10000, 25000, -10000}));
		assertFalse(specificSilenceRecognizer.isSilence(new short[] {10000, 25000, -10000}));
	}

}
