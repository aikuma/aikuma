package au.edu.melbuni.boldapp;

import android.app.Application;

public class BoldApplication extends Application {

	private User currentUser;
	private Timeline currentTimeline;
	private Users users;
	private Timelines timelines;
	
	public BoldApplication() {
		// TODO Perhaps load lazily.
		//
		this.users     = Users.load();
		this.timelines = Timelines.load();
		
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

	public Users getUsers() {
		return users;
	}

	public boolean addUser(User user) {
		boolean notContained = !getUsers().contains(user);
		if (notContained) {
			getUsers().add(user);
		}
		return notContained;
	}
	
	public void clearUsers() {
		System.out.println(users);
		users.clear();
	}
	
	public Timeline getCurrentTimeline() {
		return currentTimeline;
	}

	public void setCurrentTimeline(Timeline currentTimeline) {
		this.currentTimeline = currentTimeline;
	}
	
	public Timelines getTimelines() {
		return timelines;
	}

	public boolean addTimeline(Timeline timeline) {
		boolean notContained = !getTimelines().contains(timeline);
		if (notContained) {
			getTimelines().add(timeline);
		}
		return notContained;
	}

}
