package au.edu.melbuni.boldapp.activities;

import android.os.Bundle;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.Recorder;
import au.edu.melbuni.boldapp.Synchronizer;
import au.edu.melbuni.boldapp.behaviors.Behavior;
import au.edu.melbuni.boldapp.behaviors.TapAndReleaseRecord;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.persisters.JSONPersister;

public class RecordActivity extends BoldActivity {
	
	private Timeline timeline = null;
	
	Recorder recorder = new Recorder();
	Player   player   = new Player();
	
	static Behavior<RecordActivity> behavior = new TapAndReleaseRecord();
	
	public static void setBehavior(Behavior<RecordActivity> behavior) {
		RecordActivity.behavior = behavior;
	}
	
	public boolean hasTimeline() {
		return timeline != null;
	}

	public void setTimeline(Timeline timeline) {
		this.timeline = timeline;
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
    }
	
	@Override
	protected void onDestroy() {
		if (timeline != null) {
			timeline.save(new JSONPersister());
			
			// Try to synchronize automatically here.
			//
			try {
				Synchronizer.getDefault().push(timeline);
			} catch(RuntimeException e) {
				System.err.println(e.getMessage());
			}
		}
		
		super.onDestroy();
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
    }
}
