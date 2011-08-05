/*
 * The application needs to have the permission to write to external storage
 * if the output file is written to the external storage, and also the
 * permission to record audio. These permissions must be set in the
 * application's AndroidManifest.xml file, with something like:
 *
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.RECORD_AUDIO" />
 *
 */
package au.edu.melbuni.boldapp;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

public class Recorder extends Sounder {
	private static final String LOG_TAG = "Recorder";

	private MediaRecorder recorder = new MediaRecorder();
	private boolean recording = false;

	public Recorder() {
		File directory = new File(getBasePath());
		directory.mkdirs();
	}

	public void startRecording(String fileName) {
		if (recording) {
			return;
		}
		recording = true;

		recorder.reset();

		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // TODO
																		// Format?
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // TODO
																		// Uncompressed?
		recorder.setOutputFile(generateFullFilename(fileName));

		System.out.println(generateFullFilename(fileName));

		try {
			recorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "#prepare() failed");
		}

		recorder.start();
	}

	public void stopRecording() {
		if (!recording) {
			return;
		}
		recorder.stop();
		recording = false;
	}

	public void pause() {
		if (recorder != null) {
			recorder.release();
			recorder = null;
		}
	}
}