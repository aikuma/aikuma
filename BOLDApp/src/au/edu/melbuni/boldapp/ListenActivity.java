package au.edu.melbuni.boldapp;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class ListenActivity extends BoldActivity {

	private Player player = new Player();
	private Timeline timeline;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
     	configureView(savedInstanceState);
	    installBehavior(savedInstanceState);
	    
	    timeline = Bundler.getCurrentTimeline(this);
	    timeline.installOn(this);
    }
    
    public void configureView(Bundle savedInstanceState) {
    	super.configureView(savedInstanceState);
    	
        setContentView(R.layout.listen);
    };
	
    public void installBehavior(Bundle savedInstanceState) {
	    final ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
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
    };
    
}
