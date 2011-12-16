package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.models.Segment;

@RunWith(CustomTestRunner.class)
public class SegmentTest {
	
	Segment segment;
//	Observer observer;
	
	@Before
	public void setUp() throws Exception {
		segment = new Segment("some_identifier");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void fromHash() {
		Segment loaded = Segment.fromHash((Map<String, Object>) JSONValue.parse("{\"id\":\"Some Identifier\"}"));
		assertEquals("Some Identifier", loaded.getIdentifier());
	}
	@SuppressWarnings("unchecked")
	@Test
	public void fromHashWithoutData() {
		Segment loaded = Segment.fromHash((Map<String, Object>) JSONValue.parse("{}"));
		assertEquals("", loaded.getIdentifier());
	}
	
	@Test
	public void toHash() {
		assertEquals("{\"id\":\"some_identifier\"}", JSONValue.toJSONString(segment.toHash()));
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
