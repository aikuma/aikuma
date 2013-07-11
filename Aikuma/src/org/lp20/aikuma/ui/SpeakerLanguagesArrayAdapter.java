package org.lp20.aikuma.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.R;

public class SpeakerLanguagesArrayAdapter extends ArrayAdapter<Language> {
	public SpeakerLanguagesArrayAdapter(Context context, List<Language>
			languages) {
		super(context, listItemLayout, languages);
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View _, ViewGroup parent) {
		View recordingView =
				(View) inflater.inflate(listItemLayout, parent, false);
		Language language = getItem(position);
		TextView recordingNameView = 
				(TextView) recordingView.findViewById(R.id.recordingName);
		recordingNameView.setText(language.toString());
		return recordingView;
	}

	private static final int listItemLayout =
			R.layout.speakerlanguages_list_item;
	private LayoutInflater inflater;

}
