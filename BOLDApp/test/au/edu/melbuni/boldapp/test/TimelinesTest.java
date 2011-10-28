package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TimelinesTest {
	
	static Timelines timelines;
	
	@Before
	public void setUp() throws Exception {
		timelines = new Timelines();
	}
	
	@Test
	public void makeTestRunnerHappy() {}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithoutTimelines extends TimelinesTest {
		
		@Test
		public void get() { // delegates
			try {
				timelines.get(0);
			} catch (IndexOutOfBoundsException e) {
				assertNotNull(e);
			}
		}
		
		@Test
		public void size() { // delegates
			assertEquals(0, timelines.size());
		}
		
		@Test
		public void contains() { // delegates
			assertEquals(false, timelines.contains(new Timeline(null, null)));
		}
		
	}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithTimelines extends TimelinesTest {
		
		Timeline currentTimeline;
		
		@Before
		public void setUp() throws Exception {
			super.setUp();
			
			currentTimeline = new Timeline(null, null);
			
			timelines.add(currentTimeline);
		}
		
		@Test
		public void get() { // delegates
			assertEquals(currentTimeline, timelines.get(0));
		}
		
		@Test
		public void size() { // delegates
			assertEquals(1, timelines.size());
		}
		
		@Test
		public void contains() { // delegates
			assertEquals(true, timelines.contains(currentTimeline));
			assertEquals(false, timelines.contains(new Timeline(null, null)));
		}
		
	}

}
