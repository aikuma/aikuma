package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import au.edu.melbuni.boldapp.persisters.Persister;

/*
 * Class to hold the users.
 * 
 * Also manages saving and loading.
 * 
 */
public class Users implements Collection<User> { // TODO implements Collection<User>?

	public ArrayList<User> users;

	public Users() {
		this.users = new ArrayList<User>();
	}

	// Persistence.
	//

	// Save each user.
	//
	public void saveEach(Persister persister) {
		for (User user : users) {
			user.save(persister);
		}
	}

	public static Users fromJSON(String data) {
		// Map user = (Map) JSONValue.parse(data);
		// String name = user.get("name") == null ? "" : (String)
		// user.get("name");
		// UUID uuid = user.get("uuid") == null ? UUID.randomUUID() :
		// UUID.fromString((String) user.get("uuid"));
		return new Users();
	}

	@SuppressWarnings("rawtypes")
	public String toJSON() {
		Map<String, Comparable> users = new LinkedHashMap<String, Comparable>();
		return JSONValue.toJSONString(users);
	}

	// Delegation.
	//
	public User get(int index) {
		return users.get(index);
	}

	public int size() {
		return users.size();
	}

	public boolean add(User user) {
		return users.add(user);
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

	@Override
	public boolean addAll(Collection<? extends User> collection) {
		return users.addAll(collection);
	}

	@Override
	public boolean contains(Object object) {
		return users.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return users.containsAll(collection);
	}

	@Override
	public boolean isEmpty() {
		return users.isEmpty();
	}

	@Override
	public boolean remove(Object object) {
		return users.remove(object);
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		return users.removeAll(collection);
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		return users.retainAll(collection);
	}

	@Override
	public Object[] toArray() {
		return users.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return users.toArray(array);
	}

}
