package org.lp20.aikuma.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import java.io.IOException;
import java.util.List;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.R;

public class SpeakerArrayAdapter extends ArrayAdapter<Speaker> {
	public SpeakerArrayAdapter(Context context, List<Speaker> speakers) {
		super(context, listItemLayout, speakers);
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View _, ViewGroup parent) {
		View speakerView =
				(View) inflater.inflate(listItemLayout, parent, false);
		Speaker speaker = getItem(position);
		TextView speakerNameView =
				(TextView) speakerView.findViewById(R.id.speakerName);
		speakerNameView.setText(speaker.getName());
		TextView speakerLanguagesView =
				(TextView) speakerView.findViewById(R.id.speakerLanguages);
		List<Language> languages = speaker.getLanguages();
		String languageNames = languages.remove(0).getName();
		for (Language language : languages) {
			languageNames = languageNames + ", " + language.getName();
		}
		speakerLanguagesView.setText(languageNames);
		ImageView speakerImage =
				(ImageView) speakerView.findViewById(R.id.speakerImage);
		try {
			speakerImage.setImageBitmap(speaker.getSmallImage());
		} catch (IOException e) {
			// If the image can't be loaded, we just leave it at that.
		}
		return speakerView;
	}

	private static final int listItemLayout = R.layout.speaker_list_item;
	private LayoutInflater inflater;

}
