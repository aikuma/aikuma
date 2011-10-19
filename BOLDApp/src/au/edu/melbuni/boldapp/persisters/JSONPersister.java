package au.edu.melbuni.boldapp.persisters;

import au.edu.melbuni.boldapp.User;

// This class specializes in JSON saving.
//
public class JSONPersister extends Persister {
	
	public String fileExtension() {
		return ".json";
	}
	
	// Save method for user metadata.
	//
	public void save(User user) {
		write(user, user.toJSON());
	}
	
	// Load method for user metadata.
	//
	public User loadUser(String identifier) {
		return User.fromJSON(readUser(identifier));
	}
	
}
