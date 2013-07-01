package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.AlertDialog;
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
import java.util.UUID;
import org.lp20.aikuma.R;
import org.lp20.aikuma.MainActivity;

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
		uuid = UUID.fromString(
				getIntent().getCharSequenceExtra("uuidString").toString());
		Log.i("rma", "uuid: " + uuid);
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

	private UUID uuid;
}
