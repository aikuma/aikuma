package au.edu.unimelb.boldapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A ListActivity that presents users with recordings to choose from for both
 * respeaking and listening activities
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordingSelectionActivity extends ListActivity {

	/**
	 * The name of the activity to be started when a recording is selected
	 */
	private String nextActivityName;

	/**
	 * Called when the Activity is initially created.
	 *
	 * @param	savedInstanceState	Data the activity most recently supplied to
	 * onSaveInstanceState(Bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_selection);
		Intent intent = getIntent();
		nextActivityName = intent.getStringExtra("activity");
	}

	/**
	 * Called when the activity is started
	 */
	@Override
	public void onStart() {
		super.onStart();
		FileIO.loadRecordings();
		ArrayAdapter adapter = new RecordingArrayAdapter(this,
				GlobalState.getRecordings());
		setListAdapter(adapter);
	}

	/**
	 * When the list item is clicked.
	 *
	 * @param	l		the listview
	 * @param	v		the view that was clicked
	 * @param	positon	position in the list and array
	 * @param 	id		id
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Recording recording = (Recording) getListAdapter().getItem(position);
		//Otherwise it is listen
		Intent intent = new Intent(this, ListenActivity.class);
		if (nextActivityName.equals("RespeakActivity")) {
			intent = new Intent(this, RespeakActivity.class);
		} else if (nextActivityName.equals("TranslateActivity")) {
			intent = new Intent(this, TranslateActivity.class);
		} else if (!recording.isOriginal()) {
			// The activity is the Listen activity.  If the chosen recording is
			// not an original, open activity that gives the user a choice
			// between interleaved or not.
			intent = new Intent(this, InterleavedChoiceActivity.class);
		}
		intent.putExtra("recordingUUID", recording.getUuid());
		startActivity(intent);
	}

	/**
	 * Go back
	 *
	 * @param	view	the button that was pressed.
	 */
	public void goBack(View view) {
		this.finish();
	}

	/**
	 * Sort recordings alphabetically
	 *
	 * @param	view	the button that was pressed.
	 */
	public void sortAlphabetically(View view) {
		ArrayAdapter adapter = new RecordingArrayAdapter(this,
				GlobalState.getRecordings("alphabetical"));
		setListAdapter(adapter);
	}

	/**
	 * Sort recordings by date of creation
	 *
	 * @param	view	the button that was pressed.
	 */
	public void sortDate(View view) {
		ArrayAdapter adapter = new RecordingArrayAdapter(this,
				GlobalState.getRecordings("date"));
		setListAdapter(adapter);
	}
}
