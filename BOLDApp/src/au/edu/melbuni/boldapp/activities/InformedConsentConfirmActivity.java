package au.edu.melbuni.boldapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;

public class InformedConsentConfirmActivity extends BoldActivity {
	
	static final int SHOW_USER_INFO    = 0;
	static final int TAKE_USER_AUDIO   = 1;
	static final int TAKE_USER_PICTURE = 2;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
     	
     	if (Bundler.getCurrentUser(this).hasGivenConsent()) {
     		setUserPictureFromCurrentUser();
     	} else {
     		startActivityForResult(new Intent(getApplicationContext(), InformedConsent1InfoActivity.class), SHOW_USER_INFO);
     	}
    }
    
    @Override
	public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);
        
     	setContent(R.layout.informed_consent_confirm);
    };
    
    public void installBehavior(Bundle savedInstanceState) {
    	
        final ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
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
    		if (requestCode == SHOW_USER_INFO) { 
    			startActivityForResult(new Intent(getApplicationContext(), InformedConsent2AudioActivity.class), TAKE_USER_AUDIO);
    		} else if (requestCode == TAKE_USER_AUDIO) { 
    			// TODO Save.
    			startActivityForResult(new Intent(getApplicationContext(), InformedConsent3PhotoActivity.class), TAKE_USER_PICTURE);
    		} else if (requestCode == TAKE_USER_PICTURE) {
    			// TODO Save.
    			// Do nothing.
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
