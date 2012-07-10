package au.edu.melbuni.boldapp.activities;

import android.os.Bundle;
import android.widget.ImageView;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.behaviors.Behavior;
import au.edu.melbuni.boldapp.behaviors.TapAndReleaseListen;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;

public class ListenActivity extends BoldActivity {
	
	User listeningToUser = null;
	static Behavior<ListenActivity> behavior = new TapAndReleaseListen();
	
	public static void setBehavior(Behavior<ListenActivity> tapAndHoldListen) {
		ListenActivity.behavior = tapAndHoldListen;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
     	configureView(savedInstanceState);
	    installBehavior(savedInstanceState);
    }
    
    @Override
	public void configureView(Bundle savedInstanceState) {
    	super.configureView(savedInstanceState);
    	
    	behavior.configureView(this);
    };
	
    public void installBehavior(Bundle savedInstanceState) {
    	behavior.installBehavior(this);
    }
    
    // TODO This should maybe be in the behaviour.
    //
	@Override
	protected void onResume() {
		super.onResume();

		Timeline timeline = Bundler.getCurrentTimeline(this);
		User listeningToUser = timeline.getUser();

		// Set current user.
		//
		ImageView playUserImage = (ImageView) findViewById(R.id.playUserImage);
		if (playUserImage != null) {
			if (listeningToUser != null && listeningToUser.hasProfileImage()) {
				playUserImage.setImageDrawable(listeningToUser.getProfileImage());
			} else {
				playUserImage.setImageResource(R.drawable.unknown_user);
			}
		}
	}
    
}
