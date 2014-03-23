/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import org.apache.commons.io.FileUtils;

/**
 * The activity that allows audio to be recorded.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordingMetadataActivity extends AikumaListActivity {

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
		durationMsec = (Integer) intent.getExtras().get("durationMsec");
		originalId = (String)
				intent.getExtras().get("originalId");
		setUpPlayer(uuid, sampleRate);
		userImages =
				(LinearLayout) findViewById(R.id.userImagesAndAddUserButton);
		speakersIds = new ArrayList<String>();
		languages = new ArrayList<Language>();
		selectedLanguages = new ArrayList<Language>();
		okButton = (ImageButton) findViewById(R.id.okButton);
		updateOkButton();

		nameField = (EditText) findViewById(R.id.recordingDescription);
		nameField.addTextChangedListener(emptyTextWatcher);
	}

	// Prepares the player with the recording.
	private void setUpPlayer(UUID uuid, long sampleRate) {
		listenFragment = (ListenFragment)
				getFragmentManager().findFragmentById(R.id.ListenFragment);
		try {
			//listenFragment.setPlayer(new SimplePlayer(
			//		new File(FileIO.getNoSyncPath(), uuid.toString() + ".wav"),
			//		sampleRate, true));
			listenFragment.setPlayer(new SimplePlayer(
					new File(Recording.getNoSyncRecordingsPath(), uuid.toString() + ".wav"),
					sampleRate, true));
		} catch (IOException e) {
			//The SimplePlayer cannot be constructed, so let's end the
			//activity.
			Toast.makeText(this, "There has been an error in the creation of the audio file which prevents it from being read.", Toast.LENGTH_LONG).show();
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

	/**
	 * Starts the AddSpeakerActivity.
	 *
	 * @param	view	The AddSpeakerButton
	 */
	public void onAddUserButtonPressed(View view) {
		Intent intent =
			new Intent(RecordingMetadataActivity.this,
						SpeakersActivity.class);
		startActivityForResult(intent, ADD_SPEAKER);
	}

	/**
	 * Called when the user has indicated that the metadata is complete.
	 *
	 * @param	view	the OK button.
	 */
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
								findViewById(R.id.recordingDescription);
						String description =
								descriptionField.getText().toString();
						Date date = new Date();
						String androidID = Aikuma.getAndroidID();
						Log.i("duration", "when recording created: " + durationMsec);
						Recording recording = new Recording(
								uuid, description, date, selectedLanguages,
								speakersIds, androidID, originalId,
								sampleRate, durationMsec);
						try {
							// Move the wave file from the nosync directory to
							// the synced directory and write the metadata
							recording.write();
						} catch (IOException e) {
							Toast.makeText(RecordingMetadataActivity.this,
								"Failed to write the Recording metadata:\t" +
								e.getMessage(), Toast.LENGTH_LONG).show();
						}
						startActivity(intent);
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	/**
	 * When the user wants to cancel saving the metadata.
	 *
	 * @param	view	The cancel button.
	 */
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

	// Used to recieve the results of taking photos and adding speakers.
	@Override
	protected void onActivityResult(
			int requestCode, int resultCode, Intent intent) {
		if (requestCode == ADD_SPEAKER) {
			if (resultCode == RESULT_OK) {
				Speaker speaker = intent.getParcelableExtra("speaker");
				if (!speakersIds.contains(speaker.getId())) {
					speakersIds.add(speaker.getId());
					for (Language language : speaker.getLanguages()) {
						if (!languages.contains(language)) {
							languages.add(language);
						}
					}
					selectedLanguages = new ArrayList<Language>();
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
					recordingHasSpeaker = true;
					updateOkButton();
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

	// Used to check whether the recording name is long enough based on what
	// the user is entered, and disable/enable the okButton on the fly.
	private TextWatcher emptyTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
		}
		public void beforeTextChanged(CharSequence s,
				int start, int count, int after) {
		}
		public void onTextChanged(CharSequence s,
				int start, int before, int count) {
			if (s.length() == 0) {
				recordingHasName = false;
				updateOkButton();
			} else {
				recordingHasName = true;
				updateOkButton();
			}
		}
	};

	/**
	 * Disables or enables the OK button depending on whether the recording now
	 * has a speaker and name.
	 */
	private void updateOkButton() {
		if (recordingHasSpeaker && recordingHasName) {
			okButton.setImageResource(R.drawable.ok_48);
			okButton.setEnabled(true);
		} else {
			okButton.setImageResource(R.drawable.ok_disabled_48);
			okButton.setEnabled(false);
		}
	}

	static final int ADD_SPEAKER = 0;
	private UUID uuid;
	private Recording recording;
	private List<String> speakersIds;
	private List<Language> languages;
	private List<Language> selectedLanguages;
	private LinearLayout userImages;
	private long sampleRate;
	private int durationMsec;
	private ListenFragment listenFragment;
	private String originalId;
	private EditText nameField;
	private ImageButton okButton;
	private boolean recordingHasSpeaker;
	private boolean recordingHasName;
}
