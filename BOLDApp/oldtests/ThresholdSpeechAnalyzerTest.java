import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import au.edu.unimelb.aikuma.audio.AudioHandler;
import au.edu.unimelb.aikuma.audio.analyzers.ThresholdSpeechAnalyzer;

public class ThresholdSpeechAnalyzerTest {
	
	private class TestAudioHandler implements AudioHandler {
		
		private int silenceTriggered;
		private int audioTriggered;
		
		private short[] audioBuffer = new short[0];
		
		public TestAudioHandler() {
			reset();
		}
		
		@Override
		public void silenceTriggered(short[] buffer, boolean justChanged) {
			this.silenceTriggered += 1;
		}
		
		@Override
		public void audioTriggered(short[] buffer, boolean justChanged) {
			this.audioTriggered += 1;
			addToAudioBuffer(buffer);
		}
		
		public void reset() {
			this.silenceTriggered = 0;
			this.audioTriggered = 0;
		}
		
		protected void addToAudioBuffer(short[] buffer) {
			int offset = audioBuffer.length;

			// Create a new buffer.
			//
			audioBuffer = Arrays.copyOf(audioBuffer, audioBuffer.length
					+ buffer.length);

			// Copy the buffer to avoid reference problems.
			//
			short[] copiedBuffer = Arrays.copyOf(buffer, buffer.length);

			// Add the copied current buffer onto the end.
			//
			for (int i = 0; i < buffer.length; i++) {
				audioBuffer[i + offset] = copiedBuffer[i];
			}
		}
		
	}
	
	private ThresholdSpeechAnalyzer defaultSpeechAnalyzer;
	private ThresholdSpeechAnalyzer specificSpeechAnalyzer;
	
	private TestAudioHandler testClass;

	@Before
	public void setUp() throws Exception {
		this.defaultSpeechAnalyzer = new ThresholdSpeechAnalyzer();
		this.specificSpeechAnalyzer = new ThresholdSpeechAnalyzer(2, 2);
		
		this.testClass = new TestAudioHandler();
	}
	
	@Test
	public void defaultAnalyze1() {
		defaultSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		
		assertEquals(0, testClass.silenceTriggered);
	}
	
	@Test
	public void defaultAnalyze2() {
		for (int i = 0; i < 4; i++) {
			defaultSpeechAnalyzer.analyze(testClass, new short[]{6000});
		}
		
		for (int i = 0; i < 89; i++) {
			defaultSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		}
		
		assertEquals(1, testClass.silenceTriggered);
	}
	
	@Test
	public void defaultAnalyze3() {
		defaultSpeechAnalyzer.analyze(testClass, new short[]{6000});
		
		assertEquals(0, testClass.audioTriggered);
	}
	
	@Test
	public void defaultAnalyze4() {
		for (int i = 0; i < 4; i++) {
			defaultSpeechAnalyzer.analyze(testClass, new short[]{6000});
		}
		
		assertEquals(2, testClass.audioTriggered);
	}
	
	@Test
	public void specificAnalyze1() {
		specificSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		
		assertEquals(0, testClass.silenceTriggered);
	}
	
	@Test
	public void specificAnalyze2() {
		for (int i = 0; i < 3; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		}
		
		for (int i = 0; i < 3; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		}
		
		assertEquals(1, testClass.silenceTriggered);
	}
	
	@Test
	public void specificAnalyze3() {
		specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		
		assertEquals(0, testClass.audioTriggered);
	}
	
	@Test
	public void specificAnalyze4() {
		for (int i = 0; i < 3; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		}
		
		assertEquals(2, testClass.audioTriggered);
	}
	
	@Test
	public void specificComplexAnalyze1() {
		
		// Lots of silence.
		//
		for (int i = 0; i < 20; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{(short) i});
		}
		
		for (short i = 0; i < 4; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{(short) ((short) i+6000)});
		}
		
		// This silence does not trigger silence.
		//
		for (int i = 0; i < 2; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{(short) i});
		}

		for (short i = 0; i < 3; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{(short) ((short) i+6000)});
		}
		
		// Triggers silence.
		//
		for (int i = 0; i < 3; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{(short) i});
		}
		
		assertEquals(1, testClass.silenceTriggered);
		assertEquals(10, testClass.audioTriggered);
		
		short[] comparison = new short[]{
			0, 0, 0, 0, 0, 0, 0, 0,
			14, 15, 16, 17, 18, 19,
			6000, 6001, 6002, 6003,
			0, 1,
			6000, 6001, 6002,
			0, 1
		};
		for (int i = 0; i < testClass.audioBuffer.length; i++) {
			assertEquals(comparison[i], testClass.audioBuffer[i]);
		}
	}
	
	@Test
	public void addToOnsetBufferWithEmptyBuffer() {
		specificSpeechAnalyzer.addToOnsetBuffer(new short[] {});
		assertEquals(0, specificSpeechAnalyzer.getOnsetBuffer().length); 
	}
	
//	@Test
//	public void addToOnsetBufferWithNullBuffer() {
//		specificSpeechAnalyzer.addToOnsetBuffer(null);
//		assertEquals(0, specificSpeechAnalyzer.getOnsetBuffer().length); 
//	}
	
	@Test
	public void addToOnsetBufferWithSmallAndEmptyBuffer() {
		specificSpeechAnalyzer.addToOnsetBuffer(new short[] {1, 2, 3});
		specificSpeechAnalyzer.addToOnsetBuffer(new short[] {});
		assertEquals(3, specificSpeechAnalyzer.getOnsetBuffer().length); 
	}
	
//	@Test
//	public void addToOnsetBufferWithSmallAndNullBuffer() {
//		specificSpeechAnalyzer.addToOnsetBuffer(new short[] {1, 2, 3});
//		specificSpeechAnalyzer.addToOnsetBuffer(null);
//		assertEquals(3, specificSpeechAnalyzer.getOnsetBuffer().length); 
//	}
}
