package org.lp20.aikuma.audio.record;

import android.media.MediaPlayer;
import android.test.AndroidTestCase;
import android.util.Log;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;

/**
 * Tests for Recorder.
 *
 * Note that these tests require manual intervention.
 * Run 'adb logcat -s "ManualTesting"' and follow the instructions.
 */
public class RecorderTest extends AndroidTestCase {
	public void testRecorder() throws Exception {
		recorderTest(16000);
		recorderTest(44100);
	}

	public void recorderTest(int sampleRate) throws Exception {
		File f = new File("/mnt/sdcard/aikuma/testrecordings/testrecord1.wav");
		Recorder recorder = new Recorder(f, sampleRate);
		//recorder.prepare(path);
		recorder.listen();
		Log.i("ManualTesting", "Recording started at " + sampleRate + ".");
		TimeUnit.SECONDS.sleep(10);
		recorder.stop();
		Log.i("ManualTesting", "Recording stopped.");

		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(f.getPath());
		mediaPlayer.prepare();
		mediaPlayer.start();
		Log.i("ManualTesting", "Playback started at " + sampleRate + ".");
		TimeUnit.SECONDS.sleep(10);
		FileUtils.deleteDirectory(f.getParentFile());
	}
}
