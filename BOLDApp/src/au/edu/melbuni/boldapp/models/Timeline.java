package au.edu.melbuni.boldapp.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Recorder;
import au.edu.melbuni.boldapp.Sounder;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;

/*
 * Controller for the current time line.
 * 
 * Note: Could also be called Segments.
 * 
 */
public class Timeline implements Comparable<Timeline> {
	
	UUID uuid;
	Date date;
	String location;
	Segments segments;
	Likes likes = new Likes();
	
	User user;
	Timelines timelines;
	
	public Timeline() {
		this(UUID.randomUUID());
	}
	
	public Timeline(String identifier) {
		this(UUID.fromString(identifier));
	}
	
	public Timeline(UUID uuid) {
		this(uuid, new Segments());
	}
	
	public Timeline(UUID uuid, Segments segments) {
		this.uuid = uuid;
		this.date = new Date();
		this.location = "Some Location";
		this.segments = segments;
		
		AllTimelines.add(this);
	}
	
	public String getIdentifier() {
		return uuid.toString();
	}
	
	public Likes getLikes() {
		return this.likes;
	}
	
	// TODO Refactor.
	//
	public void setLikes(List<String> userIds) {
		this.likes = new Likes();
		for (String userId : userIds) {
			like(userId);
		}
	}
	
	public void like(String userId) {
		likes.add(userId);
	}
	
	public void unlike(String userId) {
		likes.remove(userId);
	}
	
	public int totalLikes() {
		return likes.size();
	}
	
	public boolean likedBy(String userId) {
		return likes.contains(userId);
	}
	
	public Segments getSegments() {
		return segments;
	}
	
	public void saveEach(Persister persister, String timelineIdentifier) {
		persister.save(timelineIdentifier, segments);
	}
	
	public static Timeline fromHash(Users users, Map<String, Object> hash) {
		UUID uuid = hash.get("id") == null ? UUID.randomUUID() : UUID.fromString((String) hash.get("id"));
		
		String dateString = hash.get("date") == null ? "" : (String) hash.get("date");
		Date date = null;
		try {
			DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz");
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String userReference = hash.get("user_id") == null ? null : (String) hash.get("user_id");
		User user = users.find(userReference);
		
		// No user for this timeline found.
		//
		if (user == null) {
			throw new NullPointerException("No User " + userReference + " for Timeline " + uuid + " found.");
		}
		
		Segments segments = Segments.load(new JSONPersister(), uuid.toString());
		
		if (segments == null) {
			throw new RuntimeException("segments are null");
		}
		
		Timeline timeline = new Timeline(uuid, segments);
		timeline.setDate(date);
		timeline.setUser(user);
		
		return timeline;
	}
	public Map<String, Object> toHash() {
		Map<String, Object> hash = new LinkedHashMap<String, Object>();
		
		hash.put("id", this.getIdentifier());
		hash.put("date", this.date.toGMTString());
		hash.put("user_id", this.user.getIdentifier());
		
		return hash;
	}
	
	// Load a timeline based on its uuid.
	//
	public static Timeline load(Users users, Persister persister, String timelineId) {
		return persister.loadTimeline(users, timelineId);
	}

	public void save(Persister persister) {
		persister.save(this);
	}
	
	public void installOn(Activity activity) {
		segments.installOn(activity, R.id.timeline);
	}

	public void setUser(User user) { // TODO throw NullPointerException?
		this.user = user;
		this.user.add(this); // TODO Ok?
	}

	public User getUser() {
		return user;
	}
	
	public void setDate(Date date) { // TODO throw NullPointerException?
		this.date = date;
	}

	public Date getDate() {
		return date;
	}
	
	public CharSequence getItemText() {
		return date.toLocaleString() + " " + segments.size() + " segment(s)";
	}

	// Delegator methods.
	//
	
	// This plays all segments, one after another.
	//
	// If one is finished, it selects the next etc.
	//
	public void startPlaying(final Player player, final OnCompletionListener onCompletionListener) {
		segments.startPlaying(player, getIdentifier(), new OnCompletionListener() {
			@Override
			public void onCompletion(Sounder sounder) {
				segments.stopPlaying(player);
				if (!segments.selectNext()) {
					if (onCompletionListener != null) { onCompletionListener.onCompletion(sounder); }
					return;
				}
				segments.startPlaying(player, getIdentifier(), this);
			}
		});
	}
	
	public void startPlayingLast(Player player) {
		segments.selectLastForPlaying();
		segments.startPlaying(player, getIdentifier());
	}
	
	public void startPlayingLastByDefault(Player player) {
		segments.startPlaying(player, getIdentifier(), true);
	}

	public void stopPlaying(Player player) {
		segments.stopPlaying(player);
	}

	public void startRecording(Recorder recorder) {
		segments.startRecording(recorder, getIdentifier());
	}

	public void stopRecording(Recorder recorder) {
		segments.stopRecording(recorder);
	}
	
	public boolean removeLast() {
		return segments.removeLast();
	}
	
	public boolean hasSegment() {
		return !segments.isEmpty();
	}
	
	public void selectFirstSegment() {
		if (hasSegment()) {
			segments.select(0);
		}
	}

	@Override
	public int compareTo(Timeline other) {
		if (other instanceof Timeline) {
			return this.getIdentifier().compareTo(other.getIdentifier());
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Timeline) {
			Timeline other = (Timeline) object;
			return this.compareTo(other) == 0;
		}
		return false; 
	}

	public void replaceSegments(Segments segments) {
		this.segments.clear(); // TODO Necessary?
		this.segments = segments;
	}

}
