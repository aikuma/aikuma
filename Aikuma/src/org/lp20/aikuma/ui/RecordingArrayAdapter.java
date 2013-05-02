package org.lp20.aikuma;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import org.lp20.aikuma.model.Recording;

public class RecordingArrayAdapter extends ArrayAdapter<Recording> {
	RecordingArrayAdapter(Context context, List<Recording> recordings) {
		super(context, listItemLayout, recordings);
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View _, ViewGroup parent) {
		View recordingView =
				(View) inflater.inflate(listItemLayout, parent, false);
		Recording recording = getItem(position);
		TextView recordingNameView = 
				(TextView) recordingView.findViewById(R.id.recordingName);
		recordingNameView.setText(recording.getName());
		return recordingView;
	}

	private static final int listItemLayout = R.layout.recording_list_item;
	private LayoutInflater inflater;

}
