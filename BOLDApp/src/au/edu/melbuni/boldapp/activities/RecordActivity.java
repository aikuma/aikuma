package au.edu.melbuni.boldapp.activities;

import android.os.Bundle;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.Recorder;
import au.edu.melbuni.boldapp.Synchronizer;
import au.edu.melbuni.boldapp.behaviors.Behavior;
import au.edu.melbuni.boldapp.behaviors.TapAndReleaseRecord;
import au.edu.melbuni.boldapp.models.Timeline;

public class RecordActivity extends BoldActivity {
	
	protected Timeline timeline = null;
	
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
			// Stop and save the recording.
			//
			// TODO Duplicate code (see TapAndReleaseRecord).
			//
//			timeline.stopRecording(recorder);
//			timeline.setUser(Bundler.getCurrentUser(this));
//			Bundler.addTimeline(this, timeline);
//			new JSONPersister().save(timeline);
			
			// Try to synchronize automatically here.
			//
			// Note: Removed since it didn't work very well.
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
        setFinishNavigation();
    };
    
    public void installBehavior(Bundle savedInstanceState) {
    	behavior.installBehavior(this);
    }
}
