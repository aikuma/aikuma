package au.edu.melbuni.boldapp.models;

import java.util.Collection;

import au.edu.melbuni.boldapp.persisters.Persister;

public class AllTimelines {
	
	static Timelines timelines = new Timelines();
	
	public static Timelines getTimelines() {
		return timelines;
	}
	
	public static Timelines load(Persister persister, Users users) {
		timelines = persister.loadTimelines(users);
		return timelines;
	}

	public static boolean contains(Timeline timeline) {
		return timelines.contains(timeline);
	}
	
	public static boolean add(Timeline timeline) {
		if (timelines.contains(timeline)) {
			return false;
		}
		return timelines.add(timeline);
	}

	public static boolean addAll(Collection<? extends Timeline> tls) {
		for (Timeline timeline : tls) {
			add(timeline);
		}
		return true;
	}

	public static boolean remove(Object timeline) {
		if (!timelines.contains(timeline)) {
			return true;
		}
		return timelines.remove(timeline);
	}

	public static boolean removeAll(Collection<?> tls) {
		for (Object object: tls) {
			remove(object);
		}
		return true;
	}
	
}
