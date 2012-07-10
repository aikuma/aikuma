package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Segments;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SegmentsTest {
	
	static Segments segments;
	
	@Before
	public void setUp() throws Exception {
		segments = new Segments();
	}
	
	@Test
	public void makeTestRunnerHappy() {}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithoutSegments extends SegmentsTest {
		
		@Test
		public void get() {
			try {
				segments.get(0);
			} catch (IndexOutOfBoundsException e) {
				assertNotNull(e);
			}
		}
		
		@Test
		public void size() {
			assertEquals(0, segments.size());
		}
		
		@Test
		public void isEmpty() {
			assertTrue(segments.isEmpty());
		}
		
		@Test
		public void contains() {
			assertFalse(segments.contains(new Segment("some_identifier")));
		}
		
		@Test
		public void selectLastForPlaying() {
			assertFalse(segments.selectLastForPlaying());
		}
		
		@Test
		public void toHash() {
			assertEquals("{\"segments\":[]}", JSONValue.toJSONString(segments.toHash()));
		}
		
	}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithSegments extends SegmentsTest {
		
		Segment currentSegment;
		Segment otherSegment;
		
		@Before
		public void setUp() throws Exception {
			super.setUp();
			
			currentSegment = new Segment("specific_identifier");
			otherSegment   = new Segment("unimportant");
			
			segments.add(currentSegment);
			segments.add(otherSegment);
		}
		
		@Test
		public void get() {
			assertEquals(currentSegment, segments.get(0));
		}
		
		@Test
		public void size() {
			assertEquals(2, segments.size());
		}
		
		@Test
		public void isEmpty() {
			assertFalse(segments.isEmpty());
		}
		
		@Test
		public void contains() {
			assertEquals(true, segments.contains(currentSegment));
			assertEquals(false, segments.contains(new Segment("anything")));
		}
		
		@Test
		public void selectLastForPlaying() {
			assertEquals(true, segments.selectLastForPlaying());
//			assertTrue(segments.get(1).isSelected()); // TODO
		}
		
		@Test
		public void toHash() {
			assertEquals("{\"segments\":[\"" + currentSegment.getIdentifier() + "\",\"" + otherSegment.getIdentifier() + "\"]}", JSONValue.toJSONString(segments.toHash()));
		}
		
	}

// TODO
//
//	@SuppressWarnings("unchecked")
//	@Test
//	public void fromHash() {
//		List<String> ids = new ArrayList<String>();
//		ids.add("1");
//		ids.add("2");
//		ids.add("3");
//		Segments loaded = Segments.fromHash(
//			new JSONPersister(),
//			ids
//		);
//		assertEquals(ids, loaded.getIds());
//	}
//	@SuppressWarnings("unchecked")
//	@Test
//	public void fromHashWithoutData() {
//		Segments loaded = Segments.fromHash(new JSONPersister(), (Map<String, Object>) JSONValue.parse("{}"));
//		assertEquals("", loaded.getPrefix());
//	}

}
