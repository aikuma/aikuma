import java.util.UUID;

import au.edu.unimelb.boldapp.audio.InterleavedPlayer;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.ArrayList;

import android.util.Pair;

public class InterleavedPlayerTest {

	private InterleavedPlayer player;

	@Before
	public void setUp() throws Exception {
		ArrayList<Integer> originalSegments = new ArrayList<Integer>();
		originalSegments.add(0);
		originalSegments.add(300);
		originalSegments.add(600);
		originalSegments.add(900);
		originalSegments.add(1200);
		ArrayList<Integer> respeakingSegments = new ArrayList<Integer>();
		respeakingSegments.add(0);
		respeakingSegments.add(700);
		respeakingSegments.add(1400);
		respeakingSegments.add(2100);
		player = new InterleavedPlayer(
				UUID.fromString("a961f48a-b379-408c-855c-9707e8a05f38"),
				originalSegments,
				respeakingSegments);
		


	}

	@Test
	public void calculateOffsets() {
		// player.expects :seekToOnPlayers, 1205, 600
		// player.seekTo 1805
		InterleavedPlayer.Result actuals = player.calculateOffsets(1605);
		assertEquals(new Integer(600), actuals.originalSeekTo);
		assertEquals(new Integer(1005), actuals.respeakingSeekTo);
		
		actuals = player.calculateOffsets(0);
		assertEquals(new Integer(0), actuals.originalSeekTo);
		assertEquals(new Integer(0), actuals.respeakingSeekTo);

		actuals = player.calculateOffsets(3300);
		assertEquals(new Integer(1200), actuals.originalSeekTo);
		assertEquals(new Integer(2100), actuals.respeakingSeekTo);

		actuals = player.calculateOffsets(10000000);
		assertEquals(new Integer(1200), actuals.originalSeekTo);
		assertEquals(new Integer(2100), actuals.respeakingSeekTo);

	}

	@Test
	public void seekTo() {
		// Seek to the start
		InterleavedPlayer.Result actuals = player.calculateOffsets(0);
		assertEquals(new Integer(1), actuals.segCount);
		assertEquals(new Boolean(true), actuals.toPlayOriginal);
		assertEquals(new Integer(0), actuals.originalSeekTo);
		assertEquals(new Integer(0), actuals.respeakingSeekTo);

		// Now seek to the end
		actuals = player.calculateOffsets(100000);
		assertEquals(new Integer(1200), actuals.originalSeekTo);
		assertEquals(new Integer(2100), actuals.respeakingSeekTo);

		// Seek to the start
		actuals = player.calculateOffsets(0);
		assertEquals(new Integer(1), actuals.segCount);
		assertEquals(new Boolean(true), actuals.toPlayOriginal);
		assertEquals(new Integer(0), actuals.originalSeekTo);
		assertEquals(new Integer(0), actuals.respeakingSeekTo);
	}
}
