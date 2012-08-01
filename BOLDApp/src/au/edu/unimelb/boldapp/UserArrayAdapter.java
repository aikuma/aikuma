package au.edu.unimelb.boldapp;

import android.widget.TextView;

import android.widget.ArrayAdapter;
import android.content.Context;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

import android.R;

/**
 * Array adapter to take the ListView's getItem and getView requests and adapt
 * it to an array of users.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class UserArrayAdapter extends ArrayAdapter<User> {
	/**
	 * The simple layout used in rendering the individual list items
	 */
	protected static final int listItemLayout =
			android.R.layout.simple_list_item_1;
	
	/**
	 * The layout inflater used to convert the xml into a view.
	 */
	protected LayoutInflater inflater;

	/**
	 * Default constructor
	 * @param	context	Interface to global information about the application
	 * environment.
	 * @param	users	An array of users to be displayed.
	 */
	UserArrayAdapter(Context context, User[] users){
		super(context, listItemLayout, users);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Returns the respective view for an item in the list given a position.
	 *
	 * @param	position	The position of the user in the array and listView.
	 * @param	cachedView	Unused variable as of yet.
	 * @param	parent		The parent view (the listview).
	 */
	@Override
	public View getView(int position, View _cachedView, ViewGroup parent) {

		TextView userView = (TextView) inflater
				.inflate(listItemLayout, parent, false);
		User user = getItem(position);

		userView.setText(user.getName());

		return userView;
	}

}
