package au.edu.melbuni.boldapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class RecordFragment extends Fragment {
	
	private Recorder recorder = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View v = inflater.inflate(R.layout.record, container, false);
	    
	    // Set buttons etc.
	    //
	    final ImageButton helpButton = (ImageButton) getActivity().findViewById(R.id.helpButton);
	    helpButton.setImageResource(R.drawable.help_record);
	    
	    recorder = new Recorder("audiorecordtest.3gp");
	    
	    final ImageButton playButton = (ImageButton) v.findViewById(R.id.playButton);
	    final ImageButton recordButton = (ImageButton) v.findViewById(R.id.recordButton);
	    
        // Set button actions.
        //
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent motionEvent) {
        		// Start playing.
            	recorder.startPlaying();
            	System.out.println("START PLAY");
            	return false;
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Stop playing.
				recorder.stopPlaying();
				System.out.println("STOP PLAY");
			}
		});
        recordButton.setOnTouchListener(new View.OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent motionEvent) {
        		// Start recording.
            	recorder.startRecording();
            	System.out.println("START RECORD");
            	return false;
            }
        });
        recordButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Stop recording.
				recorder.stopRecording();
				System.out.println("STOP RECORD");
			}
		});
	    
	    return v;
	}
}
