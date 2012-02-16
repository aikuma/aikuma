package au.edu.melbuni.boldapp.activities;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Recorder;
import au.edu.melbuni.boldapp.models.User;

public class InformedConsentAudioActivity extends BoldActivity {
	
	boolean playing   = false;
	boolean recording = false;
	
	public boolean isPlaying() {
		return playing;
	}

	public void togglePlaying() {
		this.playing = !playing;
	}
	
	public boolean isRecording() {
		return recording;
	}

	public void toggleRecording() {
		this.recording = !recording;
	}
	
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
    	final View recordButton = findViewById(R.id.recordButton);
    	final View playButton = findViewById(R.id.playButton);
    	final View nextButton = findViewById(R.id.nextButton);
    	
        recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				User user = Bundler.getCurrentUser(InformedConsentAudioActivity.this);
				if (InformedConsentAudioActivity.this.isRecording()) {
					user.stopRecording(recorder);
					recordButton.getBackground().clearColorFilter();
				} else {
					user.startRecording(recorder);
					recordButton.getBackground().setColorFilter(Color.RED, Mode.MULTIPLY);
				}
				InformedConsentAudioActivity.this.toggleRecording();
			}
		});
    	
    	playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				User user = Bundler.getCurrentUser(InformedConsentAudioActivity.this);
				if (InformedConsentAudioActivity.this.isPlaying()) {
					user.stopPlaying(player);
					playButton.getBackground().clearColorFilter();
				} else {
					user.startPlaying(player, null);
					playButton.getBackground().setColorFilter(Color.GREEN, Mode.MULTIPLY);
				}
				InformedConsentAudioActivity.this.togglePlaying();
			}
		});
    	
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
