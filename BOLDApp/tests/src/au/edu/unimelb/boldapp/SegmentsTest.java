package au.edu.unimelb.boldapp;

import android.util.Log;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import junit.framework.TestCase;
import au.edu.unimelb.boldapp.audio.Segments;

public class SegmentsTest extends TestCase {

	public void testReadSegments1() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "0,0\n10,20\n";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString() +
				".map"), map);

		Segments segments = new Segments(uuid);

		List<Integer> expectedOriginalSegments = Arrays.asList(0, 10);
		List<Integer> expectedRespeakingSegments = Arrays.asList(0, 20);

		assertEquals(expectedOriginalSegments, segments.getOriginalSegments());
		assertEquals(expectedRespeakingSegments, segments.getRespeakingSegments());

		new File(FileIO.getRecordingsPath(), uuid.toString() + ".map").delete();
	}


	public void testReadSegments2() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = ",0,0\n10,20\n";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString() +
				".map"), map);

		boolean caught = false;
		try {
			Segments segments = new Segments(uuid);
		} catch (Exception e) {
			caught = true;
		}
		assertTrue(caught);

		new File(FileIO.getRecordingsPath(), uuid.toString() + ".map").delete();
	}

	public void testReadSegments3() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "0,0\nfail,20\n";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString() +
				".map"), map);

		boolean caught = false;
		try {
			Segments segments = new Segments(uuid);
		} catch (Exception e) {
			caught = true;
		}
		assertTrue(caught);

		new File(FileIO.getRecordingsPath(), uuid.toString() + ".map").delete();
	}

	public void testReadSegments4() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "0,0\n,20\n";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString() +
				".map"), map);

		boolean caught = false;
		try {
			Segments segments = new Segments(uuid);
		} catch (Exception e) {
			caught = true;
		}
		assertTrue(caught);

		new File(FileIO.getRecordingsPath(), uuid.toString() + ".map").delete();
	}

	public void testReadSegments5() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "0,0\n10,20\n30";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString() +
				".map"), map);

		Segments segments = new Segments(uuid);

		List<Integer> expectedOriginalSegments = Arrays.asList(0, 10, 30);
		List<Integer> expectedRespeakingSegments = Arrays.asList(0, 20);

		assertEquals(expectedOriginalSegments, segments.getOriginalSegments());
		assertEquals(expectedRespeakingSegments, segments.getRespeakingSegments());

		new File(FileIO.getRecordingsPath(), uuid.toString() + ".map").delete();
	}

	public void testReadSegments6() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "0,0\n10,20\n30,";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString() +
				".map"), map);

		Segments segments = new Segments(uuid);

		List<Integer> expectedOriginalSegments = Arrays.asList(0, 10, 30);
		List<Integer> expectedRespeakingSegments = Arrays.asList(0, 20);

		assertEquals(expectedOriginalSegments, segments.getOriginalSegments());
		assertEquals(expectedRespeakingSegments, segments.getRespeakingSegments());

		File file = new File(FileIO.getRecordingsPath(), uuid.toString() +
				".map");
		assertTrue(file.delete());
	}
}
