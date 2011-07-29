package au.edu.melbuni.boldapp;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class RecordActivity extends BoldActivity {
	
	Recorder recorder = new Recorder();

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView();
     	installBehavior();
    }
    
    public void configureView() {
        super.configureView();
        
     	setContent(R.layout.record);
    };
    
    public void installBehavior() {
     	final TimeLine timeLine = new TimeLine(this, "audiorecordtest");
	    
	    final ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
	    final ImageButton recordButton = (ImageButton) findViewById(R.id.recordButton);
	    
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent motionEvent) {
            	timeLine.startPlaying(recorder);
            	return false;
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				timeLine.stopPlaying(recorder);
			}
		});
        recordButton.setOnTouchListener(new View.OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent motionEvent) {
        		timeLine.startRecording(recorder);
            	return false;
            }
        });
        recordButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				timeLine.stopRecording(recorder);
			}
		});
    };
}
