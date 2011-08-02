package au.edu.melbuni.boldapp;

import java.util.UUID;

/*
 * A User has
 * - name
 * - identifier (for file directories etc.)
 * 
 * - a number of files attached to it
 * 
 */
public class User {
	
	public String name;
	public UUID uuid;
	
	public User() {
		this("");
	}
	
	public User(String name) {
		this(name, UUID.randomUUID());
	}

	public User(String name, UUID uuid) {
		this.name = name;
		this.uuid = uuid;
	}
	
	
	public String getNameKey() {
		return "users/" + this.uuid.toString() + "/name";
	}
	
	public String getImagePath() {
		return "users/profile_" + this.uuid.toString() + ".png";
	}

}
