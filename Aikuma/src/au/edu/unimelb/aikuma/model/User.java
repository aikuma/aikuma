package au.edu.unimelb.aikuma.model;

import android.graphics.Bitmap;
import au.edu.unimelb.aikuma.util.ImageUtils;
import au.edu.unimelb.aikuma.util.FileIO;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.json.simple.JSONArray;
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
	 *
	 * @return	A File representing the users directory.
	 */
	private static File getUsersPath() {
		File path = new File(FileIO.getAppRootPath(), "users");
		path.mkdirs();
		return path;
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
	 * @param	languages	A list of the user's langauges.
	 */
	public User(UUID uuid, String name, List<Language> languages) {
		this(uuid, name);
		setLanguages(languages);
	}

	/**
	 * The minimal constructor.
	 *
	 * @param	uuid	The new user's UUID.
	 * @param	name	The new user's name.
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
	 * @param	language	A single language of the user.
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
	 * Returns true if the user has at least one language; false otherwise.
	 *
	 * @return	true if the user has at least one language; false otherwise.
	 */
	public boolean hasALanguage() {
		if (this.languages.size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * Add's another language to the user's list
	 *
	 * @param	language	The language to be added to the user's list of
	 * languages.
	 */
	public void addLanguage(Language language) {
		this.languages.add(language);
	}

	/**
	 * Encodes the User object as a corresponding JSONObject.
	 *
	 * @return	A JSONObject instance representing the User.
	 */
	public JSONObject encode() {
		JSONObject encodedUser = new JSONObject();
		encodedUser.put("name", getName());
		encodedUser.put("uuid", getUUID().toString());
		if (hasALanguage()) {
			encodedUser.put("languages",
					Language.encodeList(getLanguages()));
		}
		return encodedUser;
	}

	/**
	 * Write the user to file in a subdirectory of the user directory named as
	 * the UUID of this user.
	 */
	public void write() throws IOException {
		JSONObject encodedUser = this.encode();

		FileIO.writeJSONObject(new File(getUsersPath(),
				this.getUUID().toString() + "/metadata.json"), encodedUser);
	}

	/**
	 * Read a user from the file; see User.write.
	 *
	 * @param	uuid	The uuid of the user to be read.
	 */
	public static User read(UUID uuid) throws IOException {
		User user;
		JSONObject jsonObj = FileIO.readJSONObject(
				new File(getUsersPath(), uuid.toString() +
				"/metadata.json"));
		List<Language> languages =
				Language.decodeJSONArray((JSONArray) jsonObj.get("languages"));
		if (jsonObj.get("uuid") == null) {
			throw new IOException("No UUID in the JSON file.");
		}
		if (jsonObj.get("name") == null) {
			throw new IOException("No user name in the JSON file.");
		}
		user = new User(UUID.fromString(jsonObj.get("uuid").toString()),
				jsonObj.get("name").toString(), languages);
		return user;
	}

	/**
	 * Read all users from file
	 *
	 * @return	A list of the users found in the users directory.
	 */
	public static List<User> readAll() {
		// Get a list of all the UUIDs of users in the "users" directory.
		List<String> userUUIDs = Arrays.asList(getUsersPath().list());

		// Get the user data from the metadata.json files.
		List<User> users = new ArrayList<User>();
		for (String userUUID : userUUIDs) {
			try {
				users.add(User.read(UUID.fromString(userUUID)));
			} catch (IOException e) {
				// Couldn't read that user for whatever reason (perhaps JSON
				// file wasn't formatted correctly). Lets just ignore that user.
			}
		}
		return users;
	}

	/**
	 * Compares the given object with the User, and returns true if the
	 * Users uuid, name and languages are equal.
	 *
	 * @return	true if the uuid, name and languages of the User are equal;
	 * false otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) {return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		User rhs = (User) obj;
		return new EqualsBuilder()
				.append(uuid, rhs.uuid).append(name, rhs.name)
				.append(languages, rhs.languages).isEquals();
	}
}
