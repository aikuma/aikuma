package org.lp20.aikuma;

import android.app.ActionBar;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.model.Recording;

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
}
