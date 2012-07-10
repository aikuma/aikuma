package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.SpeechTriggers;
import au.edu.melbuni.boldapp.ThresholdSpeechAnalyzer;

@RunWith(CustomTestRunner.class)
public class ThresholdSpeechAnalyzerTest {
	
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
			this.speechTriggered += 1;
		}
		
		public void reset() {
			this.silenceTriggered = 0;
			this.speechTriggered = 0;
		}
		
	}
	
	private ThresholdSpeechAnalyzer defaultSpeechAnalyzer;
	private ThresholdSpeechAnalyzer specificSpeechAnalyzer;
	
	private SpeechTriggersTestClass testClass;

	@Before
	public void setUp() throws Exception {
		this.defaultSpeechAnalyzer = new ThresholdSpeechAnalyzer();
		this.specificSpeechAnalyzer = new ThresholdSpeechAnalyzer(1, 1);
		
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
		defaultSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		defaultSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		defaultSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		defaultSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		defaultSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		defaultSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		
		assertEquals(1, testClass.silenceTriggered);
	}
	
	@Test
	public void defaultAnalyze3() {
		defaultSpeechAnalyzer.analyze(testClass, new short[]{6000});
		
		assertEquals(0, testClass.speechTriggered);
	}
	
	@Test
	public void defaultAnalyze4() {
		defaultSpeechAnalyzer.analyze(testClass, new short[]{6000});
		defaultSpeechAnalyzer.analyze(testClass, new short[]{6000});
		defaultSpeechAnalyzer.analyze(testClass, new short[]{6000});
		
		assertEquals(1, testClass.speechTriggered);
	}
	
	@Test
	public void specificAnalyze1() {
		specificSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		
		assertEquals(0, testClass.silenceTriggered);
	}
	
	@Test
	public void specificAnalyze2() {
		specificSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		specificSpeechAnalyzer.analyze(testClass, new short[]{1,2,3});
		
		assertEquals(1, testClass.silenceTriggered);
	}
	
	@Test
	public void specificAnalyze3() {
		specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		
		assertEquals(0, testClass.speechTriggered);
	}
	
	@Test
	public void specificAnalyze4() {
		specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		
		assertEquals(1, testClass.speechTriggered);
	}
	
	@Test
	public void specificComplexAnalyze1() {
		specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		specificSpeechAnalyzer.analyze(testClass, new short[]{6000}); // 1
		specificSpeechAnalyzer.analyze(testClass, new short[]{6000}); // 2
		specificSpeechAnalyzer.analyze(testClass, new short[]{6000}); // 3
		
		specificSpeechAnalyzer.analyze(testClass, new short[]{1});

		specificSpeechAnalyzer.analyze(testClass, new short[]{6000});
		specificSpeechAnalyzer.analyze(testClass, new short[]{6000}); // 4
		
		specificSpeechAnalyzer.analyze(testClass, new short[]{1});
		specificSpeechAnalyzer.analyze(testClass, new short[]{1}); // 1
		
		assertEquals(1, testClass.silenceTriggered);
		assertEquals(4, testClass.speechTriggered);
	}
	
}
