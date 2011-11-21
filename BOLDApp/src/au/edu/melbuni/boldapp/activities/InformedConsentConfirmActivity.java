package au.edu.melbuni.boldapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.models.User;

public class InformedConsentConfirmActivity extends BoldActivity {
	
	static final int TAKE_USER_AUDIO   = 0;
	static final int TAKE_USER_NAME    = 1;
	static final int TAKE_USER_PICTURE = 2;
	
	Player player = new Player();

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
     	
     	if (Bundler.getCurrentUser(this).hasGivenConsent()) {
     		setUserPictureFromCurrentUser();
     	} else {
     		startActivityForResult(new Intent(getApplicationContext(), InformedConsentAudioActivity.class), TAKE_USER_AUDIO);
     	}
    }
    
    @Override
	public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);
        
     	setContent(R.layout.informed_consent_confirm);
    };
    
    public void installBehavior(Bundle savedInstanceState) {
    	
    	setUserPictureFromCurrentUser();
    	
        final View playButton = (View) findViewById(R.id.playButton);
        playButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
        		User user = Bundler.getCurrentUser(InformedConsentConfirmActivity.this);
        		user.startPlaying(player, null);
            	return false;
            }
        });
    	playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				User user = Bundler.getCurrentUser(InformedConsentConfirmActivity.this);
				user.stopPlaying(player);
			}
		});
    	
        final View nextButton = (View) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundler.saveNewUser(InformedConsentConfirmActivity.this, Bundler.getCurrentUser(InformedConsentConfirmActivity.this));
				finish();
			}
		});
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	if (resultCode == RESULT_OK) {
    		if (requestCode == TAKE_USER_AUDIO) { 
    			// TODO Save.
    			startActivityForResult(new Intent(getApplicationContext(), InformedConsentNameActivity.class), TAKE_USER_NAME);
    		} else if (requestCode == TAKE_USER_NAME) {
    			// TODO Save?
    			startActivityForResult(new Intent(getApplicationContext(), InformedConsentPhotoActivity.class), TAKE_USER_PICTURE);
    		} else if (requestCode == TAKE_USER_PICTURE) {
    			setUserPictureFromCurrentUser();
    		}
    	} else {
    		finish();
    	}
    }
    
    protected void setUserPictureFromCurrentUser() {
    	final ImageView userPictureView = (ImageView) findViewById(R.id.userPicture);
    	userPictureView.setImageDrawable(Bundler.getCurrentUser(this).getProfileImage());
    }
//    protected void setUserTextFromCurrentUser() {
//    	final EditText userNameEditText = (EditText) findViewById(R.id.newUserNameEditText);
//    	userNameEditText.setText(Bundler.getCurrentUser(this).name);
//    }
	
}
