/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.model;

import android.graphics.Bitmap;
import android.os.Parcelable;
import android.os.Parcel;
import android.util.Log;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.ImageUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * The class that stores the data pertaining to a speaker who has contributed
 * to a recording.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Speaker implements Parcelable{

	/**
	 * The minimal constructor; do not use if the Speaker already has a UUID,
	 * as a new UUID will be generated.
	 */
	public Speaker() {
		setUUID(UUID.randomUUID());
		setLanguages(new ArrayList<Language>());
	}

	/**
	 * The constructor used when first creating a Speaker.
	 *
	 * Note that it doesn't include an ID argument, as that will be generated.
	 * For any tasks involving reading Speakers, use the constructor that takes
	 * an ID argument
	 *
	 * @param	uuid	The UUID of the speaker
	 * @param	name	The name of the speaker
	 * @param	languages	A list of languages of the speaker.
	 */
	public Speaker(UUID uuid, String name, List<Language> languages) {
		setUUID(uuid);
		setName(name);
		setId(createId(name));
		setLanguages(languages);
	}

	/**
	 * The constructor used when reading an existing speaker.
	 *
	 * @param	uuid	The UUID of the speaker
	 * @param	name	The name of the speaker
	 * @param	languages	A list of languages of the speaker.
	 * @param	id	The 8+ char string identifier of the speaker.
	 */
	public Speaker(UUID uuid, String name, List<Language> languages, String id) {
		setUUID(uuid);
		setName(name);
		setLanguages(languages);
		setId(id);
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
	 * @throws	IOException	If the image cannot be retrieved.
	 */
	public Bitmap getImage() throws IOException {
		return ImageUtils.getImage(getUUID());
	}

	/**
	 * Gets the small version of the Speaker's image.
	 *
	 * @return	A Bitmap object.
	 * @throws	IOException	If the image cannot be retrieved.
	 */
	public Bitmap getSmallImage() throws IOException {
		return ImageUtils.getSmallImage(getUUID());
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
		encodedSpeaker.put("id", this.id);
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
	 *
	 * @throws	IOException	If the speaker metadata cannot be written to file.
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
	 * @return	A Speaker object corresponding to the given speaker UUID.
	 * @throws	IOException	If the speaker metadata cannot be read from file.
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
		String id = (String) jsonObj.get("id");
		String name = (String) jsonObj.get("name");
		JSONArray languageArray = (JSONArray) jsonObj.get("languages");
		if (languageArray == null) {
			throw new IOException("Null languages in the JSON file.");
		}
		List<Language> languages = Language.decodeJSONArray(languageArray);
		return new Speaker(uuid, name, languages, id);
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
	 * @param	obj	The object to compare to.
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
	 * Provides a string representation of the speaker.
	 *
	 * @return	A string representation of the Speaker
	 */
	public String toString() {
		String s = getUUID().toString() + ", " + getName() + ", " +
				getLanguages().toString();
		return s;
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
	 * @throws	IllegalArgumentException	If the speaker UUID is null.
	 */
	private void setUUID(UUID uuid) throws IllegalArgumentException {
		if (uuid == null) {
			throw new IllegalArgumentException("Speaker UUID cannot be null.");
		}
		this.uuid = uuid;
	}

	private void setId(String id) {
		Log.i("setId", "set ID: " + id);
		this.id = id;
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
	 * @throws	IllegalArgumentException	If the language list is null
	 */
	private void setLanguages(List<Language> languages) throws
			IllegalArgumentException {
		if (languages == null) {
			throw new IllegalArgumentException("Speaker languages cannot be null.");
		}
		this.languages = languages;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Creates a Parcel object representing the Speaker.
	 *
	 * @param	out	The parcel to be written to
	 * @param	_flags	Unused additional flags about how the object should be
	 * written.
	 */
	public void writeToParcel(Parcel out, int _flags) {
		out.writeString(uuid.toString());
		out.writeString(name);
		out.writeTypedList(languages);
	}

	/**
	 * Generates instances of a Speaker from a parcel.
	 */
	public static final Parcelable.Creator<Speaker> CREATOR =
			new Parcelable.Creator<Speaker>() {
		public Speaker createFromParcel(Parcel in) {
			return new Speaker(in);
		}
		public Speaker[] newArray(int size) {
			return new Speaker[size];
		}
	};

	/**
	 * Constructor that takes a parcel representing the speaker.
	 *
	 * @param	in	The parcel representing the Speaker to be constructed.
	 */
	public Speaker(Parcel in) {
		setUUID(UUID.fromString(in.readString()));
		setName(in.readString());
		List<Language> languages = new ArrayList<Language>();
		in.readTypedList(languages, Language.CREATOR);
		setLanguages(languages);
	}

	private String createId(String name) {
		// Extract the initials of the name.
		String initials = extractInitials(name);

		// Generate random number of a specified number of digits (8 - number
		// of initials)
		int digitStringLength = 8 - initials.length();
		String randomDigits = randomDigitString(digitStringLength);

		return initials + randomDigits;
	}

	/**
	 * Creates a random digit string of length n
	 *
	 * @param	n	The number of digits long the string is to be.
	 */
	private String randomDigitString(int n) {
		Random rng = new Random();
		StringBuilder randomDigits = new StringBuilder();
		for (int i = 0; i < n; i++) {
			randomDigits.append(rng.nextInt(10));
		}
		return randomDigits.toString();
	}

	// Extracts the first character of each token in a string and uppercases, but
	// stops after 4 characters have been extracted.
	private String extractInitials(String name) {
		StringBuilder initials = new StringBuilder();
		int count = 0;
		for (String token : name.split("\\s+")) {
			if (token.length() > 0) {
				initials.append(Character.toUpperCase(token.charAt(0)));
				count += 1;
				if (count >= 4) {
					break;
				}
			}
		}
		Log.i("extractInitials", "Extracting initials of: " + name + ". " +
				"Returning " + initials.toString());
		return initials.toString();
	}

	/**
	 * The UUID of the Speaker.
	 */
	private UUID uuid;

	/**
	 * The ID of the Speaker.
	 */
	 private String id;

	/**
	 * The name of the Speaker.
	 */
	private String name;

	/**
	 * The languages of the Speaker.
	 */
	private List<Language> languages;

}
