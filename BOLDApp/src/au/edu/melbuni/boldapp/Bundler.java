package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Bundle;

/*
 * This class helps with saving state.
 */
public class Bundler {
	
	public static void saveNewUser(Bundle bundle, User user) {
		saveUser(bundle, user);
		setCurrentUser(bundle, user);
		addUser(bundle, user);
	}
	
	public static void saveUser(Bundle bundle, User user) {
		bundle.putString(user.getNameKey(), user.name);
	}
	
	public static void setCurrentUser(Bundle bundle, User user) {
		bundle.putString("users/current", user.getIdentifierString());
	}
	
	public static void addUser(Bundle bundle, User user) {
		ArrayList<String> users = bundle.getStringArrayList("users");
		if (users == null) {
			users = new ArrayList<String>();
		}
		if (!users.contains(user.getIdentifierString())) {
			users.add(user.getIdentifierString());
		}
	}
	
	public static User getCurrentUser(Bundle bundle) {
		String identifier = bundle.getString("users/current");
		return getUser(bundle, identifier);
	}
	public static User getUser(Bundle bundle, String identifier) {
		User user = new User(identifier);
		user.name = bundle.getString(user.getNameKey());
		return user;
	}
	
	public static ArrayList<User> getUsers(Bundle bundle) {
		ArrayList<String> users = bundle.getStringArrayList("users");
		Iterator<String> usersIterator = users.iterator();
		
		ArrayList<User> result = new ArrayList<User>();
		
		while (usersIterator.hasNext()) {
			String identifier = usersIterator.next();
			User nextUser = getUser(bundle, identifier);
			result.add(nextUser);
		}
		
		return result;
	}
	
}
