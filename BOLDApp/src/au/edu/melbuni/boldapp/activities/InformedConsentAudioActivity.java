package au.edu.melbuni.boldapp.activities;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Recorder;
import au.edu.melbuni.boldapp.models.User;

public class InformedConsentAudioActivity extends BoldActivity {
	
//	AlertDialog helpDialog;
	
	Recorder recorder = new Recorder();
	Player player = new Player();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
    }
    
    @Override
	public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);
        
     	setContent(R.layout.informed_consent_audio);
    };
    
    public void installBehavior(Bundle savedInstanceState) {
        installHelp(R.layout.informed_consent_audio_help);
    	
    	final View recordButton = findViewById(R.id.recordButton);
        recordButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
        		User user = Bundler.getCurrentUser(InformedConsentAudioActivity.this);
        		user.startRecording(recorder);
            	return false;
            }
        });
        recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
        		User user = Bundler.getCurrentUser(InformedConsentAudioActivity.this);
        		user.stopRecording(recorder);
			}
		});
    	
        final View playButton = findViewById(R.id.playButton);
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
        		User user = Bundler.getCurrentUser(InformedConsentAudioActivity.this);
        		user.startPlaying(player, null);
            	return false;
            }
        });
    	playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				User user = Bundler.getCurrentUser(InformedConsentAudioActivity.this);
				user.stopPlaying(player);
			}
		});
    	
        final View nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Save something.
				setResult(RESULT_OK);
				finish();
			}
		});
        
    }
	
}
