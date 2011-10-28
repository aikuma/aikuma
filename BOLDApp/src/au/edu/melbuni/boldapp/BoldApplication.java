package au.edu.melbuni.boldapp;

import java.util.ArrayList;

import android.app.Application;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;
import au.edu.melbuni.boldapp.models.*;


public class BoldApplication extends Application {

	private Users users;
	private Timelines timelines;

	private User currentUser;
	private Timeline currentTimeline;

	public BoldApplication() {
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
	}

	// Loads all the metadata of the application.
	//
	public void load() {
		Persister persister = new JSONPersister();
		
		// TODO Define the save / load order somewhere else.
		//
		this.users       = persister.loadUsers();
		this.currentUser = persister.loadCurrentUser(users);
//		this.timelines   = persister.loadTimelines();
		// this.currentTimeline = persister.loadCurrentTimeline();
	}

	// Saves all the metadata of the
	// whole application.
	//
	public void save() {
		Persister persister = new JSONPersister();

		// TODO Define the save / load order somewhere else.
		//
		persister.save(getUsers());
		persister.saveCurrentUser(getCurrentUser());
//		persister.save(getTimelines());
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
		if (users == null) {
			users = new Users(new ArrayList<User>());
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
		if (timelines == null) {
			timelines = new Timelines();
		}
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
