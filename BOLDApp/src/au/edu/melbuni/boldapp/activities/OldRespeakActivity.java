package au.edu.melbuni.boldapp.activities;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.Demo;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.models.Timeline;

public class OldRespeakActivity extends BoldActivity {

	boolean playing = false;
	boolean saved = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		configureView(savedInstanceState);
		installBehavior(savedInstanceState);
	}

	@Override
	public void configureView(Bundle savedInstanceState) {
		super.configureView(savedInstanceState);

		setContent(R.layout.old_respeak);
	};

	public void installBehavior(Bundle savedInstanceState) {
		final Player player = Bundler.getPlayer();
		player.startPlaying(Demo.getSoundfilePathWithoutExtension(), null);
		player.pause();
		
		// For demo purposes.
		//
     	final Timeline recordingTimeline = new Timeline("11111111-1111-1111-1111-111111111111");
     	recordingTimeline.setUser(Bundler.getCurrentUser(OldRespeakActivity.this));
     	
	    final ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
	    final ImageButton recordButton = (ImageButton) findViewById(R.id.recordButton);
	    
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
//            	timeline.startPlayingLastByDefault(Bundler.getPlayer());
//        		player.startPlaying(Demo.getSoundfilePathWithoutExtension(), null);
        		
        		if (!playing) { player.resume(); playing = true; }
        		
            	return false;
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				timeline.stopPlaying(Bundler.getPlayer());
				
				if (playing) { player.pause(); playing = false; }
			}
		});
        recordButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
//        		recordingTimeline.startRecording(Bundler.getRecorder());
        		
            	return false;
            }
        });
        recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				recordingTimeline.stopRecording(Bundler.getRecorder());
//				recordingTimeline.save(new JSONPersister());
//				Bundler.addTimeline(OldRespeakActivity.this, recordingTimeline);
				
//				recordingTimeline.startPlayingLast(Bundler.getPlayer());
			}
		});
	}
}