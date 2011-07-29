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
package au.edu.melbuni.miniboldapp;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;


public class Recorder
{
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private MediaRecorder mRecorder = new MediaRecorder();
    private MediaPlayer   mPlayer = new MediaPlayer();
    
    private boolean playing = false;
    private boolean recording = false;
    
    public Recorder(String fileName) {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/";
        mFileName += fileName;
	}
    
    public void record() {
        if (recording) {
        	stopRecording();
        } else {
        	startRecording();
        }
    }

    public void play() {
        if (playing) {
        	stopPlaying();
        } else {
            startPlaying();
        }
    }

    public void startPlaying() {
    	if (playing) {
    		return;
    	}
    	playing = true;
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            System.out.println("Playing");
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void stopPlaying() {
    	mPlayer.reset();
        playing = false;
        System.out.println("Stopped Playing");
    }

    public void startRecording() {
    	if (recording) {
    		return;
    	}
    	recording = true;
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Recorder#prepare() failed");
        }

        mRecorder.start();
        System.out.println("Recording");
    }

    public void stopRecording() {
        mRecorder.stop();
        recording = false;
        System.out.println("Stopped Recording");
    }
    
    public void pause() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}