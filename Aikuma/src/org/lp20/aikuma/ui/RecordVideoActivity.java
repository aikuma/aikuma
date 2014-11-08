/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.util.DebugUtils;
import org.lp20.aikuma.util.VideoUtils;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Activity for recording video
 * (Camera preview + 3 buttons(record->pause->save))
 * 
 * @author Sangyeop Lee		<sangl1@student.unimelb.edu.au>
 *
 */
public class RecordVideoActivity extends Activity implements SurfaceHolder.Callback {
	
	private static final String TAG = "RecordVideoActivity";
	
	private boolean isRecording = false;
	private Button recordVideoButton;
	private Button pauseVideoButton;
	private Button saveVideoButton;
	
	private Camera camera;
	private SurfaceHolder cameraViewHolder;
	private MediaRecorder mediaRecorder;
	private UUID videoUUID;
	
	private int sampleRate = 16000;
	private int numChannels = 1;
//	private int bitsRate = 96000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Full screen mode for video recording
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.record_video);
		
		videoUUID = UUID.randomUUID();
		recordVideoButton = 
				(Button) findViewById(R.id.recordVideoButton);
		pauseVideoButton =
				(Button) findViewById(R.id.pauseVideoButton);
		saveVideoButton = 
				(Button) findViewById(R.id.saveVideoButton);
		
		try{
			camera = Camera.open();	
		} catch(Exception e) {
			Log.e(TAG, e.getMessage());
			finish();
		}
		
		SurfaceView cameraView = 
				(SurfaceView) findViewById(R.id.videoRecordingView);
		cameraViewHolder = cameraView.getHolder();
		cameraViewHolder.addCallback(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Create camera, media recorder objects
		try{
			if(camera == null)
				camera = Camera.open();	
		} catch(Exception e) {
			Log.e(TAG, e.getMessage());
			finish();
		}
		mediaRecorder = new MediaRecorder();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		releaseMediaRecorder();
		releaseCamera();
	}


	/**
	 * Prepare the MediaRecorder for video-recording
	 * @return	true: success / false: fail
	 */
	public boolean prepareVideoRecorder() {	
		int videoWidth = camera.getParameters().getPreviewSize().width;
	    int videoHeight = camera.getParameters().getPreviewSize().height;
	    
		// Unlock the camera temporarily to be used by mediaRecorder
	    camera.unlock();
	    mediaRecorder.setCamera(camera);

	    // Set the source, output-format, encoders and metadata
	    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
	    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

	    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	    
	    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
	    mediaRecorder.setAudioChannels(numChannels);
	    mediaRecorder.setMaxDuration(0);
	    mediaRecorder.setAudioSamplingRate(sampleRate);
//	    mediaRecorder.setAudioEncodingBitRate(bitsRate);
	    
	    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
	    mediaRecorder.setVideoSize(videoWidth, videoHeight);
//	    mediaRecorder.setVideoFrameRate(24);
//	    mediaRecorder.setVideoEncodingBitRate(3000000);
	    
	    // Set output file
	    mediaRecorder.setOutputFile(getOutputVideoFile().toString());

	    // Set the preview output
	    mediaRecorder.setPreviewDisplay(cameraViewHolder.getSurface());

	    // Prepare the configured MediaRecorder
	    try {
	        mediaRecorder.prepare();
	    } catch (IllegalStateException e) {
	        Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    } catch (IOException e) {
	        Log.e(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    }
	    return true;
	}
	
	// Release the MediaRecorder
	private void releaseMediaRecorder() {
		if (mediaRecorder != null) {
			// Clear configuration and release object
			// Camera is locked until onPause is called
            mediaRecorder.reset();   
            mediaRecorder.release(); 
            mediaRecorder = null;
            camera.lock();           
        }
	}
	
	// Release the camera object
	private void releaseCamera(){
        if (camera != null){
            camera.release();
            camera = null;
        }
    }
	
	/**
	 * When the Save button is pressed(Record -> Pause -> Save)
	 * @param view	Save button view
	 */
	public void onRecordVideoSave(View view) {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
	    retriever.setDataSource(getOutputVideoFile().toString());
	    
	    String time = retriever.extractMetadata(
	    		MediaMetadataRetriever.METADATA_KEY_DURATION);
	    int duration = Integer.parseInt(time);
	    
	    //Start next activity
		Double latitude = MainActivity.locationDetector.getLatitude();
		Double longitude = MainActivity.locationDetector.getLongitude();
		
		Intent intent = new Intent(this, RecordingMetadataActivity1.class);
		intent.putExtra("uuidString", videoUUID.toString());
		intent.putExtra("sampleRate", (long) sampleRate);
		intent.putExtra("durationMsec", duration);
		intent.putExtra("numChannels", numChannels);
		intent.putExtra("format", "mp4");
		
		// In case of Video bitsPerSample is meaningless
		intent.putExtra("bitsPerSample", 0);	
		
		if(latitude != null && longitude != null) {
			// if location data is available, put else don't put
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
		}
		
		startActivity(intent);
		RecordVideoActivity.this.finish();
	}
	
	/**
	 * When the Pause button is pressed(Record -> Pause -> Save)
	 * @param view	Pause button view
	 */
	public void onRecordVideoPause(View view) {
		// Stop the recording and release mediarecorder object
        mediaRecorder.stop();  
        releaseMediaRecorder(); 
        camera.lock();         
        
        pauseVideoButton.setVisibility(View.GONE);
        saveVideoButton.setVisibility(View.VISIBLE);
        isRecording = false;
	}
	
	/**
	 * When the Record button is pressed(Record -> Pause -> Save)
	 * @param view	Record button view
	 */
	public void onRecordVideoStart(View view) {
		if(prepareVideoRecorder()) {
			// If camera is avaliable, unlocked, mediarecorder prepared
			// Start video recording
			mediaRecorder.start();
			recordVideoButton.setVisibility(View.GONE);
			pauseVideoButton.setVisibility(View.VISIBLE);
			Log.i(TAG, "prepare okay, start recording");
			isRecording = true;
		} else {
			Log.i(TAG, "prepare not okay");
			releaseMediaRecorder();
		}
	}
	
	private File getOutputVideoFile() {
		return VideoUtils.getNoSyncVideoFile(videoUUID);
	}
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(cameraViewHolder);
			camera.startPreview();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if(!isRecording) {
			// Shutdown current preview
			if(cameraViewHolder.getSurface() == null) {
				return;
			}
			camera.stopPreview();

			// portrait
			if(width < height) {
				// Put rotation matrix metadata to video file
				mediaRecorder.setOrientationHint(90);
				// Rotate camera display
				camera.setDisplayOrientation(90);
			} else {	//landscape
				mediaRecorder.setOrientationHint(0);
				camera.setDisplayOrientation(0);
			}
			
			try {
				camera.setPreviewDisplay(cameraViewHolder);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			camera.startPreview();
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
}
