package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.BufferingThresholdSpeechAnalyzer;
import au.edu.melbuni.boldapp.SpeechTriggers;

@RunWith(CustomTestRunner.class)
public class BufferingThresholdSpeechAnalyzerTest {
	
	private class SpeechTriggersTestClass implements SpeechTriggers {
		
		private int silenceTriggered;
		private int speechTriggered;
		
		public SpeechTriggersTestClass() {
			reset();
		}
		
		@Override
		public void silenceTriggered(short[] buffer, boolean justChanged) {
			this.silenceTriggered += 1;
		}
		
		@Override
		public void speechTriggered(short[] buffer, boolean justChanged) {
//			System.out.print("buffer: [");
//			for (int i = 0; i < buffer.length; i++) {
//				System.out.print("" + buffer[i] + ", ");
//			}
//			System.out.println("]");
			
			this.speechTriggered += 1;
		}
		
		public void reset() {
			this.silenceTriggered = 0;
			this.speechTriggered = 0;
		}
		
	}
	
	private BufferingThresholdSpeechAnalyzer defaultSpeechAnalyzer;
	private BufferingThresholdSpeechAnalyzer specificSpeechAnalyzer;
	
	private SpeechTriggersTestClass testClass;

	@Before
	public void setUp() throws Exception {
		this.defaultSpeechAnalyzer = new BufferingThresholdSpeechAnalyzer();
		this.specificSpeechAnalyzer = new BufferingThresholdSpeechAnalyzer(2, 2);
		
		this.testClass = new SpeechTriggersTestClass();
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
		for (int i = 0; i < 2; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		}
		
		for (int i = 0; i < 2; i++) {
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
		for (int i = 0; i < 2; i++) {
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
		
		for (int i = 0; i < 4; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		}
		
		// This silence does not trigger silence.
		//
		for (int i = 0; i < 2; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{1});
		}

//		specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
//		specificSpeechAnalyzer.analyze(testClass, new short[]{6000}); // 4
		
		for (int i = 0; i < 3; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		}
		
//		specificSpeechAnalyzer.analyze(testClass, new short[]{1});
//		specificSpeechAnalyzer.analyze(testClass, new short[]{1}); // 1
		
		// Triggers silence.
		//
		for (int i = 0; i < 3; i++) {
			specificSpeechAnalyzer.analyze(testClass, new short[]{1});
		}
		
		assertEquals(1, testClass.silenceTriggered);
		assertEquals(10, testClass.speechTriggered);
	}
	
}
