package au.edu.melbuni.boldapp.activities;

import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.models.Camera;
import au.edu.melbuni.boldapp.models.User;

public class InformedConsent3PhotoActivity extends BoldActivity {
	
	Camera camera;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
    }
    
    @Override
	public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);
        
     	setContent(R.layout.informed_consent_3_photo);
    };
    
    public void installBehavior(Bundle savedInstanceState) {
    	final User currentUser = Bundler.getCurrentUser(this);
    	
    	final View nextButton = (View) findViewById(R.id.nextButton);
    	
    	final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.userPictureSurfaceView);
    	camera = new Camera(surfaceView, new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] imageData, android.hardware.Camera camera) {
				if (imageData != null) {
					currentUser.putProfileImage(imageData);
					nextButton.setEnabled(true);
				}
			}
		});
    	
    	final View takePictureButton = (View) findViewById(R.id.takePictureButton);
    	takePictureButton.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			camera.takePicture();
    		}
    	});
    	
        nextButton.setEnabled(currentUser.hasProfileImage());
        nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentUser.hasProfileImage()) {
					setResult(RESULT_OK);
					finish();
				} // TODO Else show something.
			}
		});
        
    }
	
}
