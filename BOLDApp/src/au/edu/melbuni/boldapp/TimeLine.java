package au.edu.melbuni.boldapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONValue;

import android.app.Activity;
import au.edu.melbuni.boldapp.persisters.BasePersister;

/*
 * Controller for the current time line.
 * 
 * Note: Could also be called Segments.
 * 
 */
public class Timeline {
	
	public String identifier;
	User user;
	Date date;
	String location;
	
	Segments segments;

	public Timeline(Activity activity, String identifier) {
		this.date = new Date();
		this.location = "Some Location";
		this.identifier = identifier;
		
		this.segments = BasePersister.loadSegments(this);
	}
	
	// TODO Test!
	//
	@SuppressWarnings("rawtypes")
	public static Timeline fromJSON(String data) {
		Map timeline = (Map) JSONValue.parse(data);
		String identifier = timeline.get("identifier") == null ? "" : (String) timeline.get("identifier");
		DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
		String dateString = timeline.get("date") == null ? "" : (String) timeline.get("date");
		Date date = null;
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Timeline loaded = new Timeline(null, identifier);
//		loaded.setUser(user);
		loaded.setDate(date);
		return loaded;
	}
	@SuppressWarnings("rawtypes")
	public String toJSON() {
		Map<String, Comparable> timeline = new LinkedHashMap<String, Comparable>();
		timeline.put("identifier", this.identifier);
		timeline.put("date", this.date.toString()); // EEE MMM dd HH:mm:ss zzz yyyy
		timeline.put("user_reference", this.user.uuid.toString()); // TODO Beautify. toJSONReference?
		return JSONValue.toJSONString(timeline);
	}

	public void save(BasePersister persister) {
		persister.save(this);
	}
	
	public void installOn(Activity activity) {
		segments.installOn(activity, R.id.timeline);
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}
	
	public void setDate(Date date) {
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
