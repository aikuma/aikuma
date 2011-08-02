package au.edu.melbuni.boldapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class NewUserActivity extends BoldActivity {
	
	static final int TAKE_USER_PICTURE = 0;
	User newUser;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        newUser = new User();
     	
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
				startActivityForResult(new Intent(view.getContext(), CameraActivity.class), TAKE_USER_PICTURE);
			}
        });
        
        final EditText newUserNameEditText = (EditText) findViewById(R.id.newUserNameEditText);
        newUserNameEditText.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
            	newUser.name = newUserNameEditText.getText().toString();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == TAKE_USER_PICTURE && resultCode == RESULT_OK) {
    		// Try to show the picture on the button.
    		//
    		final ImageButton userPictureButton = (ImageButton) findViewById(R.id.userPictureImageButton);
        	userPictureButton.setImageDrawable(newUser.getProfileImage());
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	Bundler.saveNewUser(outState, newUser);
    	
    	super.onSaveInstanceState(outState);
    }
    
}
