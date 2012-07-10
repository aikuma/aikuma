package au.edu.melbuni.boldapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.adapters.TimelineItemAdapter;
import au.edu.melbuni.boldapp.models.Timeline;

public class OriginalSelectionActivity extends BoldActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		configureView(savedInstanceState);
		installBehavior(savedInstanceState);
	}

	@Override
	protected void onResume() {
		ListView timelinesListView = (ListView) findViewById(R.id.timelines);
		((TimelineItemAdapter) timelinesListView.getAdapter())
				.notifyDataSetChanged();

		super.onResume();
	}

	@Override
	public void configureView(Bundle savedInstanceState) {
		super.configureView(savedInstanceState);

		setContent(R.layout.original_selection);
	};

	public void installBehavior(Bundle savedInstanceState) {
		ListView timelinesListView = (ListView) findViewById(R.id.timelines);
		timelinesListView.setAdapter(new TimelineItemAdapter(this, Bundler
				.getTimelines(this)));

		timelinesListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Bundler.setCurrentTimeline(
								OriginalSelectionActivity.this,
								(Timeline) Bundler.getTimelines(
										OriginalSelectionActivity.this).toArray()[position]);
						startActivityForResult(new Intent(
								getApplicationContext(), ListenActivity.class),
								0);
					}
				});

		// timelinesListView
		// .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		// {
		// @Override
		// public boolean onItemLongClick(AdapterView<?> parent,
		// View view, int position, long id) {
		// startActivityForResult(
		// new Intent(getApplicationContext(),
		// ListenActivity.class), 0);
		// return false;
		// }
		// });
	};
}
