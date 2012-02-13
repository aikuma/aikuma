package au.edu.melbuni.boldapp.behaviors;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Sounder;
import au.edu.melbuni.boldapp.activities.ListenActivity;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;

public class TapAndReleaseListen implements Behavior<ListenActivity> {
	
	private Player player = new Player();
	
	boolean playing = false;
	
	public boolean isPlaying() {
		return playing;
	}

	public void togglePlaying() {
		this.playing = !playing;
	}
	
	@Override
	public void configureView(ListenActivity activity) {
		activity.setContent(R.layout.tap_and_release_listen);
	}

	@Override
	public void installBehavior(final ListenActivity activity) {
	    final Timeline timeline = Bundler.getCurrentTimeline(activity);
	    
	    timeline.installOn(activity);
	    
	    // By default, select the first.
	    //
	    timeline.selectFirstSegment();
		
	    final ImageButton playButton = (ImageButton) activity.findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isPlaying()) {
					timeline.stopPlaying(player);
					
					playButton.getBackground().clearColorFilter();
				} else {
					timeline.startPlaying(player, new OnCompletionListener() {
						@Override
						public void onCompletion(Sounder sounder) {
							playButton.getBackground().clearColorFilter();
						}
					});
					playButton.getBackground().setColorFilter(Color.GREEN, Mode.MULTIPLY);
				}
				togglePlaying();
			}
		});
        
	    final Button likeButton = (Button) activity.findViewById(R.id.likeButton);
		final User user = Bundler.getCurrentUser(activity);
		final String userId = user.getIdentifier();
	    
		// TODO Dry.
		//
	    if (timeline.likedBy(userId)) {
	    	likeButton.getBackground().setColorFilter(Color.YELLOW, Mode.MULTIPLY);
	    } else {
	    	likeButton.getBackground().clearColorFilter();
	    }
	    
        likeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (timeline.likedBy(userId)) {
					timeline.unlike(userId);
					
					likeButton.getBackground().clearColorFilter();
				} else {
					timeline.like(userId);
					
					likeButton.getBackground().setColorFilter(Color.YELLOW, Mode.MULTIPLY);
				}
			}
		});
	}
	
}
