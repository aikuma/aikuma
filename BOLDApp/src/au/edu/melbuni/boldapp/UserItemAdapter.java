package au.edu.melbuni.boldapp;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserItemAdapter extends BaseAdapter {

	static class ViewReferences {
		ImageView picture;
		TextView name;
	}

	protected LayoutInflater inflater;
	protected Activity activity;
	protected ArrayList<User> users;

	public UserItemAdapter(Activity activity, ArrayList<User> users) {
		this.inflater = LayoutInflater.from(activity.getApplicationContext());
		this.activity = activity;
		this.users = users;
	}

	@Override
	public int getCount() {
		return users.size();
	}

	@Override
	public Object getItem(int position) {
		return users.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Ugh?
		//
		return new Long(users.get(position).hashCode()).longValue();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewReferences references;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.user_list_item, null);

			// Creates a ViewReferences and store references to the two children
			// views we want to bind data to.
			//
			references = new ViewReferences();
			references.picture = (ImageView) convertView
					.findViewById(R.id.userPicture);
			references.name = (TextView) convertView
					.findViewById(R.id.userName);

			// convertView.setOnClickListener(new OnClickListener() {
			// private int pos = position;
			//
			// @Override
			// public void onClick(View v) {
			// Toast.makeText(context, "Click-" + String.valueOf(pos),
			// Toast.LENGTH_SHORT).show();
			// }
			// });
			//
			// holder.buttonLine.setOnClickListener(new OnClickListener() {
			// private int pos = position;
			//
			// @Override
			// public void onClick(View v) {
			// Toast.makeText(context, "Delete-" + String.valueOf(pos),
			// Toast.LENGTH_SHORT).show();
			//
			// }
			// });

			convertView.setTag(references);
		} else {
			// Get the ViewReferences back to get fast access to the TextView
			// and the ImageView.
			//
			references = (ViewReferences) convertView.getTag();
		}
		 
		// Bind the data efficiently with the holder.
		User user = Bundler.getUsers(activity).get(position);
		references.picture.setImageDrawable(user.getProfileImage());
		references.name.setText(user.name);
		 
		return convertView;
	}

}
