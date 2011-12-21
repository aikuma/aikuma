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
	
	@Override
	public String fileExtension() {
		return ".json";
	}
	
	@Override
	public String toJSON(Object object) {
		return JSONValue.toJSONString(object);
	}
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> fromJSON(String jsonData) {
		return (Map<String, Object>) JSONValue.parse(jsonData);
	}
	
	@Override
	public void save(Users users) {
		write(users, toJSON(users.toHash()));
		users.saveEach(this);
	}
	
	@Override
	public Users loadUsers() {
		Users users = null;
		try {
			users = Users.fromHash(this, fromJSON(readUsers()));
		} catch (IOException e) {
			users = new Users();
		}
		return users;
	}
	
	@Override
	public void save(User user) {
		write(user, toJSON(user.toHash()));
	}
	
	@Override
	public User loadUser(String identifier) {
		User user = null;
		try {
			user = User.fromHash(fromJSON(readUser(identifier)));
		} catch (IOException e) {
			user = new User(UUID.fromString(identifier));
		}
		return user;
	}
	
	@Override
	public void save(Timelines timelines) {
		write(timelines, toJSON(timelines.toHash()));
		timelines.saveEach(this);
	}
	
	@Override
	public Timelines loadTimelines(Users users) {
		Timelines timelines = null;
		try {
			timelines = Timelines.fromHash(this, users, fromJSON(readTimelines()));
		} catch (IOException e) {
			timelines = new Timelines();
		}
		return timelines;
	}

	@Override
	public void save(Timeline timeline) {
		write(timeline, toJSON(timeline.toHash()));
		timeline.saveEach(this);
	}
	
	@Override
	public Timeline loadTimeline(Users users, String identifier) {
		Timeline timeline = null;
		try {
			timeline = Timeline.fromHash(users, fromJSON(readTimeline(identifier)));
		} catch (IOException e) {
			// System.out.println("OUCH " + e);
		}
		return timeline;
	}
	
	@Override
	public void save(Segments segments) {
		write(segments, toJSON(segments.toHash()));
		segments.saveEach(this);
	}
	
	@Override
	public Segments loadSegments(String prefix) {
		Segments segments = null;
		try {
			segments = Segments.fromHash(this, fromJSON(readSegments()));
		} catch (IOException e) {
			// segments = new Segments(prefix);
		}
		return segments;
	}

	@Override
	public void save(Segment segment) {
		write(segment, toJSON(segment.toHash()));
	}
	
	@Override
	public Segment loadSegment(String identifier) {
		Segment segment = null;
		try {
			segment = Segment.fromHash(fromJSON(readSegment(identifier)));
		} catch (IOException e) {
			// segment = new Segment(identifier);
		}
		return segment;
	}
	
}
