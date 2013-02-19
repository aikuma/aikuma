package au.edu.unimelb.aikuma.model;

import android.graphics.Bitmap;
import au.edu.unimelb.aikuma.util.ImageUtils;
import au.edu.unimelb.aikuma.util.FileIO;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.simple.JSONObject;

/**
 * The class that contains user data.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class User {

	/**
	 * Returns the application's users directory
	 */
	private static File getUsersPath() {
		return new File(FileIO.getAppRootPath(), "users");
	}

	/**
	 * The user's UUID.
	 */
	private UUID uuid;
	/**
	 * The user's name.
	 */
	private String name;

	/**
	 * The user's languages
	 */
	private List<Language> languages;

	/**
	 * Constructor that allows for languages to be specified.
	 *
	 * @param	uuid	The new user's UUID.
	 * @param	name	The new user's name.
	 */
	public User(UUID uuid, String name, List<Language> languages) {
		this(uuid, name);
		setLanguages(languages);
	}

	/**
	 * Constructor that doesn't require a language
	 */
	public User(UUID uuid, String name) {
		setUuid(uuid);
		setName(name);
		this.languages = new ArrayList<Language>();
	}

	/**
	 * Constructor that allows for a single language to be specified.
	 *
	 * @param	uuid	The new user's UUID.
	 * @param	name	The new user's name.
	 */
	public User(UUID uuid, String name, Language language) {
		this(uuid, name);
		addLanguage(language);
	}

	/**
	 * Languages accessor.
	 */
	public List<Language> getLanguages() {
		return languages;
	}

	/**
	 * Returns the small version of the image associated with a user. We don't
	 * return a drawable because creating a BitmapDrawable requires access to
	 * Resources.
	 *
	 * @return 	small version of a user's image; null if it doesn't exist.
	 */
	public Bitmap getSmallImage() throws IOException {
		return ImageUtils.retrieveFromFile(getUUID() + ".small.jpg");
	}

	/**
	 * Languages mutator.
	 */
	public void setLanguages(List<Language> languages) { 
		this.languages = languages;
	}

	/**
	 * uuid mutator
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * name mutator
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * uuid accessor
	 */
	public UUID getUUID() {
		return this.uuid;
	}

	/**
	 * name accessor
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns true if the user has at least one language; false otherwise
	 */
	public boolean hasALanguage() {
		if (this.languages.size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if the user has a name; false otherwise
	 */
	public boolean hasName() {
		if (getName() == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns true if the user has a UUID; false otherwise
	 */
	public boolean hasUUID() {
		if (getUUID() == null) {
			return false;
		} else {
			return true;
		}
	}


	/**
	 * Add's another language to the user's list
	 */
	public void addLanguage(Language language) {
		this.languages.add(language);
	}

	/**
	 * Encodes the User object as a corresponding JSONObject.
	 */
	public JSONObject encode() {
		JSONObject encodedUser = new JSONObject();
		if (hasName()) {
			encodedUser.put("name", getName());
		}
		if (hasUUID()) {
			encodedUser.put("uuid", getUUID().toString());
		}
		if (hasALanguage()) {
			encodedUser.put("languages",
					Language.encodeList(getLanguages()));
		}
		return encodedUser;
	}

	/**
	 * Write the user to file
	 */
	public void write() throws IOException {
		JSONObject encodedUser = this.encode();

		StringWriter stringWriter = new StringWriter();
		encodedUser.writeJSONString(stringWriter);
		String jsonStr = stringWriter.toString();
		FileIO.write(new File(getUsersPath(),
				this.getUUID().toString() + "/metadata.json"), jsonStr);
	}

	/**
	 * Read a user from the file
	 */
	//public static User readUser(UUID uuid) {
	//}

	/**
	 * Read all users from file
	 */
	//public static User readUser(UUID uuid) {
	//}
}
