package org.lp20.aikuma.audio;

import android.util.Log;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.model.Segments;
import org.lp20.aikuma.model.Segments.Segment;
import android.test.AndroidTestCase;

public class SegmentsTest extends AndroidTestCase {
	public void testSegments() {
		Segments segments = new Segments();
		segments.put(new Segment(0l,100l), new Segment(0l, 200l));
	}
}
