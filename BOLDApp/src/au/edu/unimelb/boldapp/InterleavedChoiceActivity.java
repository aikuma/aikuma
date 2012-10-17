package au.edu.unimelb.boldapp;

import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

/**
 * The activity that starts after a user has chosen a respeaking to listen to
 * and offers them a choice between interleaving with the original or just
 * listening to the respeaking.
 *
 * @author	Oliver Adams<oliver.adams@gmail.com>
 * @author	Florian Hanke<florian.hanke@gmail.com>
 */
public class InterleavedChoiceActivity extends Activity {

	/**
	 * The UUID of the audio file to be played.
	 */
	private UUID recordingUUID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interleaved_choice);
		Intent intent = getIntent();
		recordingUUID = (UUID) intent.getExtras().get("recordingUUID");
	}

	public void goBack(View _) {
		this.finish();
	}

	public void simple(View _) {
		Intent intent = new Intent(this, ListenActivity.class);
		intent.putExtra("recordingUUID", recordingUUID);
		intent.putExtra("interleavedChoice", false);
		startActivity(intent);
	}

	public void interleaved(View _) {
		Intent intent = new Intent(this, ListenActivity.class);
		intent.putExtra("recordingUUID", recordingUUID);
		intent.putExtra("interleavedChoice", true);
		startActivity(intent);
	}
}
