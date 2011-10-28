package au.edu.melbuni.boldapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import au.edu.melbuni.boldapp.models.Segments;
import au.edu.melbuni.boldapp.models.*;

public class SegmentItemAdapter extends BaseAdapter {

	static class ViewReferences {
		TextView segment;
	}

	protected LayoutInflater inflater;
	protected Segments segments;

	public SegmentItemAdapter(Activity activity, Segments segments) {
		this.inflater = LayoutInflater.from(activity.getApplicationContext());
		this.segments = segments;
		notifyDataSetChanged();
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

			// Creates a ViewReferences and store references to the child
			// view we want to bind data to.
			//
			references = new ViewReferences();
			references.segment = (TextView) convertView
					.findViewById(R.id.segment);

			Segment segment = segments.get(position);
			
			System.out.println("Adding Observer");
			
			segment.addObserver(new Segment.ViewHandler(references.segment));
		} else {
			// Get the ViewReferences back to get fast access to the TextView
			// and the ImageView.
			//
			references = (ViewReferences) convertView.getTag();
		}

		return convertView;
	}

}
