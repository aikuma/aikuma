package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.List;

import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;

public class Synchronizer {
	
	String serverURI;
	HTTPClient client;
	
	public Synchronizer(String serverURI) {
		this.serverURI = serverURI;
	}
	
	public void lazilyInitializeClient() {
		if (this.client == null) {
			this.client = new HTTPClient(this.serverURI);
		}
	}
	
	public boolean synchronize(Users users) {
		lazilyInitializeClient();
		
		List<String> serverIds = client.getUserIds();
		List<String> localIds  = users.getIds();
		
		// Check if there are more things on the server.
		//
		List<String> serverMoreIds = difference(serverIds, localIds);
		List<User> moreUsers = new ArrayList<User>();
		if (!serverMoreIds.isEmpty()) {
			// Get stuff from the server.
			//
			for (String userId : serverMoreIds) {
				User user = client.getUser(userId);
				moreUsers.add(user);
			}
		}
		
		// Check if we have more things locally.
		//
		List<String> localMoreIds = difference(localIds, serverIds);
		if (!localMoreIds.isEmpty()) {
			// Send stuff to the server.
			//
			for (String userId : localMoreIds) {
				client.post(users.find(userId));
			}
		}
		
		users.addAll(moreUsers);
		
		return true;
	}
	
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
