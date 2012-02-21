package au.edu.melbuni.boldapp.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Likes implements Iterable<String> {
	
	List<String> userIds = new ArrayList<String>();
	
	public List<String> getUserIds() {
		return userIds;
	}
	
	@Override
	public Iterator<String> iterator() {
		return userIds.iterator();
	}
	
	public void add(String userId) {
		userIds.remove(userId);
		userIds.add(userId);
	}

	public void remove(String userId) {
		userIds.remove(userId);
	}

	public int size() {
		return userIds.size();
	}
	
	public boolean contains(String userId) {
		return userIds.contains(userId);
	}

}
