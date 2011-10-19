package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;

public class Timelines {

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
	
	@SuppressWarnings("rawtypes")
	public static Timelines fromJSON(String data) {
//		Map user = (Map) JSONValue.parse(data);
//		String name = user.get("name") == null ? "" : (String) user.get("name");
//		UUID uuid = user.get("uuid") == null ? UUID.randomUUID() : UUID.fromString((String) user.get("uuid"));
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

	public void add(Timeline object) {
		timelines.add(object);
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
	
}
