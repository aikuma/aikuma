package au.edu.melbuni.boldapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;

public class EditUserActivity extends BoldActivity {
	
	static final int TAKE_USER_PICTURE = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
     	
        // Copy data from current user into view.
        //
        setUserPictureFromCurrentUser();
        setUserTextFromCurrentUser();
    }
    
    @Override
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
            @Override
			public void afterTextChanged(Editable s) {
            	Bundler.getCurrentUser(EditUserActivity.this).name = newUserNameEditText.getText().toString();
            }
            @Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
			public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        
	    final Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Bundler.saveNewUser(EditUserActivity.this, Bundler.getCurrentUser(EditUserActivity.this));
				finish();
			}
        });
        
	    final Button cancelButton = (Button) findViewById(R.id.cancelButton);
	    cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Bundler.setCurrentUser(EditUserActivity.this, null);
				finish();
			}
        });
    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	if (requestCode == TAKE_USER_PICTURE && resultCode == RESULT_OK) {
    		setUserPictureFromCurrentUser();
    	}
    }
    
    protected void setUserPictureFromCurrentUser() {
    	final ImageButton userPictureButton = (ImageButton) findViewById(R.id.userPictureImageButton);
    	userPictureButton.setImageDrawable(Bundler.getCurrentUser(this).getProfileImage());
    }
    protected void setUserTextFromCurrentUser() {
    	final EditText userNameEditText = (EditText) findViewById(R.id.newUserNameEditText);
    	userNameEditText.setText(Bundler.getCurrentUser(this).name);
    }
    
}
