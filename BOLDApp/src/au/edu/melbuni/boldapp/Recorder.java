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

import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

public class Recorder extends Sounder {
	private static final String LOG_TAG = "Recorder";

	private MediaRecorder recorder = new MediaRecorder();
	private boolean recording = false;

	public Recorder() {
		prepareNextRecording();
	}
	
	protected void prepareNextRecording() {
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
	}

	public void startRecording(String relativeFilename) {
		if (recording) { return; }
		recording = true;
		
		recorder.setOutputFile(prepareFile(relativeFilename));

		try {
			recorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "#prepare() failed");
		}
		
		// TODO Reset on problem?
		//
		recorder.start();
	}

	public void stopRecording() {
		if (!recording) { return; }
		recorder.stop();
		recording = false;
		
		prepareNextRecording();
	}

//	public void pause() {
//		if (recorder != null) {
//			recorder.release();
//			recorder = null;
//		}
//	}
}