package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
				languages.add((Language)
						intent.getParcelableExtra("language"));
			}
		} else if (requestCode == PHOTO_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Log.i("addspeaker", "woot");
			}
		}
	}

	public void onOkButtonPressed(View view) {
		EditText textField = (EditText) findViewById(R.id.Name);
		String name = textField.getText().toString();
		Speaker newSpeaker = new Speaker(uuid, name, languages);
		Log.i("addspeaker", "newSpeaker: " + newSpeaker);
		try {
			newSpeaker.write();
		} catch (IOException e) {
			Toast.makeText(this, "Failed to write the Speaker to file",
					Toast.LENGTH_LONG).show();
		}
		this.finish();
	}

	public void takePhoto(View view) {
		dispatchTakePictureIntent(PHOTO_REQUEST_CODE);
	}

	private void dispatchTakePictureIntent(int actionCode) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		String imageFilename = this.uuid.toString() + ".jpg";
		try {
			File image = new File(ImageUtils.getImagesPath(), imageFilename);
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(image));
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
}
