package au.edu.melbuni.boldapp;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

public class SegmentItemAdapter extends BaseAdapter {

	static class ViewReferences {
		Button button;
	}

	protected LayoutInflater inflater;
	protected Activity activity;
	protected ArrayList<Segment> segments;

	public SegmentItemAdapter(Activity activity, ArrayList<Segment> segments) {
		this.inflater = LayoutInflater.from(activity.getApplicationContext());
		this.activity = activity;
		this.segments = segments;
	}

	@Override
	public int getCount() {
		return segments.size();
	}

	@Override
	public Object getItem(int position) {
		return segments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewReferences references;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.segment_list_item, null);

			// Creates a ViewReferences and store references to the two children
			// views we want to bind data to.
			//
			references = new ViewReferences();
			references.button = (Button) convertView.findViewById(R.id.button);

			convertView.setTag(references);
		} else {
			// Get the ViewReferences back to get fast access to the TextView
			// and the ImageView.
			//
			references = (ViewReferences) convertView.getTag();
		}

		// Bind the data efficiently with the references.
		Segment segment = segments.get(position);
		segment.colorize(references.button);

		return convertView;
	}

}
