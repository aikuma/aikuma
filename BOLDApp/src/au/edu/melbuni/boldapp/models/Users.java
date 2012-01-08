package au.edu.melbuni.boldapp.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import au.edu.melbuni.boldapp.persisters.Persister;

/*
 * Class to hold the users.
 * 
 * Also manages saving and loading.
 * 
 */
public class Users extends Model implements Collection<User> { // TODO implements Collection<User>?

	public ArrayList<User> users;

	public Users() {
		this(new ArrayList<User>());
	}
	
	public Users(ArrayList<User> users) {
		this.users = users;
	}
	
	public List<String> getIds() {
		List<String> userIds = new ArrayList<String>();
		for (User user : users) {
			userIds.add(user.getIdentifier());
		}
		return userIds;
	}

	// Persistence.
	//
	
	public void save(Persister persister) {
		persister.save(this);
	}

	public void saveEach(Persister persister) {
		for (User user : users) {
			persister.save(user);
		}
	}
	
	public static Users fromHash(Persister persister, List<String> usersIds) {
		ArrayList<User> users = new ArrayList<User>();
		if (usersIds != null) {
			for (String userId : usersIds) {
				User user = User.load(persister, userId);
				users.add(user);
			}
		}
		return new Users(users);
	}

	@Override
	public Map<String, Object> toHash() {
		Map<String, Object> hash = new LinkedHashMap<String, Object>();
		ArrayList<String> usersIds = new ArrayList<String>();
		for (User user : users) {
			usersIds.add(user.getIdentifier());
		}
		hash.put("users", usersIds);
		return hash;
	}
	
	public User find(String identifier) {
		for (User user : users) {
			if (user.getIdentifier().equals(identifier)) {
				return user;
			}
		}
		return null;
	}
	
	public Timelines getTimelines() {
		SortedSet<Timeline> timelines = new TreeSet<Timeline>();
		for (User user : users) {
			timelines.addAll(user.getTimelines());
		}
		return new Timelines(timelines);
	}
	
	public String toString() {
		return users.toString();
	}

	// Delegation.
	//
	public User get(int index) {
		return users.get(index);
	}

	@Override
	public int size() {
		return users.size();
	}

	@Override
	public boolean add(User user) {
		return users.add(user);
	}

	public boolean contains(User object) {
		return users.contains(object);
	}

	@Override
	public void clear() {
		users.clear();
	}

	@Override
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
