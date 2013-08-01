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

public class RecordingLanguagesArrayAdapter extends ArrayAdapter<Language> {
	public RecordingLanguagesArrayAdapter(Context context, List<Language>
			languages, List<Language> selectedLanguages) {
		super(context, listItemLayout, languages);
		this.languages = languages;
		this.selectedLanguages = selectedLanguages;
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View _, ViewGroup parent) {
		View recordingView =
				(View) inflater.inflate(listItemLayout, parent, false);
		final Language language = getItem(position);
		TextView recordingNameView =
				(TextView) recordingView.findViewById(R.id.recordingName);
		final ImageButton removeLanguageButton =
				(ImageButton) recordingView.findViewById(R.id.removeButton);
		removeLanguageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (checked) {
					removeLanguageButton.setImageResource(
							R.drawable.ok_disabled_32);
					selectedLanguages.remove(language);
					checked = false;
				} else {
					removeLanguageButton.setImageResource(R.drawable.ok_32);
					selectedLanguages.add(language);
					checked = true;
				}
				Log.i("recordinglangs", "languages: " + languages);
				Log.i("recordinglangs", "selectedLanguages: " + selectedLanguages);
				//notifyDataSetChanged();
			}
			private boolean checked = true;
		});

		recordingNameView.setText(language.toString());
		return recordingView;
	}

	private static final int listItemLayout =
			R.layout.recordinglanguages_list_item;
	private LayoutInflater inflater;
	private List<Language> languages;
	private List<Language> selectedLanguages;
}
