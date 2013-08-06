package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.ImageUtils;

public class AddSpeakerActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_speaker);
		languages = FileIO.readDefaultLanguages();
		uuid = UUID.randomUUID();
		ImageButton okButton = (ImageButton) findViewById(R.id.okButton);
		okButton.setEnabled(false);
	}

	@Override
	public void onResume() {
		super.onResume();

		ArrayAdapter<Language> adapter =
				new SpeakerLanguagesArrayAdapter(this, languages);
		setListAdapter(adapter);
	}

	public void onAddLanguageButton(View view) {
		Intent intent = new Intent(this, LanguageFilterList.class);
		startActivityForResult(intent, SELECT_LANGUAGE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent
			intent) {
		if (requestCode == SELECT_LANGUAGE) {
			if (resultCode == RESULT_OK) {
				Language language =
						(Language) intent.getParcelableExtra("language");
				if (!languages.contains(language)) {
					languages.add(language);
				}
			}
		} else if (requestCode == PHOTO_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				handleSmallCameraPhoto();
			}
		}
	}

	private void handleSmallCameraPhoto() {
		Bitmap image;
		try {
			ImageUtils.createSmallSpeakerImage(this.uuid);
			image = ImageUtils.getSmallImage(this.uuid);
		} catch (IOException e) {
			image = null;
		}
		ImageView speakerImage = (ImageView) findViewById(R.id.speakerImage);
		speakerImage.setImageBitmap(image);
		if (image != null) {
			speakerHasImage = true;
			ImageButton okButton = (ImageButton) findViewById(R.id.okButton);
			okButton.setEnabled(true);
		} else {
			speakerHasImage = false;
		}
	}

	public void onCancelButtonPressed(View view) {
		this.finish();
	}

	public void onOkButtonPressed(View view) {
		EditText textField = (EditText) findViewById(R.id.Name);
		String name = textField.getText().toString();
		Speaker newSpeaker = new Speaker(uuid, name, languages);
		if (speakerHasImage) {
			try {
				newSpeaker.write();
			} catch (IOException e) {
				Toast.makeText(this, "Failed to write the Speaker to file",
						Toast.LENGTH_LONG).show();
			}
			this.finish();
		} else {
			Toast.makeText(this, "Each speaker needs an image. Take a photo!",
					Toast.LENGTH_LONG).show();
		}
	}

	public void takePhoto(View view) {
		dispatchTakePictureIntent(PHOTO_REQUEST_CODE);
	}

	private void dispatchTakePictureIntent(int actionCode) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		try {
			File imageFile = ImageUtils.getImageFile(this.uuid);
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(imageFile));
		} catch (Exception e) {
			Toast.makeText(this, "Failed to take a photo.",
					Toast.LENGTH_LONG).show();
		}

		startActivityForResult(takePictureIntent, actionCode);
	}

	static final int SELECT_LANGUAGE = 0;
	static final int PHOTO_REQUEST_CODE = 1;
	private List<Language> languages = new ArrayList<Language>();
	private UUID uuid;
	private boolean speakerHasImage;
}
