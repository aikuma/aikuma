import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import au.edu.unimelb.boldapp.audio.analyzers.ThresholdSpeechAnalyzer;

public class ThresholdSpeechAnalyzerTest {
	
	private class TestAudioHandler implements AudioHandler {
		
		private int silenceTriggered;
		private int speechTriggered;
		
		private short[] speechBuffer = new short[0];
		
		public TestAudioHandler() {
			reset();
		}
		
		@Override
		public void silenceTriggered(short[] buffer, boolean justChanged) {
			this.silenceTriggered += 1;
		}
		
		@Override
		public void speechTriggered(short[] buffer, boolean justChanged) {
			this.speechTriggered += 1;
			addToSpeechBuffer(buffer);
		}
		
		public void reset() {
			this.silenceTriggered = 0;
			this.speechTriggered = 0;
		}
		
		protected void addToSpeechBuffer(short[] buffer) {
			int offset = speechBuffer.length;

			// Create a new buffer.
			//
			speechBuffer = Arrays.copyOf(speechBuffer, speechBuffer.length
					+ buffer.length);

			// Copy the buffer to avoid reference problems.
			//
			short[] copiedBuffer = Arrays.copyOf(buffer, buffer.length);

			// Add the copied current buffer onto the end.
			//
			for (int i = 0; i < buffer.length; i++) {
				speechBuffer[i + offset] = copiedBuffer[i];
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
	
//	@After
//	public void tearDown() {
//		this.testClass.reset();
//	}
	
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
		
		assertEquals(0, testClass.speechTriggered);
	}
	
	@Test
	public void defaultAnalyze4() {
		for (int i = 0; i < 4; i++) {
			defaultSpeechAnalyzer.analyze(testClass, new short[]{6000});
		}
		
		assertEquals(2, testClass.speechTriggered);
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
		
		assertEquals(0, testClass.speechTriggered);
	}
	
	@Test
	public void specificAnalyze4() {
		for (int i = 0; i < 3; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		}
		
		assertEquals(2, testClass.speechTriggered);
	}
	
	@Test
	public void specificComplexAnalyze1() {
		
		// Lots of silence.
		//
		for (int i = 0; i < 20; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{(short) i});
		}
		
//		specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
//		specificSpeechAnalyzer.analyze(testClass, new short[]{6000}); // 1
//		specificSpeechAnalyzer.analyze(testClass, new short[]{6000}); // 2
//		specificSpeechAnalyzer.analyze(testClass, new short[]{6000}); // 3
		
		for (short i = 0; i < 4; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{(short) ((short) i+6000)});
		}
		
		// This silence does not trigger silence.
		//
		for (int i = 0; i < 2; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{(short) i});
		}

//		specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
//		specificSpeechAnalyzer.analyze(testClass, new short[]{6000}); // 4
		
		for (short i = 0; i < 3; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{(short) ((short) i+6000)});
		}
		
//		specificSpeechAnalyzer.analyze(testClass, new short[]{1});
//		specificSpeechAnalyzer.analyze(testClass, new short[]{1}); // 1
		
		// Triggers silence.
		//
		for (int i = 0; i < 3; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{(short) i});
		}
		
		assertEquals(1, testClass.silenceTriggered);
		assertEquals(10, testClass.speechTriggered);
		
		short[] comparison = new short[]{
			0, 0, 0, 0, 0, 0, 0, 0,
			14, 15, 16, 17, 18, 19,
			6000, 6001, 6002, 6003,
			0, 1,
			6000, 6001, 6002,
			0, 1
		};
		for (int i = 0; i < testClass.speechBuffer.length; i++) {
			assertEquals(comparison[i], testClass.speechBuffer[i]);
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
