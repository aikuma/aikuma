package au.edu.melbuni.boldapp.persisters;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONValue;

import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Segments;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;

// This class specializes in JSON saving.
//
public class JSONPersister extends Persister {
	
	public String fileExtension() {
		return ".json";
	}
	
	public String toJSON(Object object) {
		return JSONValue.toJSONString(object);
	}
	@SuppressWarnings("unchecked")
	public Map<String, Object> fromJSON(String jsonData) {
		return (Map<String, Object>) JSONValue.parse(jsonData);
	}
	
	public void save(Users users) {
		write(users, toJSON(users.toHash()));
		users.saveEach(this);
	}
	
	public Users loadUsers() {
		Users users = null;
		try {
			users = Users.fromHash(this, fromJSON(readUsers()));
		} catch (IOException e) {
			users = new Users();
		}
		return users;
	}
	
	public void save(User user) {
		write(user, toJSON(user.toHash()));
	}
	
	public User loadUser(String identifier) {
		User user = null;
		try {
			user = User.fromHash(fromJSON(readUser(identifier)));
		} catch (IOException e) {
			user = new User(UUID.fromString(identifier));
		}
		return user;
	}
	
	public void save(Timelines timelines) {
		write(timelines, toJSON(timelines.toHash()));
		timelines.saveEach(this);
	}
	
	public Timelines loadTimelines(Users users) {
		Timelines timelines = null;
		try {
			timelines = Timelines.fromHash(this, users, fromJSON(readTimelines()));
		} catch (IOException e) {
			timelines = new Timelines();
		}
		return timelines;
	}

	public void save(Timeline timeline) {
		write(timeline, toJSON(timeline.toHash()));
		timeline.saveEach(this);
	}
	
	public Timeline loadTimeline(Users users, String identifier) {
		Timeline timeline = null;
		try {
			timeline = Timeline.fromHash(users, fromJSON(readTimeline(identifier)));
		} catch (IOException e) {
			System.out.println("OUCH " + e);
		}
		return timeline;
	}
	
	public void save(Segments segments) {
		write(segments, toJSON(segments.toHash()));
		segments.saveEach(this);
	}
	
	public Segments loadSegments(String prefix) {
		Segments segments = null;
		try {
			segments = Segments.fromHash(this, fromJSON(readSegments()));
		} catch (IOException e) {
			segments = new Segments(prefix);
		}
		return segments;
	}

	public void save(Segment segment) {
		write(segment, toJSON(segment.toHash()));
	}
	
	public Segment loadSegment(String identifier) {
		Segment segment = null;
		try {
			segment = Segment.fromHash(fromJSON(readSegment(identifier)));
		} catch (IOException e) {
			segment = new Segment(identifier);
		}
		return segment;
	}
	
}
