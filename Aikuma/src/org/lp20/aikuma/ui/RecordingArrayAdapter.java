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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma2.R;
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
		this.recordings = new ArrayList<Recording>(recordings);
		inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// Sort so that the recordings with the most stars are displayed first.
		this.sort(new Comparator<Recording>() {
			@Override
			public int compare(Recording lhs, Recording rhs) {
				if (lhs.numStars() - 2*lhs.numFlags() < rhs.numStars() -
						2*rhs.numFlags()) {
					return +1;
				} else if (lhs.numStars() - 2*lhs.numFlags() > rhs.numStars() -
						2*rhs.numFlags()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
	}

	/**
	 * Constructor when Quick-menu is attached to each list_item
	 * 
	 * @param	context	the current context
	 * @param	recordings	The list of recordings.
	 * @param	quickMenu	QuickActionMenu for each liste item
	 */
	public RecordingArrayAdapter(Context context, List<Recording> recordings, 
			QuickActionMenu<Recording> quickMenu) {
		this(context, recordings);
		this.quickMenu = quickMenu;
	}
	
	@Override
	public View getView(int position, View _, ViewGroup parent) {
		LinearLayout recordingView =
				(LinearLayout) inflater.inflate(LIST_ITEM_LAYOUT, parent, false);
		final Recording recording = getItem(position);

		// Set the view to have recording name, date, duration, speakerImage
		TextView recordingNameView = 
				(TextView) recordingView.findViewById(R.id.recordingName);
		TextView recordingDateDurationView = 
				(TextView) recordingView.findViewById(R.id.recordingDateDuration);
		LinearLayout speakerImagesView = (LinearLayout)
				recordingView.findViewById(R.id.speakerImages);

		String verName = recording.getVersionName();
		String ownerId = recording.getOwnerId();
		for (String id : recording.getSpeakersIds()) {
			speakerImagesView.addView(makeSpeakerImageView(verName, ownerId, id));
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
		
		// Set the speakers' names
		List<String> speakers = recording.getSpeakersIds();
		StringBuilder sb = new StringBuilder();
		for(String speakerId : speakers) {
			try {
				sb.append(Speaker.read(verName, ownerId, speakerId).getName()+", ");
			} catch (IOException e) {
				// If the reader can't be read for whatever reason 
				// (perhaps JSON file wasn't formatted correctly),
				// Empty the speakersName
				e.printStackTrace();
			}
		}
		
		TextView speakerNameView = (TextView)
				recordingView.findViewById(R.id.speakerNames);
		if(sb.length() > 2)
			speakerNameView.setText(sb.substring(0, sb.length()-2));

		// Add the comment(two way arrow icon) or movie icon
		LinearLayout icons = (LinearLayout)
				recordingView.findViewById(R.id.recordingIcons);
		
		List<Recording> respeakings = recording.getRespeakings();
		int numComments = respeakings.size();
		if(numComments > 0) {
			icons.addView(makeRecordingInfoIcon(R.drawable.commentary_32));
		}
		if(recording.isMovie()) {
			icons.addView(makeRecordingInfoIcon(R.drawable.movie_32));
		}
		
		
		// Add the number of views information
		TextView viewCountsView = (TextView)
				recordingView.findViewById(R.id.viewCounts);
		viewCountsView.setText(""+recording.numViews());
		
		// Add the number of stars information
		TextView numStarsView = (TextView)
				recordingView.findViewById(R.id.numStars);
		numStarsView.setText(String.valueOf(recording.numStars()));

		// Add the number of flags information
		TextView numFlagsView = (TextView)
				recordingView.findViewById(R.id.numFlags);
		numFlagsView.setText(String.valueOf(recording.numFlags()));
		
		// When list is used to show only one item, quickMenu is not null
		// and only long-click is enabled for the item
		if(quickMenu != null) {
			recordingView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					quickMenu.show(v, recording);
					return false;
				}
				
			});
		}
		

		return recordingView;
	}

	/**
	 * Create the view for a icon with resourceId
	 * 
	 * @param resourceId	The id of a drawalbe image
	 * @return
	 */
	private ImageView makeRecordingInfoIcon(int resourceId) {
		ImageView iconImage = new ImageView(context);
		iconImage.setImageResource(resourceId);
		iconImage.setAdjustViewBounds(true);
		iconImage.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		
		return iconImage;
	}
	
	/**
	 * Creates the view for a given speaker.
	 *
	 * @param	verName		The version of the recording
	 * @param	ownerId		The owner ID of the speaker
	 * @param	speakerId	The ID of the speaker.
	 * @return	The image view for the speaker.
	 */
	private ImageView makeSpeakerImageView(String verName, String ownerId, 
			String speakerId) {
		int pixels = ImageUtils.getPixelsFromDp(context, 40);
		ImageView speakerImage = new ImageView(context);
		speakerImage.setAdjustViewBounds(true);
//		speakerImage.setScaleType(ImageView.ScaleType.FIT_END);
		speakerImage.setMaxHeight(pixels);//40
		speakerImage.setMaxWidth(pixels);//40
		try {
			speakerImage.setImageBitmap(Speaker.getSmallImage(verName, ownerId, speakerId));
		} catch (IOException e) {
			// Not much can be done if the image can't be loaded.
		}
		return speakerImage;
	}
	
	@Override
	public Filter getFilter() {
		return new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				// TODO Auto-generated method stub
				constraint = constraint.toString().toLowerCase();
				Filter.FilterResults results = new Filter.FilterResults();
				
				if(constraint == null || constraint.length() == 0) {
					results.values = recordings;
				} else {
					List<Recording> filteredRecordings = 
							new ArrayList<Recording>();
					for(int i=0; i<recordings.size(); i++) {
						Recording item = recordings.get(i);
						List<Language> languages = item.getLanguages();
						for(Language lang : languages) {
							if(lang.getCode().contains(constraint)) {
								filteredRecordings.add(item);
								break;
							}
						}
					}
					results.values = filteredRecordings;
				}
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				// TODO Auto-generated method stub
				List<Recording> filteredRecordings = 
						(List<Recording>) results.values;
				RecordingArrayAdapter.this.clear();
				RecordingArrayAdapter.this.addAll(filteredRecordings);
				RecordingArrayAdapter.this.notifyDataSetChanged();
			}
			
		};
	}

	private static final int LIST_ITEM_LAYOUT = R.layout.recording_list_item;
	private LayoutInflater inflater;
	private Context context;
	private List<Recording> recordings;
	private SimpleDateFormat simpleDateFormat;
	private QuickActionMenu<Recording> quickMenu = null;

}
