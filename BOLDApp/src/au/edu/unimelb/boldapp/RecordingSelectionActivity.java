package au.edu.unimelb.boldapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RecordingSelectionActivity extends ListActivity {

	/**
	 * The name of the activity to be started when a recording is selected
	 */
	private String nextActivityName;

	/**
	 * Initialization when the activity starts.
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
		}
		intent.putExtra("recordingUUID", recording.getUuid());
		startActivity(intent);
		RecordingSelectionActivity.this.finish();
	}
}
