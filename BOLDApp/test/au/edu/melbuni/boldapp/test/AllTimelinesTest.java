package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.models.AllTimelines;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AllTimelinesTest {
	
	@Before
	public void clearAllTimelines() {
		AllTimelines.getTimelines().clear();
	}
	
	@Test
	public void makeTestRunnerHappy() {}

	@Test
	public void initialTimelinesNotNull() {
		assertNotNull(AllTimelines.getTimelines());
	}
	
	@Test
	public void initialTimelinesEmpty() {
		assertTrue(AllTimelines.getTimelines().isEmpty());
	}
	
	@Test
	public void addWithoutTimeline() {
		AllTimelines.add(new Timeline("prefix_"));
	}
	
	@Test
	public void addWithTimeline() {
		Timeline timeline = new Timeline("prefix_");
		AllTimelines.add(timeline);
		
		assertFalse(AllTimelines.add(timeline));
		
		assertEquals(1, AllTimelines.getTimelines().size());
	}
	
	// "Functional" tests.
	//
	
	@Test
	public void creatingNewTimelineAddsToAllTimelines() {
		assertTrue(AllTimelines.getTimelines().isEmpty());
		
		Timeline timeline = new Timeline("prefix_");
		Timelines timelines = new Timelines();
		
		timelines.add(timeline);
		
		assertTrue(AllTimelines.contains(timeline));
	}

}
