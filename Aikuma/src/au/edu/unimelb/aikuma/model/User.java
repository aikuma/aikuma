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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
	 * Read a user from the file.
	 */
	public static User read(UUID uuid) throws IOException {
		User user;
		try {
			JSONParser parser = new JSONParser();
			String jsonStr = FileIO.read(
					new File(getUsersPath(), uuid.toString() +
					"/metadata.json"));
			Object obj = parser.parse(jsonStr);
			JSONObject jsonObj = (JSONObject) obj;
			JSONArray languagesArray = (JSONArray) jsonObj.get("languages");
			List<Language> languages = new ArrayList<Language>();
			if (languagesArray != null) {
				for (Object langObj : languagesArray) {
					JSONObject jsonLangObj = (JSONObject) langObj;
					Language lang = new Language(
							jsonLangObj.get("name").toString(),
							jsonLangObj.get("code").toString());
					languages.add(lang);
				}
			}
			if (jsonObj.get("uuid") == null) {
				throw new IOException("No UUID in the JSON file.");
			}
			if (jsonObj.get("name") == null) {
				throw new IOException("No user name in the JSON file.");
			}
			user = new User(UUID.fromString(jsonObj.get("uuid").toString()),
					jsonObj.get("name").toString(), languages);
		} catch (org.json.simple.parser.ParseException e) {
			throw new IOException(e);
		}
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
