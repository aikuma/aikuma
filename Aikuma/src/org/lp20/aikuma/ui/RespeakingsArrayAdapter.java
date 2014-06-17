package org.lp20.aikuma.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.lp20.aikuma.R;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RespeakingsArrayAdapter extends BaseExpandableListAdapter {
	 
	private Context context;
	private LayoutInflater inflator;
    private List<Recording> respeakings; // header titles
    private SimpleDateFormat simpleDateFormat;

    private static final int LIST_ITEM_LAYOUT = R.layout.recording_list_item;
    private static final int LIST_ITEM_INTERFACE_LAYOUT = 
    		R.layout.recording_list_item_interface;
 
    public RespeakingsArrayAdapter(Context context, 
    		List<Recording> respeakings) {
    	this.context = context;
    	this.respeakings = respeakings;
        inflator = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 
    	convertView = inflator.
    			inflate(LIST_ITEM_INTERFACE_LAYOUT, null);
    	Recording recording = (Recording) getGroup(groupPosition);

    	List<String> speakers = recording.getSpeakersIds();
		StringBuilder sb = new StringBuilder("Speakers:\n");
		for(String speakerId : speakers) {
			try {
				sb.append(Speaker.read(speakerId).getName()+" ");
			} catch (IOException e) {
				// If the reader can't be read for whatever reason 
				// (perhaps JSON file wasn't formatted correctly),
				// Empty the speakersName
				e.printStackTrace();
			}
		}
		TextView speakersNameView = (TextView)
				convertView.findViewById(R.id.speakersName);
		speakersNameView.setText(sb);
        
        return convertView;
    }
 

    @Override
    public Object getGroup(int groupPosition) {
        return this.respeakings.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this.respeakings.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View recordingView, ViewGroup parent) {
    	
    	recordingView = inflator.inflate(LIST_ITEM_LAYOUT, parent, false);
    	Recording recording = (Recording) getGroup(groupPosition);
    	
    	TextView recordingNameView = 
				(TextView) recordingView.findViewById(R.id.recordingName);
		TextView recordingDateDurationView = 
				(TextView) recordingView.findViewById(R.id.recordingDateDuration);
		LinearLayout speakerImagesView = (LinearLayout)
				recordingView.findViewById(R.id.speakerImages);
		for (String id : recording.getSpeakersIds()) {
			speakerImagesView.addView(makeSpeakerImageView(id));
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

		// Add the number of views information
		TextView viewCountsView = (TextView)
				recordingView.findViewById(R.id.viewCounts);
		viewCountsView.setText(""+recording.numViews());

		// Add the number of comments information
		TextView numCommentsView = (TextView)
				recordingView.findViewById(R.id.numComments);
		List<Recording> respeakings = recording.getRespeakings();
		int numComments = respeakings.size();
		numCommentsView.setText(""+numComments);
		
		// Add the number of stars information
		TextView numStarsView = (TextView)
				recordingView.findViewById(R.id.numStars);
		numStarsView.setText(String.valueOf(recording.numStars()));

		// Add the number of flags information
		TextView numFlagsView = (TextView)
				recordingView.findViewById(R.id.numFlags);
		numFlagsView.setText(String.valueOf(recording.numFlags()));

		return recordingView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return null;
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    
    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }
    
    /**
	 * Creates the view for a given speaker.
	 *
	 * @param	speakerId	The ID of the speaker.
	 * @return	The image view for the speaker.
	 */
	private ImageView makeSpeakerImageView(String speakerId) {
		ImageView speakerImage = new ImageView(context);
		speakerImage.setAdjustViewBounds(true);
//		speakerImage.setScaleType(ImageView.ScaleType.FIT_END);
		speakerImage.setMaxHeight(60);//40
		speakerImage.setMaxWidth(60);//40
		try {
			speakerImage.setImageBitmap(Speaker.getSmallImage(speakerId));
		} catch (IOException e) {
			// Not much can be done if the image can't be loaded.
		}
		return speakerImage;
	}

}