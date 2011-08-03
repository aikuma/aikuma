package au.edu.melbuni.boldapp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

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
		String fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
    	fileName += "/";
		return fileName + "users/profile_" + this.uuid.toString() + ".png";
	}
	public Drawable getProfileImage() {
    	return Drawable.createFromPath(getProfileImagePath());
	}
	public boolean hasProfileImage() {
		File image = new File(getProfileImagePath());
		return image.exists();
	}
	public void putProfileImage(byte[] imageData) {
		String fileName = "";
		
		try {
			
        	fileName = getProfileImagePath();

        	File file = new File(fileName);
        	file.getParentFile().mkdirs();
        	file.createNewFile();
        	
            FileOutputStream out = new FileOutputStream(fileName);
            BufferedOutputStream bufOut = new BufferedOutputStream(out);
            bufOut.write(imageData);
            bufOut.close();
            
        } catch (Exception e) {
        	System.out.println("ERROR:" + fileName);
            Log.e("Error reading file", e.toString());
        }
	}

}
