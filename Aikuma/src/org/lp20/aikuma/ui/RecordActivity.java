package org.lp20.aikuma.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
