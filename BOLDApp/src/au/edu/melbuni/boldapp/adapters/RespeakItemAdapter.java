package au.edu.melbuni.boldapp.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.models.RespeakOriginal;
import au.edu.melbuni.boldapp.models.RespeakOriginals;

public class RespeakItemAdapter extends BaseAdapter {

	static class ViewReferences {
		TextView name;
	}

	protected LayoutInflater inflater;
	protected Activity activity;
	protected RespeakOriginals originals;

	public RespeakItemAdapter(Activity activity, RespeakOriginals originals) {
		this.inflater = LayoutInflater.from(activity.getApplicationContext());
		this.activity = activity;
		this.originals = originals;
	}

	@Override
	public int getCount() {
		return originals.size();
	}

	@Override
	public Object getItem(int position) {
		return originals.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewReferences references;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.respeak_list_item, null);

			// Creates a ViewReferences and store references to the two children
			// views we want to bind data to.
			//
			references = new ViewReferences();
			references.name = (TextView) convertView
					.findViewById(R.id.respeakName);

			convertView.setTag(references);
		} else {
			// Get the ViewReferences back to get fast access to the TextView
			// and the ImageView.
			//
			references = (ViewReferences) convertView.getTag();
		}

		// Bind the data efficiently with the references.
//		User user = Bundler.getUsers(activity).get(position);
		RespeakOriginal original = Bundler.getRespeakOriginals(activity).get(position);
//		references.name.setText(user.name);
		references.name.setText(original.relativeFilename);
		 
		return convertView;
	}

}
