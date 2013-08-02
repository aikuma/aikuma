package org.lp20.aikuma.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.ImageUtils;

public class RecordingArrayAdapter extends ArrayAdapter<Recording> {
	public RecordingArrayAdapter(Context context, List<Recording> recordings) {
		super(context, listItemLayout, recordings);
		this.context = context;
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View _, ViewGroup parent) {
		LinearLayout recordingView =
				(LinearLayout) inflater.inflate(listItemLayout, parent, false);
		Recording recording = getItem(position);
		TextView recordingNameView = 
				(TextView) recordingView.findViewById(R.id.recordingName);
		ImageView speakerImage;
		for (UUID uuid : recording.getSpeakersUUIDs()) {
			speakerImage = new ImageView(context);
			speakerImage.setAdjustViewBounds(true);
			speakerImage.setMaxHeight(60);
			speakerImage.setMaxWidth(60);
			speakerImage.setPaddingRelative(5,5,5,5);
			try {
				speakerImage.setImageBitmap(ImageUtils.getSmallImage(uuid));
			} catch (IOException e) {
				// Not much can be done if the image can't be loaded.
			}
			recordingView.addView(speakerImage);
		}
		recordingNameView.setText(recording.getName());
		return recordingView;
	}

	private static final int listItemLayout = R.layout.recording_list_item;
	private LayoutInflater inflater;
	private Context context;

}
