package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import au.edu.melbuni.boldapp.clients.Client;
import au.edu.melbuni.boldapp.clients.FTPClient;
import au.edu.melbuni.boldapp.models.Likes;
import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Segments;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.Timelines;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;
import au.edu.melbuni.boldapp.persisters.JSONPersister;

public class Synchronizer {

	String serverURI;
	protected Client server; // Yes, "Client server" looks strange, but it fits
								// better with the method calls.

	// Note: We do not talk about this.
	//
	int userSyncCount;
	int timelineSyncCount;

	public Synchronizer(Client server) {
		this.server = server;
	}

	public static Synchronizer getDefault() {
		Client server = new FTPClient("192.168.1.1");
		return new Synchronizer(server);
	}

	// Synchronize and return how many users were synchronized.
	//
	// It first synchronizes all the users, then all the timelines.
	//
	public int[] synchronize(Activity activity) {
		int[] counts = new int[2];

		Users users = Bundler.getUsers(activity);
		Timelines timelines = Bundler.getTimelines(activity);
		counts[0] = synchronize(users);
		
		// Reload everything.
		//
		// Note: Needs a little nap afterwards.
		//
		Bundler.load(activity);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Get the latest users to include the recently synced.
		//
		users = Bundler.getUsers(activity);
		counts[1] = synchronize(timelines, users);

		return counts;
	}

	// Synchronizes the users itself, user after user.
	// (In an unspecified order)
	//
	public int synchronize(final Users users) {
		List<String> remoteUserIds = server.getUserIds();
		List<String> localUserIds = users.getIds();

		userSyncCount = 0;

		synchronizeWithIds(remoteUserIds, localUserIds,
				new SynchronizerCallbacks() {
					@Override
					public void serverMore(String id) {
						// Gets the user including files.
						//
						User user = getUser(id);
						if (user != null) {
							userSyncCount++;
						}
					}

					@Override
					public void localMore(String id) {
						// Try to find it.
						//
						User user = users.find(id);
						if (user != null) {
							push(user);
							userSyncCount++;
						}
					}

					@Override
					public void both(String id) {
						// TODO Only use if the users become editable.
					}
				});

		return userSyncCount;
	}

	protected User getUser(String userId) {
		return server.getUser(userId);
	}

	public boolean push(User user) {
		boolean exists = server.doesUserExist(user.getIdentifier());
		if (!exists) {
			return server.post(user);
		}
		return false;
	}

	protected int synchronize(final Timelines timelines, final Users users) {
		List<String> remoteTimelineIds = server.getTimelineIds();
		List<String> localTimelineIds = timelines.getIds();

		synchronizeWithIds(remoteTimelineIds, localTimelineIds,
				new SynchronizerCallbacks() {
					@Override
					public void serverMore(String id) {
						// Gets the timeline including segments;
						//
						Timeline timeline = getTimeline(id, users);
						if (timeline != null) {
							timelineSyncCount++;
						}
					}

					@Override
					public void localMore(String id) {
						Timeline timeline = timelines.find(id);
						if (timeline != null) {
							push(timeline);
							timelineSyncCount++;
						}
					}

					@Override
					public void both(String id) {
						// TODO
					}
				});

		return timelineSyncCount;
	}

	protected Timeline getTimeline(String timelineId, Users users) {
		Timeline timeline = server.getTimeline(timelineId, users);

		if (timeline != null) {
			getSegments(timeline);
			getLikes(timeline);
		}
		
		return timeline;
	}

	public boolean push(Timeline timeline) {
		boolean exists = server.doesTimelineExist(timeline.getIdentifier());
		if (!exists) {
			server.post(timeline);
			push(timeline.getSegments(), timeline.getIdentifier());
			push(timeline.getLikes(), timeline.getIdentifier());
			return true;
		}
		return false;
	}

	protected void getSegments(Timeline timeline) {
		server.getSegments(timeline.getIdentifier());

		// Load the segments into the timeline.
		//
		Segments segments = Segments.load(new JSONPersister(),
				timeline.getIdentifier());
		if (segments != null) {
			timeline.replaceSegments(segments);
		}
	}
	
	// TODO I need to synchronize the likes as well!
	//
	protected void getLikes(Timeline timeline) {
		List<String> userIds = server.getLikesIds(timeline.getIdentifier());

		if (userIds != null) {
			timeline.setLikes(userIds);
		}
	}

	protected boolean push(Segments segments, String timelineId) {
		for (Segment segment : segments) {
			server.post(segment, timelineId);
		}
		return true;
	}
	
	protected boolean push(Likes likes, String timelineId) {
		for (String userId : likes) {
			server.postLike(userId, timelineId);
		}
		return true;
	}

	protected void synchronizeWithIds(List<String> serverIds,
			List<String> localIds, SynchronizerCallbacks callbacks) {

		List<String> bothIds = intersection(localIds, serverIds);
		List<String> serverMoreIds = difference(serverIds, localIds);
		List<String> localMoreIds = difference(localIds, serverIds);

		// Check what things both have.
		//
		if (!bothIds.isEmpty()) {
			// Send stuff to the server.
			//
			for (String bothId : bothIds) {
				callbacks.both(bothId);
			}
		}

		// Get what we have on the server.
		//
		if (!serverMoreIds.isEmpty()) {
			// Get stuff from the server.
			//
			for (String serverMoreId : serverMoreIds) {
				callbacks.serverMore(serverMoreId);
			}
		}

		// Send what we have more locally.
		//
		if (!localMoreIds.isEmpty()) {
			// Send stuff to the server.
			//
			for (String localMoreId : localMoreIds) {
				callbacks.localMore(localMoreId);
			}
		}
	};

	public List<String> difference(List<String> presumedLarger,
			List<String> presumedSmaller) {
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

	public List<String> intersection(List<String> firstList,
			List<String> secondList) {
		List<String> result = new ArrayList<String>();
		for (String string : firstList) {
			if (secondList.contains(string)) {
				result.add(string);
			}
		}
		return result;
	}

}
