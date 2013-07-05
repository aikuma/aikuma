package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.lp20.aikuma.R;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.Recording;

/**
 * The activity that allows audio to be recorded
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordingMetadataActivity extends Activity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_metadata);
		Intent intent = getIntent();
		UUID uuid = UUID.fromString(
				(String) intent.getExtras().get("uuidString"));
		int sampleRate = (Integer) intent.getExtras().get("sampleRate");
		ListenFragment fragment = (ListenFragment)
				getFragmentManager().findFragmentById(R.id.ListenFragment);
		try {
			setRecording(Recording.read(uuid));
			fragment.setRecording(getRecording());
		} catch (IOException e) {
			//If there is an issue reading the recording metadata, then we'll
			//just try use the wav file alone.
			File wavFile = new File(Recording.getRecordingsPath(),
						uuid.toString() + ".wav");
			fragment.setUUID(uuid);
			fragment.setSampleRate(sampleRate);
		}
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
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
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

	private void setRecording(Recording recording) {
		this.recording = recording;
	}

	private Recording getRecording() {
		return this.recording;
	}

	private UUID uuid;
	private Recording recording;
}
