package au.edu.melbuni.boldapp;

import java.util.ArrayList;

import android.app.Application;

public class BoldApplication extends Application {

	private User currentUser;
	private Timeline currentTimeline;
	private ArrayList<User> users;
	private ArrayList<Timeline> timelines;
	
	public BoldApplication() {
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
	}

	public User getCurrentUser() {
		if (currentUser == null) {
			currentUser = new User();
		}
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public ArrayList<User> getUsers() {
		if (users == null) {
			users = new ArrayList<User>();
		}
		return users;
	}

	public boolean addUser(User user) {
		boolean contains = getUsers().contains(user);
		if (!contains) {
			getUsers().add(user);
		}
		return contains;
	}
	
	public Timeline getCurrentTimeline() {
//		if (currentTimeline == null) {
//			currentTimeline = new Timeline();
//		}
		return currentTimeline;
	}

	public void setCurrentTimeline(Timeline currentTimeline) {
		this.currentTimeline = currentTimeline;
	}
	
	public ArrayList<Timeline> getTimelines() {
		if (timelines == null) {
			timelines = new ArrayList<Timeline>();
		}
		return timelines;
	}

	public boolean addTimeline(Timeline timeline) {
		boolean contains = getTimelines().contains(timeline);
		if (!contains) {
			getTimelines().add(timeline);
		}
		return contains;
	}

}
