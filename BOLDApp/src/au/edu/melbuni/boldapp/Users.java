package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.Iterator;

/*
 * Class to hold the users.
 * 
 * Also manages saving and loading.
 * 
 */
public class Users {

	public ArrayList<User> users;

	public Users() {
		this.users = new ArrayList<User>();
	}
	
	// Persistence.
	//
	
	// For each file, create a User.
	//
	public static Users load() {
		return Persister.loadUsers();
	}

	// Delegation.
	//
	public User get(int index) {
		return users.get(index);
	}

	public int size() {
		return users.size();
	}

	public void add(User object) {
		users.add(object);
	}

	public boolean contains(User object) {
		return users.contains(object);
	}

	public void clear() {
		users.clear();
	}
	public Iterator<User> iterator() {
		return users.iterator();
	}

}
