package au.edu.melbuni.boldapp.persisters;

import au.edu.melbuni.boldapp.Timeline;
import au.edu.melbuni.boldapp.Timelines;
import au.edu.melbuni.boldapp.User;
import au.edu.melbuni.boldapp.Users;

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
		return Users.fromJSON(readUsers());
	}
	
	public void save(User user) {
		write(user, user.toJSON());
	}
	
	public User loadUser(String identifier) {
		return User.fromJSON(readUser(identifier));
	}
	
	public void save(Timelines timelines) {
		write(timelines, timelines.toJSON());
		timelines.saveEach(this);
	}
	
	public Timelines loadTimelines() {
		return Timelines.fromJSON(readTimelines());
	}

	public void save(Timeline timeline) {
		write(timeline, timeline.toJSON());
	}
	
	public Timeline loadTimeline(String identifier) {
		return Timeline.fromJSON(readTimeline(identifier));
	}
	
}
