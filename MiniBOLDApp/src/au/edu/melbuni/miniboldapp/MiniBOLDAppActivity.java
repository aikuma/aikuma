package au.edu.melbuni.miniboldapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import au.edu.melbuni.R;
import au.edu.melbuni.miniboldapp.Recorder;

public class MiniBOLDAppActivity extends Activity {
	
	private Recorder recorder = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
	    // Set buttons etc.
	    //
	    
	    recorder = new Recorder("audiorecordtest.3gp");
	    
	    final ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
	    final ImageButton recordButton = (ImageButton) findViewById(R.id.recordButton);
	    
        // Set button actions.
        //
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent motionEvent) {
        		// Start playing.
            	recorder.startPlaying();
            	return false;
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Stop playing.
				recorder.stopPlaying();
			}
		});
        recordButton.setOnTouchListener(new View.OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent motionEvent) {
        		// Start recording.
            	recorder.startRecording();
            	return false;
            }
        });
        recordButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Stop recording.
				recorder.stopRecording();
			}
		});
    }
}