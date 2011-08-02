package au.edu.melbuni.boldapp;

import java.util.UUID;

import android.graphics.drawable.Drawable;
import android.os.Environment;

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
	
	public User(UUID uuid) {
		this("", uuid);
	}
	
	public User(String name) {
		this(name, UUID.randomUUID());
	}

	public User(String name, UUID uuid) {
		this.name = name;
		this.uuid = uuid;
	}
	
	
	public String getIdentifierString() {
		return this.uuid.toString();
	}
	
	public String getNameKey() {
		return "users/" + this.uuid.toString() + "/name";
	}
	
	public String getProfileImagePath() {
		return "users/profile_" + this.uuid.toString() + ".png";
	}
	public Drawable getProfileImage() {
		String fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
    	fileName += "/";
    	fileName += getProfileImagePath();
    	
    	return Drawable.createFromPath(fileName);
	}

}
