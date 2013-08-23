package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.lp20.aikuma.R;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.model.Language;

/**
 * The activity that allows audio to be recorded
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordingMetadataActivity extends AikumaListActivity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_metadata);
		//Lets method in superclass know to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		safeActivityTransition = true;
		Intent intent = getIntent();
		uuid = UUID.fromString(
				(String) intent.getExtras().get("uuidString"));
		sampleRate = (Long) intent.getExtras().get("sampleRate");
		String originalUUIDString = (String) intent.getExtras().get("originalUUIDString");
		if (originalUUIDString != null) {
			originalUUID = UUID.fromString(originalUUIDString);
		}
		setUpPlayer(uuid, sampleRate);
		userImages =
				(LinearLayout) findViewById(R.id.userImagesAndAddUserButton);
		speakersUUIDs = new ArrayList<UUID>();
		languages = new ArrayList<Language>();
		selectedLanguages = new ArrayList<Language>();
	}

	private void setUpPlayer(UUID uuid, long sampleRate) {
		listenFragment = (ListenFragment)
				getFragmentManager().findFragmentById(R.id.ListenFragment);
		try {
			listenFragment.setPlayer(new SimplePlayer(
					new File(Recording.getRecordingsPath(), uuid.toString() + ".wav"),
					sampleRate));
		} catch (IOException e) {
			//The SimplePlayer cannot be constructed, so let's end the
			//activity.
			RecordingMetadataActivity.this.finish();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		ArrayAdapter<Language> adapter =
				new RecordingLanguagesArrayAdapter(this, languages,
						selectedLanguages);
		setListAdapter(adapter);
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setMessage(R.string.discard_dialog)
				.setPositiveButton(R.string.discard, new
				DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						Intent intent =
							new Intent(RecordingMetadataActivity.this,
										MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						RecordingMetadataActivity.this.finish();
						startActivity(intent);
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	public void onAddUserButtonPressed(View view) {
		Intent intent =
			new Intent(RecordingMetadataActivity.this,
						SpeakersActivity.class);
		startActivityForResult(intent, ADD_SPEAKER);
	}

	public void onOkButtonPressed(View view) {
		new AlertDialog.Builder(this)
				.setMessage(R.string.share_dialog)
				.setPositiveButton(R.string.share, new
				DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent =
								new Intent(RecordingMetadataActivity.this,
										MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						EditText descriptionField = (EditText)
								findViewById(R.id.description);
						String description =
								descriptionField.getText().toString();
						Date date = new Date();
						String androidID = Aikuma.getAndroidID();
						Recording recording = new Recording(
								uuid, description, date, selectedLanguages, 
								speakersUUIDs, androidID, originalUUID, sampleRate);
						try {
							recording.write();
						} catch (IOException e) {
							Toast.makeText(RecordingMetadataActivity.this,
								"Failed to write the Recording metadata.",
								Toast.LENGTH_LONG).show();
						}
						startActivity(intent);
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}
	public void onCancelButtonPressed(View view) {
		new AlertDialog.Builder(this)
				.setMessage(R.string.discard_dialog)
				.setPositiveButton(R.string.discard, new
				DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent =
								new Intent(RecordingMetadataActivity.this,
										MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	protected void onActivityResult(
			int requestCode, int resultCode, Intent intent) {
		if (requestCode == ADD_SPEAKER) {
			if (resultCode == RESULT_OK) {
				Speaker speaker = intent.getParcelableExtra("speaker");
				if (!speakersUUIDs.contains(speaker.getUUID())) {
					speakersUUIDs.add(speaker.getUUID());
					for (Language language : speaker.getLanguages()) {
						if (!languages.contains(language)) {
							languages.add(language);
						}
					}
					selectedLanguages = new ArrayList<Language>(languages);
					ImageView speakerImage = new ImageView(this);
					speakerImage.setAdjustViewBounds(true);
					speakerImage.setMaxHeight(60);
					speakerImage.setMaxWidth(60);
					speakerImage.setPaddingRelative(5,5,5,5);
					try {
						speakerImage.setImageBitmap(speaker.getSmallImage());
					} catch (IOException e) {
						// If the image can't be loaded, we just leave it at that.
					}
					userImages.addView(speakerImage);
				}
			}
		}
	}
	private void setRecording(Recording recording) {
		this.recording = recording;
	}

	private Recording getRecording() {
		return this.recording;
	}

	static final int ADD_SPEAKER = 0;
	private UUID uuid;
	private Recording recording;
	private List<UUID> speakersUUIDs;
	private List<Language> languages;
	private List<Language> selectedLanguages;
	private LinearLayout userImages;
	private long sampleRate;
	private ListenFragment listenFragment;
	private UUID originalUUID;
	private MenuBehaviour menuBehaviour;
}
