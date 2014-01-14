/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.IOException;
import java.util.List;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;

/**
 * An array adapter to handle the list of languages associated with speakers.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class SpeakerLanguagesArrayAdapter extends ArrayAdapter<Language> {

	/**
	 * Default constructor.
	 *
	 * @param	context	The application context.
	 * @param	languages	The list of languages to be dealt with.
	 */
	public SpeakerLanguagesArrayAdapter(Context context, List<Language>
			languages) {
		super(context, LIST_ITEM_LAYOUT, languages);
		this.languages = languages;
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Given a position, gets the appropriate list element as a View.
	 *
	 * @param	position	The position in the list view.
	 * @param	_	Unused.
	 * @param	parent	The parent ViewGroup.
	 * @return	The list element's corresponding view.
	 */
	@Override
	public View getView(int position, View _, ViewGroup parent) {
		View recordingView =
				(View) inflater.inflate(LIST_ITEM_LAYOUT, parent, false);
		final Language language = getItem(position);
		TextView recordingNameView = 
				(TextView) recordingView.findViewById(R.id.recordingName);
		ImageButton removeLanguageButton = 
				(ImageButton) recordingView.findViewById(R.id.removeButton);
		removeLanguageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				languages.remove(language);
				notifyDataSetChanged();
				/*
				try {
					FileIO.writeDefaultLanguages(languages);
				} catch (IOException e) {
					// If it can't be written, then not much can be done.
					// Perhaps toast the user.
				}
				*/
			}
		});

		recordingNameView.setText(language.toString());
		return recordingView;
	}

	private static final int LIST_ITEM_LAYOUT =
			R.layout.speakerlanguages_list_item;
	private LayoutInflater inflater;
	private List<Language> languages;

}
