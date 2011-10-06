package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.Segment;
import au.edu.melbuni.boldapp.User;

@RunWith(CustomTestRunner.class)
public class SegmentTest {
	
	Segment segment;
//	Observer observer;
	
	@Before
	public void setUp() throws Exception {
		segment = new Segment("some_identifier");
	}
	
	@Test
	public void fromJSON() {
		Segment loaded = Segment.fromJSON("{\"identifier\":\"Some Identifier\"}");
		assertEquals("Some Identifier", loaded.identifier);
	}
	@Test
	public void fromJSONWithoutData() {
		Segment loaded = Segment.fromJSON("{}");
		assertEquals("", loaded.identifier);
	}
	
	@Test
	public void toJSON() {
		assertEquals("{\"identifier\":\"some_identifier\"}", segment.toJSON());
	}
	
	@Test
	public void select() {
		segment.select();
		assertEquals(true, segment.isSelected());
	}
	@Test
	public void setSelectedTrue() {
		segment.setSelected(true);
		assertEquals(true, segment.isSelected());
	}
	@Test
	public void deselect() {
		segment.deselect();
		assertEquals(false, segment.isSelected());
	}
	@Test
	public void setSelectedFalse() {
		segment.setSelected(false);
		assertEquals(false, segment.isSelected());
	}
	
	@Test
	public void setPlayingTrue() {
		segment.setPlaying(true);
		assertEquals(true, segment.isPlaying());
	}
	@Test
	public void setPlayingFalse() {
		segment.setPlaying(false);
		assertEquals(false, segment.isPlaying());
	}
	
	@Test
	public void setRecordingTrue() {
		segment.setRecording(true);
		assertEquals(true, segment.isRecording());
	}
	@Test
	public void setRecordingFalse() {
		segment.setRecording(false);
		assertEquals(false, segment.isRecording());
	}
}
