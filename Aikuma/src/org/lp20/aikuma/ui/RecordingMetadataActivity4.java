/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.ImageUtils;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author 	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingMetadataActivity4 extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_metadata4);
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
		bitsPerSample = (Integer) intent.getExtras().get("bitsPerSample");
		latitude = (Double) intent.getExtras().get("latitude");
		longitude = (Double) intent.getExtras().get("longitude");
		format = (String)
				intent.getExtras().get("format");
		
		// settings for recording information
		description = (String) intent.getExtras().get("description");
		speakersIds = new ArrayList<String>();
		selectedSpeakers = intent.getParcelableArrayListExtra("speakers");
		selectedLanguages = intent.getParcelableArrayListExtra("languages");
		// Speakers images
		userImages =
				(LinearLayout) findViewById(R.id.userImagesAndAddUserButton);
		setUpSpeakersImages();
		// Recording name
		TextView descriptionView = (TextView) findViewById(R.id.description);
		descriptionView.setText(description);	
		// Recording languages
		TextView languageView = (TextView) findViewById(R.id.languageView);
		StringBuilder sb = new StringBuilder("Languages:\n");
		for(Language lang : selectedLanguages) {
			sb.append(lang.getName() + "\n");
		}
		languageView.setText(sb);
		for(Speaker speaker : selectedSpeakers) {
			speakersIds.add(speaker.getId());
		}

		//Lets method in superclass know to ask user if they are willing to go back
		safeActivityTransition = true;
		safeActivityTransitionMessage = "Are you sure you want to change one of the metadata?";
		
		videoView = (VideoView) findViewById(R.id.videoView);
	}

	// Prepares the player with the recording.
	private void setUpPlayer(UUID uuid, long sampleRate) {
		try {
			File recordingFile = new File(Recording.getNoSyncRecordingsPath(), 
					uuid.toString() + ".wav");
			listenFragment.setPlayer(new SimplePlayer(
					recordingFile,
					sampleRate, true));
		} catch (IOException e) {
			//The SimplePlayer cannot be constructed, so let's end the
			//activity.
			Toast.makeText(this, "There has been an error in the creation of the audio file which prevents it from being read.", Toast.LENGTH_LONG).show();
			RecordingMetadataActivity4.this.finish();
		}
	}
	
	// Set up the video-player
	private void setUpVideoView(UUID uuid) {
		File recordingFile = new File(Recording.getNoSyncRecordingsPath(), uuid.toString() + ".mp4");
		videoView.setVideoPath(recordingFile.getAbsolutePath());

		videoView.setMediaController(new MediaController(this));
	}	
	
	// Show the speakers' images
	private void setUpSpeakersImages() {
		for(Speaker speaker : selectedSpeakers) {
			ImageView speakerImage = new ImageView(this);
			speakerImage.setAdjustViewBounds(true);
			speakerImage.setMaxHeight(ImageUtils.getPixelsFromDp(this, 40));
			speakerImage.setMaxWidth(ImageUtils.getPixelsFromDp(this, 40));
			
			try {
				speakerImage.setImageBitmap(speaker.getSmallImage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage() + ": " + speaker.getId());
			}
			userImages.addView(speakerImage);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		
		if(format.equals("mp4")) {
			findViewById(R.id.ListenFragment).setVisibility(View.GONE);
			
			setUpVideoView(uuid);
		} else {
			videoView.setVisibility(View.GONE);
			
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			listenFragment = new ListenFragment();
			ft.replace(R.id.ListenFragment, listenFragment);
			ft.commit();
			
			setUpPlayer(uuid, sampleRate);
		}
	}


	/**
	 * Called when the user has indicated that the metadata is complete.
	 *
	 * @param	view	the OK button.
	 */
	public void onOkButtonPressed(View view) {
		Intent intent =
				new Intent(RecordingMetadataActivity4.this,
						MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		Date date = new Date();
		String deviceName = Aikuma.getDeviceName();
		String androidID = Aikuma.getAndroidID();
		Log.i("duration", "when recording created: " + durationMsec);

		Recording recording = new Recording(
				uuid, description, date, 
				AikumaSettings.getLatestVersion(), 
				AikumaSettings.getCurrentUserId(),
				selectedLanguages, speakersIds, 
				deviceName, androidID, groupId, sourceVerId,
				sampleRate, durationMsec, format, numChannels,
				bitsPerSample, latitude, longitude);
		try {
			// Move the wave file from the nosync directory to
			// the synced directory and write the metadata
			recording.write();
		} catch (IOException e) {
			Toast.makeText(RecordingMetadataActivity4.this,
				"Failed to write the Recording metadata:\t" +
				e.getMessage(), Toast.LENGTH_LONG).show();
			startActivity(intent);
			return;
		}
		Toast.makeText(RecordingMetadataActivity4.this, String.format("%s       %s (%s)", 
						recording.getNameAndLang(), 
						new SimpleDateFormat("yyyy-MM-dd").format(recording.getDate()),
						(durationMsec / 1000) + ""), Toast.LENGTH_LONG).show();
		

		// If automatic-backup is enabled, archive this file
		if(AikumaSettings.isBackupEnabled) {
			Intent serviceIntent = new Intent(RecordingMetadataActivity4.this, 
					GoogleCloudService.class);
			serviceIntent.putExtra(GoogleCloudService.ACTION_KEY, 
					recording.getVersionName() + "-" + recording.getId());
			serviceIntent.putExtra(GoogleCloudService.ARCHIVE_FILE_TYPE_KEY, "recording");
			serviceIntent.putExtra(GoogleCloudService.ACCOUNT_KEY, 
					AikumaSettings.getCurrentUserId());
			serviceIntent.putExtra(GoogleCloudService.TOKEN_KEY, 
					AikumaSettings.getCurrentUserToken());
			
			startService(serviceIntent);
		}
		
		startActivity(intent);
	}

	
	private static final String TAG = "RecordingMetadataActivity4";

	static final int ADD_SPEAKER = 0;
	private UUID uuid;
	private List<String> speakersIds;
	private List<Speaker> selectedSpeakers;
	private List<Language> selectedLanguages;
	private LinearLayout userImages;
	private long sampleRate;
	private int durationMsec;
	private String groupId;
	private String sourceVerId;
	private String format;
	private int bitsPerSample;
	private int numChannels;
	private String description;
	
	private Double latitude;
	private Double longitude;
	
	private ListenFragment listenFragment;
	private VideoView videoView;
}