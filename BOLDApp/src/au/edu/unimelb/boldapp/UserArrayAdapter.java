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

public class UserArrayAdapter extends ArrayAdapter<User> {
	protected static final int listItemLayout = 
			android.R.layout.simple_list_item_1; 
	
	protected LayoutInflater inflater;

	UserArrayAdapter(Context context, User[] users){
		super(context, listItemLayout, users);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View _cachedView, ViewGroup parent) {

		TextView userView = (TextView) inflater
				.inflate(listItemLayout, parent, false);
		User user = getItem(position);

		userView.setText(user.getName());

		return userView;
	}

}
