/*
	Copyright (C) 2013, The Aikuma Project
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
public class AddSpeakerActivity3 extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_speaker3);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		Intent intent = getIntent();
		name = (String) intent.getExtras().getString("name");
		selectedLanguages = intent.getParcelableArrayListExtra("languages");
		
		TextView nameView = (TextView) findViewById(R.id.nameView2);
		nameView.setText("Name: " + name);
		TextView languageView = (TextView) findViewById(R.id.languageView1);
		StringBuilder sb = new StringBuilder("Languages:\n");
		for(Language lang : selectedLanguages) {
			sb.append(lang + "\n");
		}
		languageView.setText(sb);
		
		//Lets method in superclass(AikumaAcitivity) know 
		//to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		safeActivityTransition = false;
		safeActivityTransitionMessage = 
				"This will discard the new speaker's photo.";
		
		if(savedInstanceState != null) {
			imageUUID = UUID.fromString(savedInstanceState.getString("imageUUID"));
		} else {
			imageUUID = UUID.randomUUID();
		}
		
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
			Intent lastIntent = new Intent(this, AddSpeakerActivity4.class);
			lastIntent.putExtra("origin", getIntent().getExtras().getInt("origin"));
			lastIntent.putExtra("name", name);
			lastIntent.putParcelableArrayListExtra("languages", selectedLanguages);
			lastIntent.putExtra("imageUUID", imageUUID.toString());
			startActivity(lastIntent);
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
		takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
		
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, actionCode);
	    }
		
	}

	static final int PHOTO_REQUEST_CODE = 1;
	
	private String name;
	private ArrayList<Language> selectedLanguages;

	private UUID imageUUID;
}