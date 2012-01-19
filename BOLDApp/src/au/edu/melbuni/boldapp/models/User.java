package au.edu.melbuni.boldapp.models;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.Recorder;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;

/*
 * A User has
 * - name
 * - identifier (for file directories etc.)
 * 
 * - a number of files attached to it
 * 
 */
public class User extends Model {
	
	public String name;
	protected UUID uuid;
	protected Segment audio;
	
	// Note: Just some references.
	//
	protected Timelines timelines;
	
	public boolean consented = true;
	
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
		this(name, uuid, new Segment(""));
	}
	
	public User(String name, UUID uuid, Segment segment) {
		this.name  = name;
		this.uuid  = uuid;
		this.audio = segment;
		this.timelines = new Timelines();
	}
	
	public void add(Timeline timeline) {
		this.timelines.add(timeline);
	}
	
	public void addAll(Timelines timelines) {
		this.timelines.addAll(timelines);
	}
	
	public void set(Timelines timelines) {
		this.timelines = timelines;
	}
	
	public Timelines getTimelines() {
		return timelines;
	}
	
	public static User newUnconsentedUser() {
		User newUser = new User();
		newUser.setConsented(false);
		return newUser;
	}
	
	public void setConsented(boolean consented) {
		this.consented = consented;
	}
	
	public boolean hasGivenConsent() {
		return consented;
	}
	
	public void startRecording(Recorder recorder) {
		audio.startRecording(recorder, getProfileAudioPath());
	}
	
	public void stopRecording(Recorder recorder) {
		audio.stopRecording(recorder);
	}
	
	public void startPlaying(Player player, OnCompletionListener listener) {
		audio.startPlaying(player, getProfileAudioPath(), listener);
	}
	
	public void stopPlaying(Player player) {
		audio.stopPlaying(player);
	}
	
	public static User fromHash(Map<String, Object> hash) {
		String name = hash.get("name") == null ? "" : (String) hash.get("name");
		UUID uuid = hash.get("id") == null ? UUID.randomUUID() : UUID.fromString((String) hash.get("id"));
		return new User(name, uuid);
	}
	@Override
	public Map<String, Object> toHash() {
		Map<String, Object> hash = new LinkedHashMap<String, Object>();
		hash.put("name", this.name);
		hash.put("id", this.uuid.toString());
		return hash;
	}
	
	public String getProfileAudioPath() {
		return new JSONPersister().dirForUsers() + getIdentifier() + "/profile";
	}
	
	// Load a user based on his UUID.
	//
	public static User load(Persister persister, String uuid) {
		return persister.loadUser(uuid);
	}
	
	// Save the user's metadata.
	//
	public void save(Persister persister) {
		persister.save(this);
	}
	
	public String getIdentifier() {
		return this.uuid.toString();
	}
	
	public String getNameKey() {
		return "users/" + this.uuid.toString() + "/name";
	}
	
	public String getProfileImagePath() {
		return new JSONPersister().dirForUsers() + getIdentifier() + "/profile.png";
	}
	public Drawable getProfileImage() {
    	return Drawable.createFromPath(getProfileImagePath());
	}
	public String getProfileImageData() {
		try {
			StringBuffer fileData = new StringBuffer(1000);
	        BufferedReader reader = new BufferedReader(
	                new FileReader(getProfileImagePath()));
	        char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead = reader.read(buf)) != -1){
	            String readData = String.valueOf(buf, 0, numRead);
	            fileData.append(readData);
	            buf = new char[1024];
	        }
	        reader.close();
	        return fileData.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	public boolean hasProfileImage() {
		File image = new File(getProfileImagePath());
		return image.exists();
	}
	public void setProfileImage(String data) {
		String fileName = "";
		
		try {
			// TODO DRY. See above.
			//
        	fileName = getProfileImagePath();
        	File file = new File(fileName);
        	file.getParentFile().mkdirs();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(data);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void putProfileImage(byte[] imageData) {
		String fileName = "";
		
		try {
			// TODO DRY. See above.
			//
        	fileName = getProfileImagePath();
        	File file = new File(fileName);
        	file.getParentFile().mkdirs();
        	
        	file.createNewFile();
        	
            FileOutputStream out = new FileOutputStream(fileName);
            BufferedOutputStream bufOut = new BufferedOutputStream(out);
            
			BitmapFactory.Options options = new BitmapFactory.Options();               
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap picture = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
			// Scale.
			int width  = 426 * 254 / 160;
			int height = 256 * 254 / 160;
			Bitmap scaled = Bitmap.createScaledBitmap(picture, width, height, false);
			// Crop.
			scaled = Bitmap.createBitmap(scaled, 135, 0, height, height);
			// Rotate.
//			Matrix matrix = new Matrix();
//			matrix.setRotate(90, scaled.getWidth()/2, scaled.getHeight()/2);
//			scaled = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
			
			scaled.compress(Bitmap.CompressFormat.PNG, 90, bufOut);
			
            bufOut.close();
            
        } catch (Exception e) {
        	System.out.println("ERROR:" + fileName);
            Log.e("Error reading file", e.toString());
        }
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof User) {
			User other = (User) object;
			return name.equals(other.name) &&
				   uuid.equals(other.uuid);
		}
		return false; 
	}
	
	@Override
	public String toString() {
		return "User(" + uuid.toString() + ", \"" + name + "\")";
	}

}
