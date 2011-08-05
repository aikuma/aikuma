package au.edu.melbuni.boldapp;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class RecordActivity extends BoldActivity {
	
	Recorder recorder = new Recorder();
	Player   player   = new Player();

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
    }
    
    @Override
	public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);
        
     	setContent(R.layout.record);
    };
    
    public void installBehavior(Bundle savedInstanceState) {
     	final Timeline timeline = new Timeline(this, "recording_");
	    
	    final ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
	    final ImageButton recordButton = (ImageButton) findViewById(R.id.recordButton);
	    
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
            	timeline.startPlaying(player);
            	return false;
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeline.stopPlaying(player);
			}
		});
        recordButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
        		timeline.startRecording(recorder);
            	return false;
            }
        });
        recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeline.stopRecording(recorder);
				timeline.setUser(Bundler.getCurrentUser(RecordActivity.this));
				Bundler.addTimeline(RecordActivity.this, timeline);
			}
		});
    };
}
