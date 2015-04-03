/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.lp20.aikuma2.R;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.util.FileIO;


import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * The activity that allows metadata of audio or video to be recorded.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author 	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingMetadataActivity1 extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_metadata1);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Get metadata
		Intent intent = getIntent();
		uuid = UUID.fromString(
				(String) intent.getExtras().get("uuidString"));
		sampleRate = (Long) intent.getExtras().get("sampleRate");
		durationMsec = (Integer) intent.getExtras().get("durationMsec");
		groupId = (String)
				intent.getExtras().get("groupId");
		sourceVerId = (String)
				intent.getExtras().get("sourceVerId");
		numChannels = (Integer) intent.getExtras().get("numChannels");
		bitsPerSample = (Integer) intent.getExtras().getInt("bitsPerSample");
		latitude = (Double) intent.getExtras().get("latitude");
		longitude = (Double) intent.getExtras().get("longitude");
		format = (String)
				intent.getExtras().get("format");

		//Lets method in superclass know to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		//if duration of the file > 250msec
		if(durationMsec > 250) {
			safeActivityTransition = true;
		}
		safeActivityTransitionMessage = "Are you sure you want to discard this recording?";
		
		videoView = (VideoView) findViewById(R.id.videoView);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// Video -> videoView / Sound -> ListenFragment
		if(format.equals("mp4")) {
			setUpVideoView(uuid);
		} else if(listenFragment == null) {
			videoView.setVisibility(View.GONE);
			
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			listenFragment = new ListenFragment();
			ft.replace(R.id.ListenFragment, listenFragment);
			ft.commit();
			
			setUpPlayer(uuid, sampleRate);
		}
	}

	// Prepares the player with the recording.
	private void setUpPlayer(UUID uuid, long sampleRate) {
		try {
			recordingFile = new File(Recording.getNoSyncRecordingsPath(), 
					uuid.toString() + ".wav");
			listenFragment.setPlayer(new SimplePlayer(
					recordingFile,
					sampleRate, true));
		} catch (IOException e) {
			//The SimplePlayer cannot be constructed, so let's end the
			//activity.
			Toast.makeText(this, "There has been an error in the creation of the audio file which prevents it from being read.", Toast.LENGTH_LONG).show();
			RecordingMetadataActivity1.this.finish();
		}
	}
	
	// Set up the video-player
	private void setUpVideoView(UUID uuid) {
		recordingFile = new File(Recording.getNoSyncRecordingsPath(), uuid.toString() + ".mp4");
		videoView.setVideoPath(recordingFile.getAbsolutePath());

		videoView.setMediaController(new MediaController(this));
	}
	

	/**
	 * Called when the user has confirmed the recording
	 *
	 * @param	view	the OK button.
	 */
	public void onOkButtonPressed(View view) {
		Intent intent;
		if(sourceVerId != null) {
			intent = new Intent(this, RecordingMetadataActivity3.class);
			intent.putExtra("sourceVerId", sourceVerId);
			intent.putExtra("description", "");
		} else {
			intent = new Intent(this, RecordingMetadataActivity2.class);
		} 
		intent.putExtra("uuidString", uuid.toString());
		intent.putExtra("sampleRate", sampleRate);
		intent.putExtra("durationMsec", durationMsec);
		intent.putExtra("numChannels", numChannels);
		intent.putExtra("format", format);
		intent.putExtra("bitsPerSample", bitsPerSample);
		if(latitude != null && longitude != null) {
			// if location data is available, put else don't put
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
		}
		
		
		if(groupId != null)
			intent.putExtra("groupId", groupId);
		
		startActivity(intent);
	}
	
	@Override
	public void onBackPressed() {
		if (safeActivityTransition) {
			menuBehaviour.safeGoBack(safeActivityTransitionMessage, safeBehaviour);
		} else {
			safeBehaviour.onSafeBackButton();
			this.finish();
		}
	}
	/**
	 * Interface class having a function called when back-button is pressed
	 */
	private MenuBehaviour.BackButtonBehaviour safeBehaviour = 
			new MenuBehaviour.BackButtonBehaviour() {
				@Override
				public void onSafeBackButton() {
					try {
						FileIO.delete(recordingFile);
					} catch (IOException e) {
						Log.e(TAG, "deleting recording file failed");
					}
				}
			}; 
	
	private final static String TAG = "RecordingMetadataActivity1";
	
	private File recordingFile;
	
	static final int ADD_SPEAKER = 0;
	private UUID uuid;
	private long sampleRate;
	private int durationMsec;
	private String groupId;
	private String sourceVerId;
	private String format;
	private int bitsPerSample;
	private int numChannels;
	
	private Double latitude;
	private Double longitude;
	
	private ListenFragment listenFragment;
	private VideoView videoView;
}
