package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.AudioCommand;

@RunWith(CustomTestRunner.class)
public class AudioCommandTest {

	private AudioCommand audioCommand;

	@Before
	public void setUp() throws Exception {
		short[][] pattern = new short[][] { { 0, 100 }, { 3000, 6000 },
				{ 0, 100 } };

		this.audioCommand = new AudioCommand(pattern);
	}

	@Test
	public void isGoodPatternRecognized() {
		assertFalse(audioCommand.isRecognized((short) 50));
		assertFalse(audioCommand.isRecognized((short) 4500));
		assertTrue(audioCommand.isRecognized((short) 70));
	}
	
	@Test
	public void isWrongPatternRecognized() {
		assertFalse(audioCommand.isRecognized((short) 50));
		assertFalse(audioCommand.isRecognized((short) 100));
		assertFalse(audioCommand.isRecognized((short) 70));
	}
	
	@Test
	public void isLongPatternRecognized() {
		assertFalse(audioCommand.isRecognized((short) 50));
		assertFalse(audioCommand.isRecognized((short) 100));
		assertFalse(audioCommand.isRecognized((short) 70));
		assertFalse(audioCommand.isRecognized((short) 50));
		assertFalse(audioCommand.isRecognized((short) 100));
		assertFalse(audioCommand.isRecognized((short) 70));
		assertFalse(audioCommand.isRecognized((short) 50));
		assertFalse(audioCommand.isRecognized((short) 5000));
		assertTrue(audioCommand.isRecognized((short) 70));
	}

}
