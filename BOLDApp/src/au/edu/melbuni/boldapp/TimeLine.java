package au.edu.melbuni.boldapp;

import java.util.Date;

import android.app.Activity;

/*
 * Controller for the current time line.
 * 
 * Note: Could also be called Segments.
 * 
 */
public class Timeline {
	
	String identifier;
	User user;
	Date date;
	String location;
	
	Segments segments;

	public Timeline(Activity activity, String identifier) {
		this.date     = new Date();
		this.location = "Some Location";
		this.identifier = identifier;
		
		this.segments = Persister.loadSegments(this);
	}
	
	public void installOn(Activity activity) {
		segments.installOn(activity, R.id.timeline);
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	// Delegator methods.
	//
	public void startPlaying(Player player) {
		segments.startPlaying(player);
	}

	public void stopPlaying(Player player) {
		segments.stopPlaying(player);
	}

	public void startRecording(Recorder recorder) {
		segments.startRecording(recorder);
	}

	public void stopRecording(Recorder recorder) {
		segments.stopRecording(recorder);
	}

	public CharSequence getItemText() {
		return date.toLocaleString() + " " + location;
	}

}
