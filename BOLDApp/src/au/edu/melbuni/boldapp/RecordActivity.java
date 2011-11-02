package au.edu.melbuni.boldapp;

import android.os.Bundle;
import au.edu.melbuni.boldapp.behaviors.Behavior;
import au.edu.melbuni.boldapp.behaviors.TapAndReleaseRecord;

public class RecordActivity extends BoldActivity {
	
	Recorder recorder = new Recorder();
	Player   player   = new Player();
	
	static Behavior<RecordActivity> behavior = new TapAndReleaseRecord();
	
	public static void setBehavior(Behavior<RecordActivity> behavior) {
		RecordActivity.behavior = behavior;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
    }
	
	public Player getPlayer() {
		return player;
	}
	public Recorder getRecorder() {
		return recorder;
	}
    
    @Override
	public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);
        behavior.configureView(this);
    };
    
    public void installBehavior(Bundle savedInstanceState) {
    	behavior.installBehavior(this);
    };
}
