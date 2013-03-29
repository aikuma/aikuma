package au.edu.unimelb.aikuma;

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
		GlobalState.loadRecordings();
		GlobalState.loadUsers();
		ArrayAdapter adapter = new RecordingArrayAdapter(this,
				GlobalState.getRecordings().toArray(new Recording[0]));
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
		Intent intent = new Intent(this, InterleavedChoiceActivity.class);
		if (recording.isOriginal() && nextActivityName.equals("ListenActivity")) {
			intent = new Intent(this, ListenActivity.class);
		}
		if (nextActivityName.equals("RespeakActivity")) {
			intent = new Intent(this, RespeakActivity.class);
		}
		if (nextActivityName.equals("RespeakActivity2")) {
			intent = new Intent(this, RespeakActivity2.class);
		}
		//Otherwise it is listen
		intent.putExtra("recordingUUID", recording.getUUID());
		startActivity(intent);
		this.finish();
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
				GlobalState.getRecordings("alphabetical").toArray(new
				Recording[0]));
		setListAdapter(adapter);
	}

	/**
	 * Sort recordings by date of creation
	 *
	 * @param	view	the button that was pressed.
	 */
	public void sortDate(View view) {
		ArrayAdapter adapter = new RecordingArrayAdapter(this,
				GlobalState.getRecordings("date").toArray(new Recording[0]));
		setListAdapter(adapter);
	}

	/**
	 * Sort recordings by number of likes
	 */
	public void sortLikes(View view) {
		ArrayAdapter adapter = new RecordingArrayAdapter(this,
				GlobalState.getRecordings("likes").toArray(new Recording[0]));
		setListAdapter(adapter);
	}
}
