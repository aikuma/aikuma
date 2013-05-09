package org.lp20.aikuma;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.ui.ListenActivity;

public class MainActivity extends ListActivity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//ActionBar actionBar = getActionBar();
		//actionBar.setDisplayShowHomeEnabled(false);
		//actionBar.setDisplayShowTitleEnabled(false);

		List<Recording> recordings = Recording.readAll();
		ArrayAdapter adapter = new RecordingArrayAdapter(this, recordings);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		Recording recording = (Recording) getListAdapter().getItem(position);
		Intent intent = new Intent(this, ListenActivity.class);
		intent.putExtra("uuidString", recording.getUUID().toString());
		startActivity(intent);
	}
}
