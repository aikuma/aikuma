package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.Iterator;

import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;

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
		return JSONPersister.loadUsers();
	}
	
	// Save the users metadata and each user.
	//
	public void save(Persister persister) {
		// Save each user.
		for (User user : users) {
			user.save(persister);
		}
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
