package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.List;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.R;

public class SpeakersActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speakers);
		menuBehaviour = new MenuBehaviour(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return menuBehaviour.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return menuBehaviour.onOptionsItemSelected(item,
				"This will discard the recording. Are you sure?",
				"Discard", "Cancel");
	}

	@Override
	public void onResume() {
		super.onResume();

		speakers = Speaker.readAll();
		ArrayAdapter<Speaker> adapter =
				new SpeakerArrayAdapter(this, speakers);
		setListAdapter(adapter);
	}

	public void addSpeakerButtonPressed(View view) {
		Intent intent = new Intent(this, AddSpeakerActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent();
		intent.putExtra("speaker", (Speaker)l.getItemAtPosition(position));
		setResult(RESULT_OK, intent);
		this.finish();
	}

	private List<Speaker> speakers;
	private MenuBehaviour menuBehaviour;

}
