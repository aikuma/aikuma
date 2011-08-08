package au.edu.melbuni.boldapp;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TimelineItemAdapter extends BaseAdapter {

	static class ViewReferences {
		ImageView userPicture;
		TextView text;
	}

	protected LayoutInflater inflater;
	protected Activity activity;
	protected ArrayList<Timeline> timelines;

	public TimelineItemAdapter(Activity activity, ArrayList<Timeline> timelines) {
		this.inflater = LayoutInflater.from(activity.getApplicationContext());
		this.activity = activity;
		this.timelines = timelines;
	}

	@Override
	public int getCount() {
		return timelines.size();
	}

	@Override
	public Object getItem(int position) {
		return timelines.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewReferences references;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.time_line_list_item, null);

			// Creates a ViewReferences and store references to the two children
			// views we want to bind data to.
			//
			references = new ViewReferences();
			references.userPicture = (ImageView) convertView.findViewById(R.id.userPicture);
			references.text = (TextView) convertView.findViewById(R.id.text);

			convertView.setTag(references);
		} else {
			// Get the ViewReferences back to get fast access to the TextView
			// and the ImageView.
			//
			references = (ViewReferences) convertView.getTag();
		}
		
		// Bind the data efficiently with the references.
		// TODO
		Timeline timeline = Bundler.getTimelines(activity).get(position);
		User user = timeline.getUser();
		if (user != null) {
			references.userPicture.setImageDrawable(user.getProfileImage());
		}
		references.text.setText(timeline.getItemText());
		 
		return convertView;
	}

}
