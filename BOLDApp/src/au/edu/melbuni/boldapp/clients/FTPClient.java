package au.edu.melbuni.boldapp.clients;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPReply;

import au.edu.melbuni.boldapp.models.Segment;
import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;

public class FTPClient extends Basic {
	
	org.apache.commons.net.ftp.FTPClient client;
	
	public FTPClient(String serverURI) {
		super(serverURI);
		this.client = null;
	}
	
	@Override
	public URI getServerURI(String ignored) {
		URI server = null;

		try {
			server = new URI(serverURI);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return server;
	}
	
	@Override
	public void lazilyInitializeClient() {
		if (client == null) {
			client = new org.apache.commons.net.ftp.FTPClient();
		}
	}
	
	public boolean login() {
		lazilyInitializeClient();
		boolean result = false;
		try {
			client.connect(serverURI);
			// client.login(username, password);
			int reply = client.getReplyCode();
			result = FTPReply.isPositiveCompletion(reply);
			client.enterRemotePassiveMode();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	public void cdToBase() {
		if (login()) {
	        try {
		        client.changeWorkingDirectory("bold/data");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void cdToUsers() {
		if (login()) {
	        try {
	        	cdToBase();
		        client.changeWorkingDirectory("users");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void cdToTimelines() {
		if (login()) {
	        try {
	        	cdToBase();
		        client.changeWorkingDirectory("timelines");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void cdToSegments(String timelineId) {
	    try {
	    	cdToTimelines();
		    client.changeWorkingDirectory(timelineId);
		    client.changeWorkingDirectory("segments");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean doesSegmentExist(String timelineId, String segmentId) {
		try {
			if (login()) {
				cdToSegments(timelineId);
				String status = client.getStatus(segmentId);
				return status == segmentId;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean doesExist(Timeline timeline) {
		try {
			if (login()) {
				cdToTimelines();
				String timelineId = timeline.getIdentifier();
				String status = client.getStatus(timelineId);
				return status == timelineId;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean doesExist(User user) {
		try {
			if (login()) {
				cdToUsers();
				String userId = user.getIdentifier();
				String status = client.getStatus(userId);
				return status == userId;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean post(Segment segment, String timelineId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean post(Timeline timeline) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean post(User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Segment getSegment(String segmentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getSegmentIds(String timelineId) {
		if (login()) {
			cdToSegments(timelineId);
			String[] strings = null;
			try {
				strings = client.listNames();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<String> results = new ArrayList<String>();
			for (String string : strings) {
				results.add(string);
			}
			return results;
		} else {
			return new ArrayList<String>(); // TODO Raise?
		}
	}

	@Override
	public Timeline getTimeline(String timelineId, String userId, Users users) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getTimelineIds() {
		if (login()) {
			cdToTimelines();
			String[] strings = null;
			try {
				strings = client.listNames();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<String> results = new ArrayList<String>();
			for (String string : strings) {
				results.add(string);
			}
			return results;
		} else {
			return new ArrayList<String>(); // TODO Raise?
		}
	}

	@Override
	public User getUser(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getUserIds() {
		if (login()) {
			cdToUsers();
			String[] strings = null;
			try {
				strings = client.listNames();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<String> results = new ArrayList<String>();
			for (String string : strings) {
				results.add(string);
			}
			return results;
		} else {
			return new ArrayList<String>(); // TODO Raise?
		}
	}

}
