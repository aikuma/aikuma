package au.edu.melbuni.boldapp.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.media.MediaPlayer;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Recorder;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
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
	Segments segments;
	
	User user;
	Timelines timelines;
	
	public Timeline(String prefix) {
		this(prefix, UUID.randomUUID());
	}
	
	public Timeline(String prefix, String identifier) {
		this(prefix, UUID.fromString(identifier));
	}
	
	public Timeline(String prefix, UUID uuid) {
		this(prefix, uuid, generateSegmentsFrom(prefix, uuid)); // TODO
	}
	
	public Timeline(String prefix, UUID uuid, Segments segments) {
		this.prefix = prefix;
		this.uuid = uuid;
		this.date = new Date();
		this.location = "Some Location";
		this.segments = segments;
	}
	
	public String getIdentifier() {
		return uuid.toString();
	}
	
	public void saveEach(Persister persister) {
		for (Segment segment : segments) {
			persister.save(segment);
		}
	}
	
	public static Timeline fromHash(Persister persister, Users users, Map<String, Object> hash) {
		String prefix = hash.get("prefix") == null ? "" : (String) hash.get("prefix");
		
		UUID uuid = hash.get("uuid") == null ? UUID.randomUUID() : UUID.fromString((String) hash.get("uuid"));
		
		String dateString = hash.get("date") == null ? "" : (String) hash.get("date");
		Date date = null;
		try {
			DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz");
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String userReference = hash.get("user") == null ? null : (String) hash.get("user");
		User user = users.find(userReference);
		
		if (user == null) {
			throw new NullPointerException("No User " + userReference + " for Timeline " + uuid + " found.");
		}
		
		Segments segments = Segments.load(persister, prefix, uuid);
		
		Timeline timeline = new Timeline(prefix, uuid, segments);
		timeline.setDate(date);
		timeline.setUser(user);
		
		return timeline;
	}
	public Map<String, Object> toHash() {
		Map<String, Object> hash = new LinkedHashMap<String, Object>();
		
		hash.put("prefix", this.prefix);
		hash.put("uuid", this.getIdentifier());
		hash.put("date", this.date.toGMTString());
		hash.put("user", this.user.getIdentifier());
		
		return hash;
	}
	
	public static Segments generateSegmentsFrom(String prefix, UUID uuid) {
		return new Segments(prefix + uuid.toString());
	}
	
	// Load a timeline based on its uuid.
	//
	public static Timeline load(Users users, Persister persister, String uuid) {
		return persister.loadTimeline(users, uuid);
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
	
	public CharSequence getItemText() {
		return date.toLocaleString() + " " + location;
	}

	// Delegator methods.
	//
	
	// This plays all segments, one after another.
	//
	// If one is finished, it selects the next etc.
	//
	// TODO Use our own Interface, not the onCompletionListener.
	//
	public void startPlaying(final Player player, final OnCompletionListener onCompletionListener) {
		segments.startPlaying(player, new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				segments.stopPlaying(player);
				if (!segments.selectNext()) {
					if (onCompletionListener != null) { onCompletionListener.onCompletion(null); }
					return;
				}
				segments.startPlaying(player, this);
			}
		});
	}
	
	public void startPlayingLast(Player player) {
		segments.selectLastForPlaying();
		segments.startPlaying(player);
	}
	
	public void startPlayingLastByDefault(Player player) {
		segments.startPlaying(player, true);
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

}
