package au.edu.melbuni.boldapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class NewUserActivity extends BoldActivity {
	
	static final int TAKE_USER_PICTURE = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Bundler.setCurrentUser(this, new User());
		
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
    }
    
    public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);
        
     	setContent(R.layout.new_user);
    };
    
    public void installBehavior(Bundle savedInstanceState) {
	    final ImageButton userPictureButton = (ImageButton) findViewById(R.id.userPictureImageButton);
        userPictureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivityForResult(new Intent(getApplicationContext(), CameraActivity.class), TAKE_USER_PICTURE);
			}
        });
        
        final EditText newUserNameEditText = (EditText) findViewById(R.id.newUserNameEditText);
        newUserNameEditText.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
            	currentUser.name = newUserNameEditText.getText().toString();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        
	    final Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Bundler.saveNewUser(NewUserActivity.this, currentUser);
				finish();
			}
        });
        
	    final Button cancelButton = (Button) findViewById(R.id.cancelButton);
	    cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Bundler.setCurrentUser(NewUserActivity.this, null);
				finish();
			}
        });
    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	if (requestCode == TAKE_USER_PICTURE && resultCode == RESULT_OK) {
    		currentUser = Bundler.getCurrentUser(this);
    		
    		// Try to show the picture on the button.
    		//
    		final ImageButton userPictureButton = (ImageButton) findViewById(R.id.userPictureImageButton);
        	userPictureButton.setImageDrawable(currentUser.getProfileImage());
    	}
    }
    
}
