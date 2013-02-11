import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import au.edu.unimelb.aikuma.audio.AudioHandler;
import au.edu.unimelb.aikuma.audio.Recorder;
import au.edu.unimelb.aikuma.audio.analyzers.SimpleAnalyzer;

public class SimpleAnalyzerTest {
  
	SimpleAnalyzer analyzer;
  
  class TestAudioHandler implements AudioHandler {
    
    public int audioTriggeredAmount = 0;
    public int silenceTriggeredAmount = 0;
    
  	public void audioTriggered(short[] buffer, boolean justChanged) {
  		audioTriggeredAmount += 1;
  	}

  	public void silenceTriggered(short[] buffer, boolean justChanged) {
  		silenceTriggeredAmount += 1;
  	}
    
  }
  
	@Before
	public void setUp() throws Exception {
		this.analyzer = new SimpleAnalyzer();
	}

	@Test
	public void audioTriggeredOnce() {
    TestAudioHandler handler = new TestAudioHandler();
    
    analyzer.analyze(handler, new short[]{1, 2, 3});
    
    assertEquals(1, handler.audioTriggeredAmount);
    assertEquals(0, handler.silenceTriggeredAmount);
	}
  
	@Test
	public void audioTriggeredMultipleTimes() {
    TestAudioHandler handler = new TestAudioHandler();
    
    for (int i = 0; i < 3; i++) {
      analyzer.analyze(handler, new short[]{1, 2, 3});
    }
    
    assertEquals(3, handler.audioTriggeredAmount);
    assertEquals(0, handler.silenceTriggeredAmount);
	}
  
}
