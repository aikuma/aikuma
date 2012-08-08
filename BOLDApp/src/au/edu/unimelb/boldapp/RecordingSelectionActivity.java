package au.edu.unimelb.boldapp;

import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.util.Log;

public class RecordingSelectionActivity extends ListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recording_selection);
		FileIO.loadRecordings();
		ArrayAdapter adapter = new RecordingArrayAdapter(this,
				GlobalState.getRecordings());
		Log.i("yep", " " + GlobalState.getRecordings()[0].getName());
		setListAdapter(adapter);
	}

	/*
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Recording recording = (Recording) getListAdapter().getItem(position);
	}
	*/
}
