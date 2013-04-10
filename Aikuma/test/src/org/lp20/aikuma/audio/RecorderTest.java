package org.lp20.aikuma.audio;

import android.media.MediaPlayer;
import android.test.AndroidTestCase;
import android.util.Log;
import java.util.concurrent.TimeUnit;

/**
 * Tests for Recorder.
 *
 * Note that these tests require manual intervention.
 * Run 'adb logcat -s "ManualTesting"' and follow the instructions.
 */
public class RecorderTest extends AndroidTestCase {
	public void testRecorder() throws Exception {
		Log.i("ManualTesting", "hello");
		String path = "/mnt/sdcard/aikuma/testrecordings/testrecord1.wav";
		Recorder recorder = new Recorder(16000);
		Log.i("ManualTesting", "hello2");
		recorder.prepare(path);
		Log.i("ManualTesting", "hello3");
		recorder.listen();
		Log.i("ManualTesting", "Recording started.");
		TimeUnit.SECONDS.sleep(10);
		recorder.stop();
		Log.i("ManualTesting", "Recording stopped.");

		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(path);
		mediaPlayer.prepare();
		mediaPlayer.start();
		Log.i("ManualTesting", "Playback started.");
		TimeUnit.SECONDS.sleep(10);
	}
}
