package au.edu.melbuni.boldapp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONValue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
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
	
	@SuppressWarnings("rawtypes")
	public static User fromJSON(String data) {
		Map user = (Map) JSONValue.parse(data);
		String name = user.get("name") == null ? "" : (String) user.get("name");
		UUID uuid = user.get("uuid") == null ? UUID.randomUUID() : UUID.fromString((String) user.get("uuid"));
		return new User(name, uuid);
	}
	@SuppressWarnings("rawtypes")
	public String toJSON() {
		Map<String, Comparable> user = new LinkedHashMap<String, Comparable>();
		user.put("name", this.name);
		user.put("uuid", this.uuid.toString());
		return JSONValue.toJSONString(user);
	}
	
	
	public String getIdentifierString() {
		return this.uuid.toString();
	}
	
	public String getNameKey() {
		return "users/" + this.uuid.toString() + "/name";
	}
	
	public String getRelativeProfilePathStub() {
		return "users/profile_";
	}
	public String getProfileImagePath() {
		return Bundler.getBasePath() + getRelativeProfilePathStub() + this.uuid.toString() + ".png";
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
			Matrix matrix = new Matrix();
			matrix.setRotate(90, scaled.getWidth()/2, scaled.getHeight()/2);
			scaled = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
			scaled.compress(Bitmap.CompressFormat.PNG, 90, bufOut);
			
            bufOut.close();
            
        } catch (Exception e) {
        	System.out.println("ERROR:" + fileName);
            Log.e("Error reading file", e.toString());
        }
	}

}
