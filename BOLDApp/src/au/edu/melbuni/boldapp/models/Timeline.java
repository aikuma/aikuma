package au.edu.melbuni.boldapp.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Recorder;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;

/*
 * Controller for the current time line.
 * 
 * Note: Could also be called Segments.
 * 
 */
public class Timeline {
	
	String prefix;
	UUID uuid;
	Date date;
	String location;
	
	User user;
	Timelines timelines;
	Segments segments;
	
	public Timeline(String prefix) {
		this(prefix, UUID.randomUUID());
	}
	
	public Timeline(String prefix, String identifier) {
		this(prefix, UUID.fromString(identifier));
	}
	
	public Timeline(String prefix, UUID uuid) {
		this.prefix = prefix;
		this.uuid = uuid;
		this.date = new Date();
		this.location = "Some Location";
		
		// TODO Move this. One should not have to know what kind of persister it is.
		//
		Persister persister = new JSONPersister();
		this.segments = persister.loadSegments(prefix + getIdentifier());
	}
	
	public String getIdentifier() {
		return uuid.toString();
	}
	
	public static Timeline fromHash(Map<String, Object> hash) {
		String prefix = hash.get("prefix") == null ? "" : (String) hash.get("prefix");
		UUID uuid = hash.get("uuid") == null ? UUID.randomUUID() : UUID.fromString((String) hash.get("uuid"));
		
		String dateString = hash.get("date") == null ? "" : (String) hash.get("date");
		Date date = null;
		try {
			DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String userReference = hash.get("user_reference") == null ? null : (String) hash.get("user_reference");
		User user = Bundler.getUsers(null).find(userReference);

		Timeline timeline = new Timeline(prefix, uuid);
		timeline.setDate(date);
		timeline.setUser(user);
		
		return timeline;
	}
	public Map<String, Object> toHash() {
		Map<String, Object> hash = new LinkedHashMap<String, Object>();
		hash.put("prefix", this.prefix);
		hash.put("date", this.date.toString()); // EEE MMM dd HH:mm:ss zzz yyyy
		
		hash.put("user_reference", this.user.uuid.toString()); // TODO Beautify. toJSONReference?
		return hash;
	}
	
	// Load a timeline based on its uuid.
	//
	public static Timeline load(Persister persister, String uuid) {
		return persister.loadTimeline(uuid);
	}

	public void save(Persister persister) {
		persister.save(this);
	}
	
	public void installOn(Activity activity) {
		segments.installOn(activity, R.id.timeline);
	}

	public void setUser(User user) { // TODO throw NullPointerException?
		this.user = user;
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

	// Delegator methods.
	//
	public void startPlaying(Player player) {
		segments.startPlaying(player);
	}

	public void stopPlaying(Player player) {
		segments.stopPlaying(player);
	}

	public void startRecording(Recorder recorder) {
		segments.startRecording(recorder);
	}

	public void stopRecording(Recorder recorder) {
		segments.stopRecording(recorder);
	}

	public CharSequence getItemText() {
		return date.toLocaleString() + " " + location;
	}
}
