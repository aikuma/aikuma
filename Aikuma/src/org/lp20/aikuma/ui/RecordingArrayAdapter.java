/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.ImageUtils;

/**
 * Takes a list of recordings and provides views as appropriate.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordingArrayAdapter extends ArrayAdapter<Recording> {

	/**
	 * Constructor.
	 *
	 * @param	context	the current context
	 * @param	recordings	The list of recordings.
	 */
	public RecordingArrayAdapter(Context context, List<Recording> recordings) {
		super(context, LIST_ITEM_LAYOUT, recordings);
		this.context = context;
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	}

	@Override
	public View getView(int position, View _, ViewGroup parent) {
		LinearLayout recordingView =
				(LinearLayout) inflater.inflate(LIST_ITEM_LAYOUT, parent, false);
		Recording recording = getItem(position);

		ImageView recordingTypeView = (ImageView) recordingView.findViewById(
				R.id.recordingType);
		if (!recording.isOriginal()) {
			// Set it to be a two way arrow icon to indicate respeaking.
			recordingTypeView.setImageResource(R.drawable.exchange);
		}

		TextView recordingNameView = 
				(TextView) recordingView.findViewById(R.id.recordingName);
		TextView recordingDateDurationView = 
				(TextView) recordingView.findViewById(R.id.recordingDateDuration);
		for (String id : recording.getSpeakersIds()) {
			recordingView.addView(makeSpeakerImageView(id));
		}
		recordingNameView.setText(recording.getNameAndLang());
		Integer duration = recording.getDurationMsec() / 1000;
		if (recording.getDurationMsec() == -1) {
			recordingDateDurationView.setText(
					simpleDateFormat.format(recording.getDate()));
		} else {
			recordingDateDurationView.setText(
				simpleDateFormat.format(recording.getDate()) + " (" +
				duration.toString() + "s)");
		}
		return recordingView;
	}

	/**
	 * Creates the view for a given speaker.
	 *
	 * @param	speakerId	The UUID of the speaker.
	 * @return	The image view for the speaker.
	 */
	private ImageView makeSpeakerImageView(String speakerId) {
		ImageView speakerImage = new ImageView(context);
		speakerImage.setAdjustViewBounds(true);
		speakerImage.setMaxHeight(40);
		speakerImage.setMaxWidth(40);
		speakerImage.setPaddingRelative(1,1,1,1);
		try {
			speakerImage.setImageBitmap(Speaker.getSmallImage(speakerId));
		} catch (IOException e) {
			// Not much can be done if the image can't be loaded.
		}
		return speakerImage;
	}

	private static final int LIST_ITEM_LAYOUT = R.layout.recording_list_item;
	private LayoutInflater inflater;
	private Context context;
	private SimpleDateFormat simpleDateFormat;

}
