package au.edu.melbuni.boldapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.adapters.RespeakItemAdapter;

public class RespeakSelectionActivity extends BoldActivity {

	static final int RESPEAK_CONFIRMED = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		configureView(savedInstanceState);
		installBehavior(savedInstanceState);
	}

	@Override
	protected void onResume() {
		ListView respeakOriginalsListView = (ListView) findViewById(R.id.originals);
		((RespeakItemAdapter) respeakOriginalsListView.getAdapter())
				.notifyDataSetChanged();

		super.onResume();
	}

	@Override
	public void configureView(Bundle savedInstanceState) {
		super.configureView(savedInstanceState);

		setContent(R.layout.respeak_selection);
	};

	public void installBehavior(Bundle savedInstanceState) {

		ListView originalsListView = (ListView) findViewById(R.id.originals);
		originalsListView.setAdapter(new RespeakItemAdapter(this, Bundler
				.getRespeakOriginals(this)));

		originalsListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
//						Bundler.setCurrentTimeline(
//								RespeakSelectionActivity.this,
//								(Timeline) Bundler.getTimelines(
//										RespeakSelectionActivity.this).toArray()[position]);
						startActivityForResult(new Intent(
								getApplicationContext(), RespeakActivity.class),
								0);
					}
				});
	}

}
