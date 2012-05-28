package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.MaximumSpeechRecognizer;

@RunWith(CustomTestRunner.class)
public class SpeechRecognizerTest {
	
	private MaximumSpeechRecognizer defaultSpeechRecognizer;
	private MaximumSpeechRecognizer specificSpeechRecognizer;

	@Before
	public void setUp() throws Exception {
		this.defaultSpeechRecognizer = new MaximumSpeechRecognizer();
		this.specificSpeechRecognizer = new MaximumSpeechRecognizer(2);
	}
	
	@Test
	public void isSpeech1() {
		assertFalse(defaultSpeechRecognizer.isSpeech(0));
		assertFalse(specificSpeechRecognizer.isSpeech(0));
	}
	
	@Test
	public void isSpeech2() {
		assertTrue(defaultSpeechRecognizer.isSpeech(10000));
		assertFalse(specificSpeechRecognizer.isSpeech(10000));
	}
	
	@Test
	public void isSpeech3() {
		assertTrue(defaultSpeechRecognizer.isSpeech(17000));
		assertTrue(specificSpeechRecognizer.isSpeech(17000));
	}

}
