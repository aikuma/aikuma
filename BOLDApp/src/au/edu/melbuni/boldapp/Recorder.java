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

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;


public class Recorder
{
    private static final String LOG_TAG = "Recorder";

    private MediaRecorder recorder = new MediaRecorder();
    private MediaPlayer   player = new MediaPlayer();
    
    private boolean playing = false;
    private boolean recording = false;
    
    public Recorder() {
    	File directory = new File(getBasePath());
    	directory.mkdirs();
	}
    
    protected String getBasePath() {
    	return Bundler.getBasePath() + "recordings/";
    }
    
    protected String generateFullFilename(String relativeFilename) {
    	String fileName = getBasePath();
    	fileName += relativeFilename;
    	fileName += ".3gp";
    	return fileName;
    }
    
    public void startRecording(String fileName) {
    	if (recording) {
    		return;
    	}
    	recording = true;
        
    	recorder.reset();
    	
    	recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // TODO Format?
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // TODO Uncompressed?
        recorder.setOutputFile(generateFullFilename(fileName));
        
        System.out.println(generateFullFilename(fileName));
        
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Recorder#prepare() failed");
        }

        recorder.start();
        System.out.println("Recording");
    }

    public void stopRecording() {
    	if (!recording) {
    		return;
    	}
        recorder.stop();
        recording = false;
        System.out.println("Stopped Recording");
    }

    public void startPlaying(String fileName) {
    	if (playing) {
    		return;
    	}
    	playing = true;
        try {
            player.setDataSource(generateFullFilename(fileName));
            player.prepare();
            player.start();
            System.out.println("Playing");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Recorder#prepare() failed");
        }
    }

    public void stopPlaying() {
    	if (!playing) {
    		return;
    	}
    	player.reset();
        playing = false;
        System.out.println("Stopped Playing");
    }
    
//    public void pause() {
//        if (mRecorder != null) {
//            mRecorder.release();
//            mRecorder = null;
//        }
//
//        if (mPlayer != null) {
//            mPlayer.release();
//            mPlayer = null;
//        }
//    }
}