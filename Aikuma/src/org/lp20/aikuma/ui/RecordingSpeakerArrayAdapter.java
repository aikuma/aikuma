/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ImageView;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma2.R;

/**
 * The array adapter for dealing with lists of Speakers.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class RecordingSpeakerArrayAdapter extends ArrayAdapter<Speaker> {

	/**
	 * Constructor.
	 *
	 * @param	context	The context that the array adapter will be used in.
	 * @param	speakers	The list of speakers that the array adapter is to
	 * deal with.
	 * @param	selectedSpeakers	List of speakers selected for recording
	 * 
	 */
	public RecordingSpeakerArrayAdapter(Context context,
			List<Speaker> speakers, List<Speaker> selectedSpeakers) {
		super(context, LIST_ITEM_LAYOUT, speakers);
		this.selectedSpeakers = selectedSpeakers;
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View _, ViewGroup parent) {
		View speakerView =
				(View) inflater.inflate(LIST_ITEM_LAYOUT, parent, false);
		final Speaker speaker = getItem(position);
		TextView speakerNameView =
				(TextView) speakerView.findViewById(R.id.recordingSpeakerName);
		speakerNameView.setText(speaker.getName());
		TextView speakerLanguagesView =
				(TextView) speakerView.findViewById(R.id.recordingSpeakerLanguages);
		List<Language> languages = new
				ArrayList<Language>(speaker.getLanguages());
		if (languages.size() > 0) {
			String languageNames = languages.remove(0).getName();
			for (Language language : languages) {
				languageNames = languageNames + ", " + language.getName();
			}
			speakerLanguagesView.setText(languageNames);
		}
		ImageView speakerImage =
				(ImageView) speakerView.findViewById(R.id.recordingSpeakerImage);
		try {
			speakerImage.setImageBitmap(speaker.getSmallImage());
		} catch (IOException e) {
			// If the image can't be loaded, we just leave it at that.
		}
		
		
		CheckBox speakerCheckBox = (CheckBox)
				speakerView.findViewById(R.id.speakerCheckBox);
		
		if (selectedSpeakers.contains(speaker)) {
			speakerCheckBox.setChecked(true);
		}
		
		speakerCheckBox.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				boolean checked = ((CheckBox) view).isChecked();
				if (checked) {
					selectedSpeakers.add(speaker);
					checked = true;
				} else {
					selectedSpeakers.remove(speaker);
					checked = false;
				}
				updateActivityState();
			}
		});
		
		
		return speakerView;
	}
	
	/**
	 * Overriden by the Activity which makes an instance of this class
	 * When the state of a checkbox changes, 
	 * This function will change the Activity's state
	 */
	public void updateActivityState() {}
	

	private static final int LIST_ITEM_LAYOUT = 
			R.layout.recordingspeaker_list_item;
	private LayoutInflater inflater;
	private List<Speaker> selectedSpeakers;
}
