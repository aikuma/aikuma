package org.lp20.aikuma.audio.record;

import android.media.MediaPlayer;
import android.test.AndroidTestCase;
import java.util.Date;
import java.util.UUID;
import android.util.Log;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.audio.MarkedPlayer;

/**
 * Tests for Recorder.
 *
 * Note that these tests require manual intervention.
 * Run 'adb logcat -s "ManualTesting"' and follow the instructions.
 */
public class RecorderTest extends AndroidTestCase {
	public void testRecorder() throws Exception {
		record(16000);
		markedPlayerTest();
		//recorderTest(44100);
		f.delete();
	}

	public void record(int sampleRate) throws Exception {
		UUID uuid = UUID.randomUUID();
		f = new File("/mnt/sdcard/aikuma/recordings/" + uuid + ".wav");
		File fmeta = new File("/mnt/sdcard/aikuma/recordings/" + uuid + ".json");
		testRecording =
				new Recording(uuid, "Test recording", new Date());
		testRecording.write();
		Recorder recorder = new Recorder(f, sampleRate);
		//recorder.prepare(path);
		recorder.listen();
		Log.i("ManualTesting", "Recording started at " + sampleRate + ".");
		TimeUnit.SECONDS.sleep(10);
		recorder.stop();
		Log.i("ManualTesting", "Recording stopped.");
		fmeta.delete();
	}

	public void playBackRecording() throws Exception {

		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(f.getPath());
		mediaPlayer.prepare();
		mediaPlayer.start();
		Log.i("ManualTesting", "Playback started.");
		TimeUnit.SECONDS.sleep(10);
		/*
		FileUtils.deleteDirectory(f.getParentFile());
		*/
	}

	public void markedPlayerTest() throws Exception {
		MarkedPlayer.OnMarkerReachedListener listener =
				new MarkedPlayer.OnMarkerReachedListener() {
					public void onMarkerReached(MarkedPlayer markedPlayer) {
						Log.i("ManualTesting", "Pausing for three seconds.");
						markedPlayer.pause();
						markedPlayer.unsetNotificationMarkerPosition();
						try {
							TimeUnit.SECONDS.sleep(3);
						} catch (InterruptedException e) {
							Log.e("ManualTesting", "Exception: ", e);
						}
						markedPlayer.play();
					}
				};
		if (listener == null) {
			Log.i("ManualTesting", "Listener is null");
		}
		MarkedPlayer markedPlayer = new MarkedPlayer(testRecording, listener);
		markedPlayer.setNotificationMarkerPositionMsec(5000);
		Log.i("ManualTesting", "Playing for five seconds.");
		markedPlayer.play();
		TimeUnit.SECONDS.sleep(15);
		markedPlayer.release();
	}

	private Recording testRecording;
	private File f;
}
