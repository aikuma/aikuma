package au.edu.melbuni.boldapp;

import android.app.Application;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;

public class BoldApplication extends Application {

	private Users users;
	private Timelines timelines;

	private User currentUser;
	private Timeline currentTimeline;

	public BoldApplication() {
		load();
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
	}

	// Loads all the metadata of the application.
	//
	public void load() {
		Persister persister = new JSONPersister();
		
		// TODO Define the save / load order somewhere else.
		//
		this.users     = persister.loadUsers();
		this.timelines = persister.loadTimelines();
		this.currentUser = persister.loadCurrentUser(users);
		// this.currentTimeline = persister.loadCurrentTimeline();
	}

	// Saves all the metadata of the
	// whole application.
	//
	public void save() {
		Persister persister = new JSONPersister();

		// TODO Define the save / load order somewhere else.
		//
		persister.save(users);
		persister.save(timelines);
		persister.saveCurrentUser(currentUser);
//		persister.saveCurrentTimeline(currentTimeline);
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
