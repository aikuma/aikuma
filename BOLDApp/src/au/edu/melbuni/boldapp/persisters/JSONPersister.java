package au.edu.melbuni.boldapp.persisters;

import java.io.IOException;
import java.util.ArrayList;

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
	
	public void save(Users users) {
		write(users, users.toJSON());
		users.saveEach(this);
	}
	
	public Users loadUsers() {
		Users users = null;
		try {
			users = Users.fromJSON(this, readUsers());
		} catch (IOException e) {
			users = new Users(new ArrayList<User>());
		}
		return users;
	}
	
	public void save(User user) {
		write(user, user.toJSON());
	}
	
	public User loadUser(String identifier) {
		User user = null;
		try {
			user = User.fromJSON(readUser(identifier));
		} catch (IOException e) {
			user = new User();
		}
		return user;
	}
	
	public void save(Timelines timelines) {
		write(timelines, timelines.toJSON());
		timelines.saveEach(this);
	}
	
	public Timelines loadTimelines() {
		Timelines timelines = null;
		try {
			timelines = Timelines.fromJSON(readTimelines());
		} catch (IOException e) {
			timelines = new Timelines();
		}
		return timelines;
	}

	public void save(Timeline timeline) {
		write(timeline, timeline.toJSON());
	}
	
	public Timeline loadTimeline(String identifier) {
		Timeline timeline = null;
		try {
			timeline = Timeline.fromJSON(readTimeline(identifier));
		} catch (IOException e) {
			timeline = new Timeline(null, identifier);
		}
		return timeline;
	}
	
	public void save(Segments segments) {
		write(segments, segments.toJSON());
		segments.saveEach(this);
	}
	
	public Segments loadSegments(Timeline timeline) {
		Segments segments = null;
		try {
			segments = Segments.fromJSON(readSegments());
		} catch (IOException e) {
			segments = new Segments(timeline);
		}
		return new Segments(timeline); // TODO
	}

	public void save(Segment segment) {
		write(segment, segment.toJSON());
	}
	
	public Segment loadSegment(String identifier) {
		Segment segment = null;
		try {
			segment = Segment.fromJSON(readSegment(identifier));
		} catch (IOException e) {
			segment = new Segment(identifier);
		}
		return segment;
	}
	
}
