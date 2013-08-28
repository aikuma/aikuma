package org.lp20.aikuma;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.ui.ListenActivity;
import org.lp20.aikuma.ui.MenuBehaviour;
import org.lp20.aikuma.ui.RecordActivity;
import org.lp20.aikuma.ui.RecordingArrayAdapter;
import org.lp20.aikuma.ui.SettingsActivity;
import org.lp20.aikuma.util.SyncUtil;

public class MainActivity extends ListActivity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		menuBehaviour = new MenuBehaviour(this);
		try {
			Log.i("sync", "syncUtil null: " + (syncUtil == null));
			if (syncUtil == null) {
				syncUtil = new SyncUtil();
			}
		} catch (IOException e) {
			//If an IOException is thrown here, the ServerCredentials probably
			//couldn't be read.
		}
		List<Recording> recordings = Recording.readAll();
		ArrayAdapter adapter = new RecordingArrayAdapter(this, recordings);
		setListAdapter(adapter);
		Aikuma.loadLanguages();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return menuBehaviour.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return menuBehaviour.onOptionsItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		Recording recording = (Recording) getListAdapter().getItem(position);
		Intent intent = new Intent(this, ListenActivity.class);
		intent.putExtra("uuidString", recording.getUUID().toString());
		startActivity(intent);
	}

	MenuBehaviour menuBehaviour;
	private static SyncUtil syncUtil;
}
