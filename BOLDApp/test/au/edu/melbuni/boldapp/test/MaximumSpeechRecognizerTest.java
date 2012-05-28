package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.MaximumRecognizer;

@RunWith(CustomTestRunner.class)
public class MaximumSpeechRecognizerTest {
	
	private MaximumRecognizer defaultSpeechRecognizer;
	private MaximumRecognizer specificSpeechRecognizer;

	@Before
	public void setUp() throws Exception {
		this.defaultSpeechRecognizer = new MaximumRecognizer();
		this.specificSpeechRecognizer = new MaximumRecognizer(2, 2);
	}
	
	@Test
	public void isSpeech1() {
		assertFalse(defaultSpeechRecognizer.isSpeech(new short[] {0}));
		assertFalse(specificSpeechRecognizer.isSpeech(new short[] {0}));
	}
	
	@Test
	public void isSpeech2() {
		assertTrue(defaultSpeechRecognizer.isSpeech(new short[] {10000}));
		assertFalse(specificSpeechRecognizer.isSpeech(new short[] {10000}));
	}
	
	@Test
	public void isSpeech3() {
		assertTrue(defaultSpeechRecognizer.isSpeech(new short[] {17000}));
		assertTrue(specificSpeechRecognizer.isSpeech(new short[] {17000}));
	}

}
