package au.edu.melbuni.boldapp.clients;

import java.net.URI;
import java.util.List;

import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;

public abstract class Client {

	protected String serverURI;
	
	public Client(String serverURI) {
		this.serverURI = serverURI;
	}
	
	public boolean doesExist(Timeline timeline, Segment segment) {
		return doesSegmentExist(timeline.getIdentifier(), segment.getIdentifier());
	}
	
	public abstract Object getClient();
	
	public abstract URI getServerURI(String path);

	public abstract void lazilyInitializeClient();

	public abstract boolean doesSegmentExist(String timelineId, String segmentId);

	public abstract boolean doesExist(Timeline timeline);

	public abstract boolean doesExist(User user);

	public abstract boolean post(Segment segment, String timelineId);

	public abstract boolean post(Timeline timeline);

	public abstract boolean post(User user);

	public abstract Segment getSegment(String segmentId, String timelineId);

	public abstract List<String> getSegmentIds(String timelineId);

	public abstract Timeline getTimeline(String timelineId, String userId, Users users);

	public abstract List<String> getTimelineIds();

	public abstract User getUser(String userId);

	public abstract List<String> getUserIds();

}