package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import au.edu.melbuni.boldapp.persisters.Persister;

public class Timelines implements Collection<Timeline> {

	public ArrayList<Timeline> timelines;

	public Timelines() {
		this.timelines = new ArrayList<Timeline>();
	}

	// Persistence.
	//

	public void saveEach(Persister persister) {
		for (Timeline timeline : timelines) {
			timeline.save(persister);
		}
	}

	public static Timelines fromJSON(String data) {
		// Map user = (Map) JSONValue.parse(data);
		// String name = user.get("name") == null ? "" : (String)
		// user.get("name");
		// UUID uuid = user.get("uuid") == null ? UUID.randomUUID() :
		// UUID.fromString((String) user.get("uuid"));
		return new Timelines();
	}

	@SuppressWarnings("rawtypes")
	public String toJSON() {
		Map<String, Comparable> timelines = new LinkedHashMap<String, Comparable>();
		return JSONValue.toJSONString(timelines);
	}

	// Delegation.
	//
	public Timeline get(int index) {
		return timelines.get(index);
	}

	public int size() {
		return timelines.size();
	}

	public boolean add(Timeline object) {
		return timelines.add(object);
	}

	public boolean contains(Timeline object) {
		return timelines.contains(object);
	}

	public void clear() {
		timelines.clear();
	}

	public Iterator<Timeline> iterator() {
		return timelines.iterator();
	}

	@Override
	public boolean addAll(Collection<? extends Timeline> collection) {
		return timelines.addAll(collection);
	}

	@Override
	public boolean contains(Object object) {
		return timelines.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return timelines.containsAll(collection);
	}

	@Override
	public boolean isEmpty() {
		return timelines.isEmpty();
	}

	@Override
	public boolean remove(Object object) {
		return timelines.remove(object);
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		return timelines.removeAll(collection);
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		return timelines.retainAll(collection);
	}

	@Override
	public Object[] toArray() {
		return timelines.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return timelines.toArray(array);
	}

}
