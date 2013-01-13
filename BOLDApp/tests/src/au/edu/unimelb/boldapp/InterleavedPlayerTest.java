package au.edu.unimelb.boldapp;

import au.edu.unimelb.boldapp.audio.InterleavedPlayer;

public class InterleavedPlayerTest extends TestCase {

	public void testCalculateOffsets1() throws Exception {
		UUID uuid = UUID.randomUUID();

		String map = "0,0\n10,20\n";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		InterleavedPlayer interleavedPlayer= new InterleavedPlayer(uuid);

		new File(FileIO.getRecordingsPath(), uuid.toString()).delete();
	}
}
