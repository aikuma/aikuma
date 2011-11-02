package au.edu.melbuni.boldapp.behaviors;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.activities.RecordActivity;
import au.edu.melbuni.boldapp.models.Timeline;

public class TapAndReleaseRecord implements Behavior<RecordActivity> {
	
	boolean recording = false;
	
	public boolean isRecording() {
		return recording;
	}

	public void toggleRecording() {
		this.recording = !recording;
	}

	@Override
	public void configureView(RecordActivity activity) {
		activity.setContent(R.layout.tap_and_release_record);
	}
	
	@Override
	public void installBehavior(final RecordActivity activity) {
     	final Timeline timeline = new Timeline("recording_");
	    final ImageButton playButton = (ImageButton) activity.findViewById(R.id.playButton);
	    final ImageButton deleteButton = (ImageButton) activity.findViewById(R.id.deleteButton);
	    final ImageButton recordButton = (ImageButton) activity.findViewById(R.id.recordButton);
	    
//	    timeline.installOn(activity);
	    
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
            	timeline.startPlayingLastByDefault(activity.getPlayer());
            	return false;
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeline.stopPlaying(activity.getPlayer());
			}
		});
        
        deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (timeline.hasSegment()) {
					new AlertDialog.Builder(v.getContext())
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage("Delete last recording?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							timeline.removeLast();
						}
					}).setNegativeButton("No", null).show();
				}
			}
		});
        
        recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TapAndReleaseRecord.this.isRecording()) {
					timeline.stopRecording(activity.getRecorder());
					timeline.setUser(Bundler.getCurrentUser(activity));
					Bundler.addTimeline(activity, timeline);
					
					recordButton.setBackgroundColor(Color.LTGRAY);
				} else {
					timeline.startRecording(activity.getRecorder());
					
					recordButton.setBackgroundColor(Color.RED);
				}
				TapAndReleaseRecord.this.toggleRecording();
			}
		});
	}

}
