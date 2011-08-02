package au.edu.melbuni.boldapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
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
    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == TAKE_USER_PICTURE && resultCode == RESULT_OK) {
    		// Try to show the picture on the button.
    		//
    		final ImageButton userPictureButton = (ImageButton) findViewById(R.id.userPictureImageButton);
        	
    		String fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        	fileName += "/";
        	fileName += newUser.getImagePath();
        	
        	userPictureButton.setImageDrawable(Drawable.createFromPath(fileName));
    	}
    }
    
}
