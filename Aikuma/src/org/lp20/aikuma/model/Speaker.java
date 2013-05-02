package org.lp20.aikuma.model;

import android.graphics.Bitmap;
import android.util.Log;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.ImageUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * The class that stores the data about a speaker.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Speaker {

	/**
	 * The minimal constructor; do not use if the Speaker already has a UUID,
	 * as a new UUID will be generated.
	 */
	public Speaker() {
		setUUID(UUID.randomUUID());
		setLanguages(new ArrayList<Language>());
	}

	/**
	 * The complete constructor
	 *
	 * @param	uuid	The UUID of the speaker
	 * @param	name	The name of the speaker
	 * @param	languages	A list of languages of the speaker.
	 */
	public Speaker(UUID uuid, String name, List<Language> languages) {
		setUUID(uuid);
		setName(name);
		setLanguages(languages);
	}

	/**
	 * Gets the UUID of the speaker.
	 *
	 * @return	A UUID object.
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Gets the name of the Speaker.
	 *
	 * @return	A String object.
	 */
	public String getName() {
		if (name == null) {
			return "";
		}
		return name;
	}

	/**
	 * Gets the list of languages associated with the Speaker.
	 *
	 * @return	A List of Language objects.
	 */
	public List<Language> getLanguages() {
		return languages;
	}

	/**
	 * Returns true if the Speaker has at least one language; false otherwise.
	 *
	 * @return	true if the Speaker has at least one language; false otherwise.
	 */
	public boolean hasALanguage() {
		if (languages.size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the Speaker's image.
	 *
	 * @return	A Bitmap object.
	 */
	public Bitmap getImage() throws IOException {
		return ImageUtils.retrieveFromFile(getImage() + ".jpg");
	}

	/**
	 * Gets the small version of the Speaker's image.
	 *
	 * @return	A Bitmap object.
	 */
	public Bitmap getSmallImage() throws IOException {
		return ImageUtils.retrieveFromFile(getSmallImage() + ".small.jpg");
	}

	/**
	 * Encodes the Speaker object as a corresponding JSONObject.
	 *
	 * @return	A JSONObject instance representing the Speaker.
	 */
	public JSONObject encode() {
		JSONObject encodedSpeaker = new JSONObject();
		encodedSpeaker.put("name", this.name);
		encodedSpeaker.put("uuid", this.uuid.toString());
		encodedSpeaker.put("languages", Language.encodeList(languages));
		return encodedSpeaker;
	}

	/**
	 * Encodes a list of speakers as a corresponding JSONArray object of their
	 * UUIDs.
	 *
	 * @param	speakers	A list of speakers to be encoded
	 * @return	A JSONArray object.
	 */
	public static JSONArray encodeList(List<Speaker> speakers) {
		JSONArray speakerArray = new JSONArray();
		for (Speaker speaker : speakers) {
			speakerArray.add(speaker.getUUID().toString());
		}
		return speakerArray;
	}

	/**
	 * Decodes a list of speakers from a JSONArray
	 *
	 * @param	speakerArray	A JSONArray object containing the speakers.
	 * @return	A list of the speakers in the JSONArray
	 */
	public static List<UUID> decodeJSONArray(JSONArray speakerArray) {
		List<UUID> speakerUUIDs = new ArrayList<UUID>();
		if (speakerArray != null) {
			for (Object speakerObj : speakerArray) {
				UUID speakerUUID = UUID.fromString((String) speakerObj);
				speakerUUIDs.add(speakerUUID);
			}
		}
		return speakerUUIDs;
	}

	/**
	 * Write the Speaker to file iin a subdirectory of the Speakers directory
	 * named as <uuid>.json
	 */
	public void write() throws IOException {
		JSONObject encodedSpeaker = this.encode();

		FileIO.writeJSONObject(new File(getSpeakersPath(), getUUID() + 
				"/metadata.json"), encodedSpeaker);
	}

	/**
	 * read a Speaker from the file containing the JSON describing the speaker
	 *
	 * @param	uuid	A UUID object representing the UUID of the Speaker.
	 */
	public static Speaker read(UUID uuid) throws IOException {
		JSONObject jsonObj = FileIO.readJSONObject(
				new File(getSpeakersPath(), uuid + "/metadata.json"));
		String uuidString = (String) jsonObj.get("uuid");
		if (uuidString == null) {
			throw new IOException("Null UUID in the JSON file.");
		}
		UUID readUUID = UUID.fromString(uuidString);
		if (!readUUID.equals(uuid)) {
			throw new IOException("UUID of the filename is different to UUID" +
					"in the file's JSON");
		}
		String name = (String) jsonObj.get("name");
		JSONArray languageArray = (JSONArray) jsonObj.get("languages");
		if (languageArray == null) {
			throw new IOException("Null languages in the JSON file.");
		}
		List<Language> languages = Language.decodeJSONArray(languageArray);
		return new Speaker(uuid, name, languages);
	}

	/**
	 * Read all users from file
	 *
	 * @return	A list of the users found in the users directory.
	 */
	public static List<Speaker> readAll() {
		// Get a list of all the UUIDs of users in the "users" directory.
		List<String> speakerUUIDs = Arrays.asList(getSpeakersPath().list());

		// Get the user data from the metadata.json files.
		List<Speaker> speakers = new ArrayList<Speaker>();
		for (String speakerUUID : speakerUUIDs) {
			try {
				speakers.add(Speaker.read(UUID.fromString(speakerUUID)));
			} catch (IOException e) {
				// Couldn't read that user for whatever reason (perhaps JSON
				// file wasn't formatted correctly). Lets just ignore that user.
			}
		}
		return speakers;
	}

	/**
	 * Compares the given object with the Speaker, and returns true if the
	 * Speaker uuid, name and languages are equal.
	 *
	 * @return	true if the uuid, name and languages of the Speaker are equal;
	 * false otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) {return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		Speaker rhs = (Speaker) obj;
		return new EqualsBuilder()
				.append(uuid, rhs.uuid).append(name, rhs.name)
				.append(languages, rhs.languages).isEquals();
	}

	/**
	 * Get the directory where the Speaker data is stored.
	 *
	 * @return	A file representing the path of the Speakers directory.
	 */
	 private static File getSpeakersPath() {
	 	File path = new File(FileIO.getAppRootPath(), "speakers");
		path.mkdirs();
		return path;
	 }

	/**
	 * Sets the UUID of the Speaker.
	 *
	 * @param	uuid	A UUID object representing the Speaker's UUID.
	 */
	private void setUUID(UUID uuid) throws IllegalArgumentException {
		if (uuid == null) {
			throw new IllegalArgumentException("Speaker UUID cannot be null.");
		}
		this.uuid = uuid;
	}

	/**
	 * Sets the name of the Speaker.
	 *
	 * @param	name	A String object representing the Speaker's name.
	 */
	private void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the Languages of the Speaker.
	 *
	 * @param	languages	A List<Language> object representing the languages
	 * associated with the Speaker.
	 */
	private void setLanguages(List<Language> languages) throws
			IllegalArgumentException {
		if (languages == null) {
			throw new IllegalArgumentException("Speaker languages cannot be null.");
		}
		this.languages = languages;
	}


	/**
	 * The UUID of the Speaker.
	 */
	private UUID uuid;

	/**
	 * The name of the Speaker.
	 */
	private String name;

	/**
	 * The languages of the Speaker.
	 */
	private List<Language> languages;

}
