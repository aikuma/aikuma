package au.edu.unimelb.aikuma;

import au.edu.unimelb.aikuma.audio.InterleavedPlayer;
import java.io.File;
import java.util.UUID;
import junit.framework.TestCase;

public class InterleavedPlayerTest extends TestCase {

	public void testCalculateOffsets1() throws Exception {
		UUID uuid = UUID.randomUUID();

		String map = "0,0\n10,20\n";
		FileIO.write(new File(FileIO.getRecordingsPath(), uuid.toString()), map);

		InterleavedPlayer interleavedPlayer= new InterleavedPlayer(uuid);

		new File(FileIO.getRecordingsPath(), uuid.toString()).delete();
	}
}
