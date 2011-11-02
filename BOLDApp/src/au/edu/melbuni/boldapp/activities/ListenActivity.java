package au.edu.melbuni.boldapp.activities;

import android.os.Bundle;
import au.edu.melbuni.boldapp.behaviors.Behavior;
import au.edu.melbuni.boldapp.behaviors.TapAndReleaseListen;

public class ListenActivity extends BoldActivity {

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
    
    public void configureView(Bundle savedInstanceState) {
    	super.configureView(savedInstanceState);
    	
    	behavior.configureView(this);
    };
	
    public void installBehavior(Bundle savedInstanceState) {
    	behavior.installBehavior(this);
    }
    
}
