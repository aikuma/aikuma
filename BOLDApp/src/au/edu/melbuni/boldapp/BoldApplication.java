package au.edu.melbuni.boldapp;

import android.app.Application;
import au.edu.melbuni.boldapp.models.AllTimelines;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;


public class BoldApplication extends Application {

	private Users users;

	private User currentUser;
	private Timeline currentTimeline;

	public BoldApplication() {
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
	}

	// Loads all the meta data of the application.
	//
	public void load() {
		Persister persister = new JSONPersister();
		
		// TODO Define the save / load order somewhere else.
		//
		this.users       = persister.loadUsers();
		this.currentUser = persister.loadCurrentUser(users);
		AllTimelines.load(persister, users); // AllTimelines holds the global list of timelines.
		
		// this.currentTimeline = persister.loadCurrentTimeline();
	}

	// Saves all the meta data of the
	// whole application.
	//
	public void save() {
		Persister persister = new JSONPersister();

		// TODO Define the save / load order somewhere else.
		//
		persister.save(getUsers());
		persister.saveCurrentUser(getCurrentUser());
		persister.save(getTimelines());
//		persister.saveCurrentTimeline(currentTimeline);
	}

	public User getCurrentUser() {
		if (currentUser == null) {
			currentUser = User.newUnconsentedUser();
		}
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public Users getUsers() {
		if (users == null) {
			users = new Users();
		}
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
		users.clear();
	}

	public Timeline getCurrentTimeline() {
		return currentTimeline;
	}

	public void setCurrentTimeline(Timeline currentTimeline) {
		this.currentTimeline = currentTimeline;
	}

	public Timelines getTimelines() {
		return AllTimelines.getTimelines();
	}

	public boolean addTimeline(Timeline timeline) {
		boolean notContained = !getTimelines().contains(timeline);
		if (notContained) {
			getTimelines().add(timeline);
		}
		return notContained;
	}

}
