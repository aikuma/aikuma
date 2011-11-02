package au.edu.melbuni.boldapp.behaviors;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.ListenActivity;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.models.Timeline;

public class TapAndHoldListen implements Behavior<ListenActivity> {
	
	private Player player = new Player();
	
	@Override
	public void configureView(ListenActivity activity) {
		activity.setContentView(R.layout.tap_and_hold_listen);
	}

	@Override
	public void installBehavior(ListenActivity activity) {
	    final Timeline timeline = Bundler.getCurrentTimeline(activity);
	    timeline.installOn(activity);
		
	    final ImageButton playButton = (ImageButton) activity.findViewById(R.id.playButton);
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
            	timeline.startPlaying(player, null);
            	return false;
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeline.stopPlaying(player);
			}
		});
	}
	
}
