/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Oliver Sangyeop Lee
*/
package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.ImageUtils;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingMetadataActivity3 extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_metadata3);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// For the recording image
		if(savedInstanceState != null) {
			imageUUID = UUID.fromString(savedInstanceState.getString("imageUUID"));
		} else {
			imageUUID = UUID.randomUUID();
		}
		
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
		
		selectedLanguages = intent.getParcelableArrayListExtra("languages");
		
		// settings for recording description
		description = (String) intent.getExtras().get("description");
		TextView descriptionView = (TextView) findViewById(R.id.description);
		descriptionView.setText(description);	
		
		// Recording languages
		TextView languageView = (TextView) findViewById(R.id.languageView);
		StringBuilder sb = new StringBuilder("Languages:\n");
		for(Language lang : selectedLanguages) {
			sb.append(lang.getName() + "\n");
		}
		languageView.setText(sb);

		comments = (String) intent.getExtras().getString("comments");
		if(comments == null)
			comments = "";
		TextView commentsView = (TextView) findViewById(R.id.commentsView);
		commentsView.setText("Comments:\n" + comments);
		
		//Lets method in superclass(AikumaAcitivity) know 
		//to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		safeActivityTransition = false;
		safeActivityTransitionMessage =  "This will discard the new recording's photo.";		
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save the current activity state
		savedInstanceState.putString("imageUUID", imageUUID.toString());
	    
	    //Call the superclass to save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent
			result) {
		if (resultCode == RESULT_OK) {	
			Intent intent = new Intent(this, RecordingMetadataActivity4.class);
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
			
			if(sourceVerId != null)
				intent.putExtra("sourceVerId", sourceVerId);
			if(groupId != null)
				intent.putExtra("groupId", groupId);
			
			intent.putExtra("description", description);
			intent.putExtra("comments", comments);

			intent.putParcelableArrayListExtra("languages", selectedLanguages);
			
			intent.putExtra("imageUUID", imageUUID.toString());
			
			startActivity(intent);
		}
	}

	/**
	 * When the take photo button is pressed.
	 *
	 * @param	view	The take photo button.
	 */
	public void takePhoto(View view) {
		dispatchTakePictureIntent(PHOTO_REQUEST_CODE);
	}

	private void dispatchTakePictureIntent(int actionCode) {
		// Create an no-sync-path for an image-file
		File imageOutputFile = ImageUtils.getNoSyncImageFile(this.imageUUID);
		Uri imageUri = Uri.fromFile(imageOutputFile);
		
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        // TODO: This is a temporary solution, custom camera activity is needed for facing-camera
		//takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
		
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, actionCode);
	    }
		
	}

	static final int PHOTO_REQUEST_CODE = 1;

	//Metadata
	private UUID uuid;
	private ArrayList<Language> selectedLanguages;
	private long sampleRate;
	private int durationMsec;
	private String groupId;
	private String sourceVerId;
	private String format;
	private int bitsPerSample;
	private int numChannels;
	private String description;
	private String comments;
	
	private Double latitude;
	private Double longitude;
	
	private UUID imageUUID;
}