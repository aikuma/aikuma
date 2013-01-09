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
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		Segments segments = new Segments(uuid);
		segments.readSegments();

		List<Integer> expectedOriginalSegments = Arrays.asList(0, 10);
		List<Integer> expectedRespeakingSegments = Arrays.asList(0, 20);

		assertEquals(expectedOriginalSegments, segments.getOriginalSegments());
		assertEquals(expectedRespeakingSegments, segments.getRespeakingSegments());

		new File(FileIO.getRecordingsPath(), uuid.toString()).delete();
	}

	public void testReadSegments2() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = ",0,0\n10,20\n";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		Segments segments = new Segments(uuid);
		boolean caught = false;
		try {
			segments.readSegments();
		} catch (Exception e) {
			caught = true;
		}
		assertTrue(caught);

		new File(FileIO.getRecordingsPath(), uuid.toString()).delete();
	}

	public void testReadSegments3() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "0,0\nfail,20\n";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		Segments segments = new Segments(uuid);
		boolean caught = false;
		try {
			segments.readSegments();
		} catch (Exception e) {
			caught = true;
		}
		assertTrue(caught);

		new File(FileIO.getRecordingsPath(), uuid.toString()).delete();
	}

	public void testReadSegments4() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "0,0\n,20\n";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		Segments segments = new Segments(uuid);
		boolean caught = false;
		try {
			segments.readSegments();
		} catch (Exception e) {
			caught = true;
		}
		assertTrue(caught);

		new File(FileIO.getRecordingsPath(), uuid.toString()).delete();
	}

	public void testReadSegments5() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "0,0\n10,20\n30";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		Segments segments = new Segments(uuid);
		segments.readSegments();

		List<Integer> expectedOriginalSegments = Arrays.asList(0, 10, 30);
		List<Integer> expectedRespeakingSegments = Arrays.asList(0, 20);

		assertEquals(expectedOriginalSegments, segments.getOriginalSegments());
		assertEquals(expectedRespeakingSegments, segments.getRespeakingSegments());

		new File(FileIO.getRecordingsPath(), uuid.toString()).delete();
	}

	public void testReadSegments6() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "0,0\n10,20\n30,";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		Segments segments = new Segments(uuid);
		segments.readSegments();

		List<Integer> expectedOriginalSegments = Arrays.asList(0, 10, 30);
		List<Integer> expectedRespeakingSegments = Arrays.asList(0, 20);

		assertEquals(expectedOriginalSegments, segments.getOriginalSegments());
		assertEquals(expectedRespeakingSegments, segments.getRespeakingSegments());

		new File(FileIO.getRecordingsPath(), uuid.toString()).delete();
	}

	/*
	public void testReadSegments2() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		Object[] parameters = new Object[1];
		parameters[0] = uuid;

		boolean caught = false;
		try {
			Map<String, List> segments = (Map<String, List>)
					readSegmentsMethod.invoke(interleavedPlayer, parameters);
		} catch (Exception e) {
			caught = true;
		}
		assertTrue(caught);
	}

	public void testReadSegments3() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "\n";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		Object[] parameters = new Object[1];
		parameters[0] = uuid;

		Map<String, List> segments = (Map<String, List>)
				readSegmentsMethod.invoke(interleavedPlayer, parameters);

		Map<String, List> expectedSegments = new HashMap<String, List>();
		expectedSegments.put("original", new ArrayList<Integer>());
		expectedSegments.put("respeaking", new ArrayList<Integer>());

		assertEquals(expectedSegments, segments);
	}

	public void testReadSegments4() throws Exception {

		UUID uuid = UUID.randomUUID();

		String map = "0,0\n10,20\n30,";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		Object[] parameters = new Object[1];
		parameters[0] = uuid;

		Map<String, List> segments = (Map<String, List>)
				readSegmentsMethod.invoke(interleavedPlayer, parameters);

		Map<String, List> expectedSegments = new HashMap<String, List>();
		expectedSegments.put("original", Arrays.asList(0, 10, 30));
		expectedSegments.put("respeaking", Arrays.asList(0, 20));

		assertEquals(expectedSegments, segments);
	}

	*/
}
