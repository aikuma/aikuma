package au.edu.unimelb.aikuma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

import android.widget.ArrayAdapter;
import android.R;
import android.view.LayoutInflater;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Array adapter to take the ListView's getItem and getView requesta and adapt
 * it to an array of recordings.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordingArrayAdapter extends ArrayAdapter<Recording> {
	/**
	 * The simple layout used in rendering the individual list items.
	 */
	protected static final int listItemLayout =
			android.R.layout.simple_list_item_1;

	/**
	 * The layout inflater used to convert the xml into a view.
	 */
	protected LayoutInflater inflater;

	/**
	 * Default Constructor
	 *
	 * @param	context	Interface to global information about the application
	 * environment
	 * @param	recordings	An array of recordings to be displayed.
	 */
	RecordingArrayAdapter(Context context, Recording[] recordings) {
		super(context, listItemLayout, recordings);
		inflater = (LayoutInflater) context.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Returns the respective view for an item in the list given a position.
	 *
	 * @param	position	The position of the recording in the array and
	 * listView.
	 * @param	cachedView	Unused variable as of yet.
	 * @param	parent		The parent view (the listView).
	 */
	 @Override
	 public View getView(int position, View _cachedView, ViewGroup parent) {
	 	TextView recordingView = (TextView) inflater.
				inflate(listItemLayout, parent, false);
		Recording recording = getItem(position);
		if (recording.getName().equals("")) {
			recordingView.setText(recording.getUuid().toString() + " (" +
			recording.getLikes() + " likes)");
		} else {
			recordingView.setText(recording.getName() + " (" +
			recording.getLikes() + " likes)");
		}

		return recordingView;
	 }
}
