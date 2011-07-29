package au.edu.melbuni.boldapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class ListenActivity extends Activity {

	private Recorder recorder = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
     	configureView();
	    installBehavior();
    }
    
    public void configureView() {
        setContentView(R.layout.base);
        
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout menu = (LinearLayout) findViewById(R.id.menu);
        
        // Menu.
        //
     	menu.addView(layoutInflater.inflate(R.layout.navigation, menu, false), 0);
     	menu.addView(layoutInflater.inflate(R.layout.user, menu, false), 1);
     	menu.addView(layoutInflater.inflate(R.layout.help, menu, false), 2);
     	
     	// Content.
     	//
     	FrameLayout content = (FrameLayout) findViewById(R.id.content);
     	content.addView(layoutInflater.inflate(R.layout.listen, content, false));
    };
	
    public void installBehavior() {
	    final ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
    	
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent motionEvent) {
            	recorder.startPlaying("audiorecordtest.3gp");
            	return false;
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				recorder.stopPlaying();
			}
		});
    };
    
}
