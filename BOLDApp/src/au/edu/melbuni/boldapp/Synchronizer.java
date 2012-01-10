package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.List;

import au.edu.melbuni.boldapp.clients.Client;
import au.edu.melbuni.boldapp.clients.FTPClient;
import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Segments;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;

public class Synchronizer {
	
	String serverURI;
	Client server; // Yes, "Client server" looks strange, but it fits better with the method calls.
	
	public Synchronizer(String serverURI) {
		this.serverURI = serverURI;
	}
	
	public void lazilyInitializeClient() {
		if (this.server == null) {
			this.server = new FTPClient(this.serverURI);
		}
	}
	
	// Synchronizes depth-first:
	//  1. The users itself, user after user.
	//  2. The timelines for each user.
	//  3. The segments for each timeline.
	//
	public boolean synchronize(final Users users) {
		lazilyInitializeClient();
		
		final List<User> serverMoreUsers = new ArrayList<User>();
		
		List<String> userIds = null;
//		try {
			userIds = server.getUserIds();
//		} catch(NullPointerException ne) {
//			return false; // TODO Refactor and use specialized Exception.
//		}
		synchronizeWithIds(userIds, users.getIds(), new SynchronizerCallbacks() {
			@Override
			public void serverMore(String id) {
				User user = server.getUser(id);
				serverMoreUsers.add(user);
			}
			
			@Override
			public void localMore(String id) {
				server.post(users.find(id));
			}
		});
		
		users.addAll(serverMoreUsers);
		
		// Synchronize the user's timelines.
		//
		// Note: Synchronizes each at a time.
		//
		for (User user : users) {
			synchronize(user, users);
		}
		
		return true;
	}
	
	public boolean synchronize(User user, Users users) {
		lazilyInitializeClient();
		
		User serverUser = server.getUser(user.getIdentifier());
		if (serverUser == null) {
			server.post(user);
		}
		
		synchronize(user.getTimelines(), user, users);
		
		return true;
	}
	
	public boolean synchronize(final Timelines timelines, final User user, final Users users) {
		lazilyInitializeClient();
		
		final List<Timeline> moreTimelines = new ArrayList<Timeline>();
		
		synchronizeWithIds(server.getTimelineIds(), timelines.getIds(), new SynchronizerCallbacks() {
			@Override
			public void serverMore(String id) {
				Timeline timeline = server.getTimeline(id, user.getIdentifier(), users);
				moreTimelines.add(timeline);
			}
			
			@Override
			public void localMore(String id) {
				server.post(timelines.find(id));
			}
		});
		
		timelines.addAll(moreTimelines);
		
		// Synchronize the timeline's segments.
		//
		// Note: Synchronizes each at a time.
		//
		List<Timeline> copiedList = new ArrayList<Timeline>(timelines);
		for (Timeline timeline : copiedList) {
			synchronize(timeline, users);
		}
		
		return true;
	}
	
	public boolean synchronize(Timeline timeline, Users users) {
		lazilyInitializeClient();
		
		Timeline serverTimeline = server.getTimeline(timeline.getIdentifier(), timeline.getUser().getIdentifier(), users);
		if (serverTimeline == null) {
			server.post(timeline);
		}
		
		synchronize(timeline.getSegments(), timeline);
		
		return true;
	}
	
	public boolean synchronize(final Segments segments, final Timeline timeline) {
		lazilyInitializeClient();
		
		final List<Segment> moreSegments = new ArrayList<Segment>();
		
		List<String> serverSegmentIds = server.getSegmentIds(timeline.getIdentifier());
		
		synchronizeWithIds(
			serverSegmentIds,
			segments.getIds(),
			new SynchronizerCallbacks() {
				@Override
				public void serverMore(String id) {
					Segment segment = server.getSegment(id, timeline.getIdentifier());
					if (segment != null) {
						moreSegments.add(segment);
					}
				}
			
				@Override
				public void localMore(String id) {
					server.post(segments.find(id), timeline.getIdentifier());
				}
			}
		);
		
		segments.addAll(moreSegments);
		
		synchronized (segments) {
			for (Segment segment : segments) {
				synchronize(segment, timeline);
			}	
		}
		
		return true;
	}
	
	public boolean synchronize(Segment segment, Timeline timeline) {
		lazilyInitializeClient();
		
		if (!server.doesExist(timeline, segment)) {
			server.post(segment, timeline.getIdentifier());
		}
		
		return true;
	}
	
	public void synchronizeWithIds(List<String> serverIds, List<String> localIds, SynchronizerCallbacks callbacks) {
		// Check if there are more things on the server.
		//
		List<String> serverMoreIds = difference(serverIds, localIds);
		if (!serverMoreIds.isEmpty()) {
			// Get stuff from the server.
			//
			for (String serverMoreId : serverMoreIds) {
				callbacks.serverMore(serverMoreId);
			}
		}
		
		// Check if we have more things locally.
		//
		List<String> localMoreIds = difference(localIds, serverIds);
		if (!localMoreIds.isEmpty()) {
			// Send stuff to the server.
			//
			for (String localMoreId : localMoreIds) {
				callbacks.localMore(localMoreId);
			}
		}
	};
	
	public List<String> difference(List<String> presumedLarger, List<String> presumedSmaller) {
		// Copy the larger List.
		//
		List<String> largerCopy = new ArrayList<String>();
		for (String string : presumedLarger) {
			largerCopy.add(string);
		}
		
		// Remove all.
		//
		largerCopy.removeAll(presumedSmaller);
		return largerCopy;
	}
	
}
