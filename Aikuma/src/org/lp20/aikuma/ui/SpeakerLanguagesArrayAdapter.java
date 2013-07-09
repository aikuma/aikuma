package org.lp20.aikuma.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.R;

public class SpeakerLanguagesArrayAdapter extends ArrayAdapter<Language> {
	SpeakerLanguagesArrayAdapter(Context context, List<Language> languages) {
		super(context, listItemLayout, languages);
		Log.i("aa", "OKAY");
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Log.i("aa", "OKAY2");
	}

	@Override
	public View getView(int position, View _, ViewGroup parent) {
		Log.i("aa", "OKAY3");
		View speakerLanguageView =
				(View) inflater.inflate(listItemLayout, parent, false);
		Language language = getItem(position);
		TextView languageText = 
				(TextView) speakerLanguageView.findViewById(R.id.languageText);
		languageText.setText(language.toString());
		Log.i("aa", "OKAY4");
		return languageText;
	}

	private static final int listItemLayout = R.layout.speakerlanguages_list_item;
	private LayoutInflater inflater;

}
