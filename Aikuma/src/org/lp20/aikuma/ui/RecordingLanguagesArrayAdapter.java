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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.IOException;
import java.util.List;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;

/**
 * An array adapter for lists of languages associated with Recordings.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordingLanguagesArrayAdapter extends ArrayAdapter<Language> {

	/**
	 * Default constructor.
	 *
	 * @param	context	The application context.
	 * @param	languages	The list of languages to be dealt with.
	 * @param	selectedLanguages	The subset of languages that are to be
	 * selected by default.
	 */
	public RecordingLanguagesArrayAdapter(Context context, List<Language>
			languages, List<Language> selectedLanguages) {
		super(context, LIST_ITEM_LAYOUT, languages);
		this.languages = languages;
		this.selectedLanguages = selectedLanguages;
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
		CheckBox langCheckBox = (CheckBox)
				recordingView.findViewById(R.id.langCheckBox);

		recordingNameView.setText(language.toString());

		if (selectedLanguages.contains(language)) {
			langCheckBox.setChecked(true);
		}

		langCheckBox.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				boolean checked = ((CheckBox) view).isChecked();
				if (checked) {
					selectedLanguages.add(language);
					checked = true;
				} else {
					selectedLanguages.remove(language);
					checked = false;
				}
			}
		});

		return recordingView;
	}

	private static final int LIST_ITEM_LAYOUT =
			R.layout.recordinglanguages_list_item;
	private LayoutInflater inflater;
	private List<Language> languages;
	private List<Language> selectedLanguages;
}
