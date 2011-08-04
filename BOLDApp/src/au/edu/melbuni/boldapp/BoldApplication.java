package au.edu.melbuni.boldapp;

import java.util.ArrayList;

import android.app.Application;

public class BoldApplication extends Application {

	private User currentUser;
	private ArrayList<User> users;
	
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

}
