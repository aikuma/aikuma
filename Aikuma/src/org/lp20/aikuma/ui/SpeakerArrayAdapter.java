/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma2.R;

/**
 * The array adapter for dealing with lists of Speakers.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SpeakerArrayAdapter extends ArrayAdapter<Speaker> {

	/**
	 * Constructor.
	 *
	 * @param	context	The context that the array adapter will be used in.
	 * @param	speakers	The list of speakers that the array adapter is to
	 * deal with.
	 */
	public SpeakerArrayAdapter(Context context, List<Speaker> speakers) {
		super(context, LIST_ITEM_LAYOUT, speakers);
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View _, ViewGroup parent) {
		View speakerView =
				(View) inflater.inflate(LIST_ITEM_LAYOUT, parent, false);
		Speaker speaker = getItem(position);
		TextView speakerNameView =
				(TextView) speakerView.findViewById(R.id.speakerName);
		TextView speakerOwnerView = 
				(TextView) speakerView.findViewById(R.id.speakerOwner);
		speakerNameView.setText(speaker.getName());
		if(!AikumaSettings.getCurrentUserId().equals(speaker.getOwnerId()))
			speakerOwnerView.setText("(" + speaker.getOwnerId() + ")");

		return speakerView;
	}

	private static final int LIST_ITEM_LAYOUT = R.layout.speaker_list_item;
	private LayoutInflater inflater;

}
