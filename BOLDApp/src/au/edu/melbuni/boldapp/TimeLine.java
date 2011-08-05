package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

/*
 * Controller for the current time line.
 * 
 * Note: Could also be called Segments.
 * 
 */
public class Timeline {
	
	int segmentCounter = 0;
	
	String identifier;
	HorizontalListView view;
	SegmentItemAdapter adapter;
	
	ArrayList<Segment> segments = new ArrayList<Segment>();
	Segment selectedForPlaying = null;
	Segment selectedForRecording = null;

	User user;
	
	public Timeline(Activity activity, String identifier) {
		this.identifier = identifier;
		this.view = (HorizontalListView) activity.findViewById(R.id.timeline);
		this.adapter = new SegmentItemAdapter(activity, segments);
		this.view.setAdapter(adapter);
		
		this.view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Segment segment = segments.get(position);
				segment.select();
				setSelectedForPlaying(segment);
				setSelectedForRecording(segment);
				adapter.notifyDataSetChanged();
			}
		});
		this.view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent,
							View view, final int position, long id) {
				new AlertDialog.Builder(getContext())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage("Delete?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								segments.get(position).remove();
							}
						}).setNegativeButton("No", null).show();

		return false;
			}
		});
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	// Delegator methods.
	//
	public Context getContext() {
		return view.getContext();
	}
	public void setSelectedForPlaying(Segment segment) {
		if (selectedForPlaying == segment) {
			this.selectedForPlaying = null;
		}
		this.selectedForPlaying = segment;
	}
	public void setSelectedForRecording(Segment segment) {
		if (selectedForRecording != null) {
			this.selectedForRecording.deselect();
		}
		if (selectedForRecording == segment) {
			this.selectedForRecording = null;
		} else {
			this.selectedForRecording = segment;
			this.selectedForRecording.select();
		}
	}
	public void add(Segment segment) {
		selectedForRecording = segment;
		segment.select();
		segments.add(segment);
		adapter.notifyDataSetChanged();
	}
	public void remove(Segment segment) {
		segments.remove(segment);
		if (selectedForRecording != null) { selectedForRecording.deselect(); }
		selectedForRecording = null;
		adapter.notifyDataSetChanged();
	}
	public void startPlaying(Player player) {
		System.out.println("SaP");
		
		Segment segment = getSelectedForPlaying();
		
		if (segment == null) {
			return;
		}
		
		segment.startPlaying(player);
	}
	public void stopPlaying(Player player) {
		System.out.println("SoP");
		
		Segment segment = getSelectedForPlaying();
		
		if (segment == null) {
			return;
		}
		
		segment.stopPlaying(player);
	}
	public void startRecording(Recorder recorder) {
		System.out.println("SaR");
		
		getSelectedForRecording().startRecording(recorder);
	}
	public void stopRecording(Recorder recorder) {
		System.out.println("SoR");
		
		recorder.stopRecording();
		
		// Last recorded will be the next playing.
		//
		this.selectedForPlaying = this.selectedForRecording;
		
		this.selectedForRecording.deselect();
		this.selectedForRecording = null;
	}
	
	// Returns the selected segment.
	//
	protected Segment getSelectedForPlaying() {
		if (selectedForPlaying == null) {
			if (segments.size() != 0) {
				return segments.get(segments.size()-1);
			}
		}
		
		return selectedForPlaying;
	}
	// Returns the selected segment
	// and if there is none, creates a new one.
	//
	protected Segment getSelectedForRecording() {
		if (selectedForRecording != null) {
			return selectedForRecording;
		}
		
		Segment segment = new Segment(this, segmentCounter++);
		add(segment);
		
		return segment;
	}
	
}
