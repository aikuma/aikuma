package au.edu.melbuni.boldapp.models;

import java.util.Collection;

import au.edu.melbuni.boldapp.persisters.Persister;

public class AllUsers {
	
	static Users users = new Users();
	
	public static Users getUsers() {
		return users;
	}
	
	public static Users load(Persister persister) {
		users = persister.loadUsers();
		return users;
	}

	public static boolean contains(User user) {
		return users.contains(user);
	}
	
	// Adds the user only if it's not already there.
	//
	public static boolean add(User user) {
		return users.add(user);
	}

	public static boolean addAll(Collection<? extends User> userCollection) {
		for (User user : userCollection) {
			add(user);
		}
		return true;
	}

	public static boolean remove(Object user) {
		if (!users.contains(user)) {
			return true;
		}
		return users.remove(user);
	}

	public static boolean removeAll(Collection<?> userCollection) {
		for (Object object: userCollection) {
			remove(object);
		}
		return true;
	}
	
	public static void clear() {
		users.clear();
	}
	
}
