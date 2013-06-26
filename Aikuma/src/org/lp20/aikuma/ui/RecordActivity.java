package org.lp20.aikuma.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.R;

/**
 * The activity that allows audio to be recorded
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordActivity extends Activity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		/*Do some stuff*/
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.record:
				new AlertDialog.Builder(this)
						.setMessage(R.string.restart_recording)
						.setPositiveButton(R.string.discard, new
						DialogInterface.OnClickListener() {
						
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent =
									new Intent(RecordActivity.this,
												RecordActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
								startActivity(intent);
							}
						})
						.setNegativeButton(R.string.cancel, null)
						.show();
				return true;
			case R.id.mainlist:
				Log.i("ra", "okyo");
				new AlertDialog.Builder(this)
						.setMessage(R.string.discard_dialog)
						.setPositiveButton(R.string.discard, new
						DialogInterface.OnClickListener() {
						
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent =
									new Intent(RecordActivity.this,
												MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}
						})
						.setNegativeButton(R.string.cancel, null)
						.show();
				return true;
			case R.id.settings:
				Log.i("ra", "nolo");
				new AlertDialog.Builder(this)
						.setMessage(R.string.discard_dialog)
						.setPositiveButton(R.string.discard, new
						DialogInterface.OnClickListener() {
						
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent =
									new Intent(RecordActivity.this,
												SettingsActivity.class);
								RecordActivity.this.finish();
								startActivity(intent);
							}
						})
						.setNegativeButton(R.string.cancel, null)
						.show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void onRecordButton(View view) {
		ImageButton recordButton =
				(ImageButton) findViewById(R.id.recordButton);
		LinearLayout pauseAndStopButtons =
				(LinearLayout) findViewById(R.id.pauseAndStopButtons);
		recordButton.setVisibility(View.GONE);
		pauseAndStopButtons.setVisibility(View.VISIBLE);
	}

	public void onPauseButton(View view) {
		ImageButton recordButton =
				(ImageButton) findViewById(R.id.recordButton);
		LinearLayout pauseAndStopButtons =
				(LinearLayout) findViewById(R.id.pauseAndStopButtons);
		recordButton.setVisibility(View.VISIBLE);
		pauseAndStopButtons.setVisibility(View.GONE);
	}

	public void onStopButton(View view) {
		Intent intent = new Intent(this, RecordingMetadataActivity.class);
		startActivity(intent);
	}
}
