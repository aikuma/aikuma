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
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordingLanguagesArrayAdapter extends ArrayAdapter<Language> {
	public RecordingLanguagesArrayAdapter(Context context, List<Language>
			languages, List<Language> selectedLanguages) {
		super(context, LIST_ITEM_LAYOUT, languages);
		this.languages = languages;
		this.selectedLanguages = selectedLanguages;
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View _, ViewGroup parent) {
		Log.i("checkbox", "getView()");
		View recordingView =
				(View) inflater.inflate(LIST_ITEM_LAYOUT, parent, false);
		final Language language = getItem(position);
		TextView recordingNameView =
				(TextView) recordingView.findViewById(R.id.recordingName);
		CheckBox langCheckBox = (CheckBox)
				recordingView.findViewById(R.id.langCheckBox);
		if (selectedLanguages.contains(language)) {
			langCheckBox.setChecked(true);
		}
		langCheckBox.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				boolean checked = ((CheckBox) view).isChecked();
				if (checked) {
					selectedLanguages.add(language);
					checked = true;
					Log.i("checked", "now true");
				} else {
					//removeLanguageButton.setImageResource(
					//		R.drawable.ok_disabled_32);
					selectedLanguages.remove(language);
					checked = false;
					Log.i("checked", "now false");
				}
			}
		});

		recordingNameView.setText(language.toString());
		return recordingView;
	}

	private static final int LIST_ITEM_LAYOUT =
			R.layout.recordinglanguages_list_item;
	private LayoutInflater inflater;
	private List<Language> languages;
	private List<Language> selectedLanguages;
}
