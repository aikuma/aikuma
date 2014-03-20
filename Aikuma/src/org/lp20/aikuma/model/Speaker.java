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
	 * The constructor used when first creating a Speaker.
	 *
	 * Note that it doesn't include an ID argument, as that will be generated.
	 * For any tasks involving reading Speakers, use the constructor that takes
	 * an ID argument
	 *
	 * @param	imageUUID	The UUID used to identify the temporary name of the
	 * image files
	 * @param	name	The name of the speaker
	 * @param	languages	A list of languages of the speaker.
	 */
	public Speaker(UUID imageUUID, String name, List<Language> languages) {
		setName(name);
		setId(createId(name));
		setLanguages(languages);

		// Move the image to the Speaker directory with an appropriate name
		importImage(imageUUID);

	}

	private importImage(imageUUID) {
	}

	/**
	 * The constructor used when reading an existing speaker.
	 *
	 * @param	name	The name of the speaker
	 * @param	languages	A list of languages of the speaker.
	 * @param	id	The 8+ char string identifier of the speaker.
	 */
	public Speaker(String name, List<Language> languages, String id) {
		setName(name);
		setLanguages(languages);
		setId(id);
	}

	public String getId() {
		return this.id;
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
		encodedSpeaker.put("id", this.id);
		encodedSpeaker.put("languages", Language.encodeList(languages));
		return encodedSpeaker;
	}

	/**
	 * Encodes a list of speakers as a corresponding JSONArray object of their
	 * IDs.
	 *
	 * @param	speakers	A list of speakers to be encoded
	 * @return	A JSONArray object.
	 */
	public static JSONArray encodeList(List<Speaker> speakers) {
		JSONArray speakerArray = new JSONArray();
		for (Speaker speaker : speakers) {
			speakerArray.add(speaker.getId());
		}
		return speakerArray;
	}

	/**
	 * Decodes a list of speakers from a JSONArray
	 *
	 * @param	speakerArray	A JSONArray object containing the speakers.
	 * @return	A list of the speakers in the JSONArray
	 */
	public static List<String> decodeJSONArray(JSONArray speakerArray) {
		List<String> speakerIDs = new ArrayList<String>();
		if (speakerArray != null) {
			for (Object speakerObj : speakerArray) {
				String speakerID = (String) speakerObj;
				speakerIDs.add(speakerID);
			}
		}
		return speakerIDs;
	}

	/**
	 * Write the Speaker to file in a subdirectory of the Speakers directory
	 *
	 * @throws	IOException	If the speaker metadata cannot be written to file.
	 */
	public void write() throws IOException {
		JSONObject encodedSpeaker = this.encode();

		FileIO.writeJSONObject(new File(getSpeakersPath(), getId() + 
				"/metadata.json"), encodedSpeaker);
	}

	/**
	 * Read a Speaker from the file containing the JSON describing the speaker
	 *
	 * @param	id	The ID of the speaker.
	 * @return	A Speaker object corresponding to the given speaker ID.
	 * @throws	IOException	If the speaker metadata cannot be read from file.
	 */
	public static Speaker read(String id) throws IOException {
		JSONObject jsonObj = FileIO.readJSONObject(
				new File(getSpeakersPath(), id + "/" + id + "-metadata.json"));
		String name = (String) jsonObj.get("name");
		JSONArray languageArray = (JSONArray) jsonObj.get("languages");
		if (languageArray == null) {
			throw new IOException("Null languages in the JSON file.");
		}
		List<Language> languages = Language.decodeJSONArray(languageArray);
		return new Speaker(name, languages, id);
	}

	/**
	 * Read all users from file
	 *
	 * @return	A list of the users found in the users directory.
	 */
	public static List<Speaker> readAll() {
		// Get a list of all the IDs of users in the "users" directory.
		List<String> speakerIDs = Arrays.asList(getSpeakersPath().list());

		// Get the user data from the metadata.json files.
		List<Speaker> speakers = new ArrayList<Speaker>();
		for (String speakerID : speakerIDs) {
			try {
				speakers.add(Speaker.read(speakerID));
			} catch (IOException e) {
				// Couldn't read that user for whatever reason (perhaps JSON
				// file wasn't formatted correctly). Lets just ignore that user.
			}
		}
		return speakers;
	}

	/**
	 * Compares the given object with the Speaker, and returns true if the
	 * Speaker ID, name and languages are equal.
	 *
	 * @param	obj	The object to compare to.
	 * @return	true if the ID, name and languages of the Speaker are equal;
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
				.append(id, rhs.id).append(name, rhs.name)
				.append(languages, rhs.languages).isEquals();
	}

	/**
	 * Provides a string representation of the speaker.
	 *
	 * @return	A string representation of the Speaker
	 */
	public String toString() {
		String s = getId().toString() + ", " + getName() + ", " +
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

	private void setId(String id) {
		Log.i("setId", "set Id: " + id);
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
		out.writeString(id.toString());
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
		setId(in.readString());
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
