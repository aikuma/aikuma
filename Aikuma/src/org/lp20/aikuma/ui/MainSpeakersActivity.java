/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma2.R;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class MainSpeakersActivity extends AikumaListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speakers);
		//Lets method in superclass know to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		safeActivityTransition = false;
	}

	@Override
	public void onResume() {
		super.onResume();

		speakers = Speaker.readAll(AikumaSettings.getCurrentUserId());
		Collections.reverse(speakers);

		ArrayAdapter<Speaker> adapter =
				new SpeakerArrayAdapter(this, speakers);
		setListAdapter(adapter);
	}

	/**
	 * Starts the AddSpeakerActivity.
	 *
	 * @param	_view	The add-speaker button that was pressed.
	 */
	public void addSpeakerButtonPressed(View _view) {
		Intent intent = new Intent(this, AddSpeakerActivity1.class);
		intent.putExtra("origin", 0);
		startActivity(intent);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		/*
		Intent intent = new Intent();
		intent.putExtra("speaker", (Speaker)l.getItemAtPosition(position));
		setResult(RESULT_OK, intent);
		this.finish();
		*/
	}
	
	/**
	 * When new speaker is added,
	 * scroll down to the bottom to show the new user
	 */
	private void scrollMyListViewToBottom() {
		final ListView listView = getListView();
		listView.post(new Runnable() {
	        @Override
	        public void run() {
	            // Select the last row so it will scroll into view...
	            listView.setSelection(getListAdapter().getCount() - 1);
	        }
	    });
	}

	private List<Speaker> speakers;
}
