/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Sangyeop Lee
*/
package org.lp20.aikuma.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.ImageUtils;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class AddSpeakerActivity4 extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_speaker4);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		Intent intent = getIntent();
		origin = intent.getExtras().getInt("origin");
		name = (String) intent.getExtras().getString("name");
		selectedLanguages = intent.getParcelableArrayListExtra("languages");
		String imageUUIDStr = intent.getExtras().getString("imageUUID");
		imageUUID = UUID.fromString(imageUUIDStr);
		
		TextView nameView = (TextView) findViewById(R.id.nameView3);
		nameView.setText("Name: " + name);
		TextView languageView = (TextView) findViewById(R.id.languageView2);
		StringBuilder sb = new StringBuilder("Languages:\n");
		for(Language lang : selectedLanguages) {
			sb.append(lang.getName() + "\n");
		}
		languageView.setText(sb);
		handleSmallCameraPhoto();
		
		//Lets method in superclass(AikumaAcitivity) know 
		//to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		safeActivityTransition = true;
		safeActivityTransitionMessage = 
				"This will discard the new speaker's photo.";
	}

	// Creates a smaller version of the photo taken and uses it for the speaker
	// image view.
	private void handleSmallCameraPhoto() {
		Bitmap image;
		try {
			ImageUtils.createSmallSpeakerImage(this.imageUUID);
			image = ImageUtils.getNoSyncSmallImage(this.imageUUID);
			
		} catch (IOException e) {
			image = null;
		}
		ImageView speakerImage = (ImageView) findViewById(R.id.speakerImage);
		speakerImage.setImageBitmap(image);
	}

	/**
	 * Called when the user is ready to confirm the creation of the speaker.
	 *
	 * @param	view	The OK button.
	 */
	public void onOkButtonPressed(View view) {
		Speaker newSpeaker = new Speaker(imageUUID, name, selectedLanguages, 
				AikumaSettings.getLatestVersion(), AikumaSettings.getCurrentUserId());
		try {
			newSpeaker.write();
		} catch (IOException e) {
			Toast.makeText(this, 
					"Failed to write the Speaker to file or import speaker image",
					Toast.LENGTH_LONG).show();
		}
		
		// If automatic-backup is enabled, archive this file
		if(AikumaSettings.isBackupEnabled) {
			Intent serviceIntent = new Intent(AddSpeakerActivity4.this, 
					GoogleCloudService.class);
			String verId = newSpeaker.getVersionName() + "-" + newSpeaker.getId() + "-" + newSpeaker.getOwnerId();
			serviceIntent.putExtra(GoogleCloudService.ACTION_KEY, verId);
			serviceIntent.putExtra(GoogleCloudService.ARCHIVE_FILE_TYPE_KEY, "speaker");
			serviceIntent.putExtra(GoogleCloudService.ACCOUNT_KEY, 
					AikumaSettings.getCurrentUserId());
			serviceIntent.putExtra(GoogleCloudService.TOKEN_KEY, 
					AikumaSettings.getCurrentUserToken());
			
			startService(serviceIntent);
		}
		
		Intent intent;
		if(origin == 0)
			intent = new Intent(this, MainSpeakersActivity.class);
		else
			intent = new Intent(this, RecordingSpeakersActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		if (safeActivityTransition) {
			menuBehaviour.safeGoBack(safeActivityTransitionMessage, safeBehaviour);
		} else {
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
						FileIO.delete(ImageUtils.getNoSyncSmallImageFile(imageUUID));
						FileIO.delete(ImageUtils.getNoSyncImageFile(imageUUID));
					} catch (IOException e) {
						Log.e(TAG, "deleting speaker images failed");
					}
				}
			}; 
	
	
	private static final String TAG = "AddSpeakerActivity4";
	
	private int origin;	//0: MainSpeakersActivity, 1:RecordingSpeakersActivity
	private String name;
	private ArrayList<Language> selectedLanguages;
	private UUID imageUUID;
}
