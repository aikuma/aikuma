package au.edu.melbuni.boldapp.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import au.edu.melbuni.boldapp.persisters.Persister;

/*
 * Class to hold ALL the timelines.
 * 
 * Also manages saving and loading.
 * 
 */
public class Timelines extends Model implements Collection<Timeline> {
	
	ArrayList<Timeline> timelines;

	public Timelines() {
		this(new ArrayList<Timeline>());
	}
	
	public Timelines(ArrayList<Timeline> timelines) {
		this.timelines = timelines;
	}
	
	public List<String> getIds() {
		List<String> timelineIds = new ArrayList<String>();
		for (Timeline timeline : timelines) {
			timelineIds.add(timeline.getIdentifier());
		}
		return timelineIds;
	}

	// Persistence.
	//

	public void saveEach(Persister persister) {
		for (Timeline timeline : timelines) {
			persister.save(timeline);
		}
	}
	
	public static String getDefaultPrefix() {
		return "timeline_";
	}
	
	@SuppressWarnings("unchecked")
	public static Timelines fromHash(Persister persister, Users users, Map<String, Object> hash) {
		ArrayList<String> timelineIds = (ArrayList<String>) hash.get("timelines");
		ArrayList<Timeline> timelines = new ArrayList<Timeline>();
		if (timelineIds != null) {
			for (String timelineId : timelineIds) {
				Timeline timeline = Timeline.load(users, persister, timelineId);
				timelines.add(timeline);
			}
		}
		return new Timelines(timelines);
	}

	public Map<String, Object> toHash() {
		Map<String, Object> hash = new LinkedHashMap<String, Object>();
		
		ArrayList<String> timelinesIds = new ArrayList<String>();
		for (Timeline timeline : timelines) {
			timelinesIds.add(timeline.getIdentifier());
		}
		hash.put("timelines", timelinesIds);
		
		return hash;
	}
	
//	public static Timelines fromJSON(String data) {
//		// Map user = (Map) JSONValue.parse(data);
//		// String name = user.get("name") == null ? "" : (String)
//		// user.get("name");
//		// UUID uuid = user.get("uuid") == null ? UUID.randomUUID() :
//		// UUID.fromString((String) user.get("uuid"));
//		return new Timelines();
//	}
//
//	@SuppressWarnings("rawtypes")
//	public String toJSON() {
//		Map<String, Comparable> timelines = new LinkedHashMap<String, Comparable>();
//		return JSONValue.toJSONString(timelines);
//	}
	
	public Timeline find(String identifier) {
		for (Timeline timeline : timelines) {
			if (timeline.getIdentifier().equals(identifier)) {
				return timeline;
			}
		}
		return null;
	}


	// Delegation.
	//
	public Timeline get(int index) {
		return timelines.get(index);
	}

	public int size() {
		return timelines.size();
	}

	public boolean add(Timeline timeline) {
		boolean result = timelines.add(timeline);
		AllTimelines.add(timeline);
		return result;
	}

	public boolean contains(Timeline object) {
		return timelines.contains(object);
	}

	public void clear() {
		timelines.clear();
		AllTimelines.removeAll(timelines);
	}

	public Iterator<Timeline> iterator() {
		return timelines.iterator();
	}

	@Override
	public boolean addAll(Collection<? extends Timeline> collection) {
		boolean result = timelines.addAll(collection);
		AllTimelines.addAll(collection);
		return result;
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
	public boolean remove(Object timeline) {
		boolean result = timelines.remove(timeline);
		AllTimelines.remove(timeline);
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean result = timelines.removeAll(collection);
		AllTimelines.removeAll(collection);
		return result;
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
