package au.edu.melbuni.boldapp.behaviors;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.activities.RecordActivity;
import au.edu.melbuni.boldapp.models.Timeline;

public class TapAndHoldRecord implements Behavior<RecordActivity> {
	
	@Override
	public void configureView(RecordActivity activity) {
     	activity.setContent(R.layout.tap_and_hold_record);
    }

	@Override
	public void installBehavior(final RecordActivity activity) {
     	final Timeline timeline = new Timeline();
	    final ImageButton playButton = (ImageButton) activity.findViewById(R.id.playButton);
	    final ImageButton recordButton = (ImageButton) activity.findViewById(R.id.recordButton);

	    timeline.installOn(activity);
	    
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
            	timeline.startPlayingLastByDefault(Bundler.getPlayer());
            	return false;
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeline.stopPlaying(Bundler.getPlayer());
			}
		});
        recordButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
        		timeline.startRecording(Bundler.getRecorder());
            	return false;
            }
        });
        recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeline.stopRecording(Bundler.getRecorder());
				timeline.setUser(Bundler.getCurrentUser(activity));
				Bundler.addTimeline(activity, timeline);
			}
		});
	};
	
}
