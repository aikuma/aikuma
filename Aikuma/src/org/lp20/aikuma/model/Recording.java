package org.lp20.aikuma.model;

import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.StandardDateFormat;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

/**
 * The class that stores the metadata of a recording, including it's UUID,
 * creator's UUID, name, date, originalUUID (if applicable), and languages.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Recording {

	/**
	 * The minimal constructor
	 */
	public Recording() {
		setUUID(UUID.randomUUID());
		setDate(new Date());
		setLanguages(new ArrayList<Language>());
		setSpeakersUUIDs(new ArrayList<UUID>());
		setAndroidID(Aikuma.getAndroidID());
	}

	/**
	 * Constructs a new Recording using a specified UUID, name and date.
	 *
	 * @param	uuid	the recording's UUID.
	 * @param	name	The recording's name.
	 * @param	date	The date of creation.
	 */
	public Recording(UUID uuid, String name, Date date) {
		setUUID(uuid);
		setName(name);
		setDate(date);
		setLanguages(new ArrayList<Language>());
		setSpeakersUUIDs(new ArrayList<UUID>());
		setAndroidID(Aikuma.getAndroidID());
	}

	/**
	 * Constructs a new Recording using a specified UUID, name, date,
	 * languages, speakersUUIDs and android ID
	 *
	 * @param	uuid	the recording's UUID.
	 * @param	name	The recording's name.
	 * @param	date	The date of creation.
	 */
	public Recording(UUID uuid, String name, Date date,
			List<Language> languages, List<UUID> speakersUUIDs,
			String androidID) {
		setUUID(uuid);
		setName(name);
		setDate(date);
		setLanguages(languages);
		setSpeakersUUIDs(speakersUUIDs);
		setAndroidID(androidID);
	}

	/**
	 * Constructs a new Recording using a specified UUID, name, date,
	 * languages and UUID.
	 *
	 * @param	uuid	the recording's UUID.
	 * @param	name	The recording's name.
	 * @param	date	The date of creation.
	 */
	public Recording(UUID uuid, String name, Date date,
			List<Language> languages, List<UUID> speakersUUIDs,
			String androidID, UUID originalUUID) {
		setUUID(uuid);
		setName(name);
		setDate(date);
		setLanguages(languages);
		setSpeakersUUIDs(speakersUUIDs);
		setAndroidID(androidID);
		setOriginalUUID(originalUUID);
	}

	/**
	 * UUID accessor.
	 */
	public UUID getUUID() {
		return uuid;
	}

	/** Returns a File that refers to the actual recording file. */
	public File getFile() {
		return new File(getRecordingsPath(), getUUID() + ".wav");
	}

	/**
	 * Name accessor.
	 */
	public String getName() {
		if (name != null) {
			return name;
		} else {
			return "";
		}
	}

	/**
	 * Date accessor.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * languages accessor.
	 */
	public List<Language> getLanguages() {
		return languages;
	}

	/**
	 * speakersUUIDs accessor.
	 */
	public List<UUID> getSpeakersUUIDs() {
		return speakersUUIDs;
	}

	/**
	 * Returns true if the Recording has at least one language; false otherwise.
	 *
	 * @return	true if the Recording has at least one language; false otherwise.
	 */
	public boolean hasALanguage() {
		if (this.languages.size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * androidID accessor
	 */
	public String getAndroidID() {
		return androidID;
	}

	/**
	 * originalUUID accessor.
	 */
	public UUID getOriginalUUID() throws Exception {
		if (originalUUID == null) {
			throw new Exception(
					"Cannot call getOriginalUUID when originalUUID is null." + 
					" Call isOriginal().");
		}
		return originalUUID;
	}

	/**
	 * Returns true if the Recording is an original; false if respeaking
	 *
	 * @return	True if the recording is an original.
	 */
	public boolean isOriginal() {
		if (originalUUID == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Encode the Recording as a corresponding JSONObject.
	 *
	 * @return	A JSONObject instance representing the Recording;
	 */
	public JSONObject encode() {
		JSONObject encodedRecording = new JSONObject();
		encodedRecording.put("uuid", this.uuid.toString());
		encodedRecording.put("name", this.name);
		encodedRecording.put("date", new StandardDateFormat().format(this.date));
		encodedRecording.put("languages", Language.encodeList(languages));
		JSONArray speakersUUIDsArray = new JSONArray();
		for (UUID uuid : speakersUUIDs) {
			speakersUUIDsArray.add(uuid.toString());
		}
		encodedRecording.put("speakersUUIDs", speakersUUIDsArray);
		encodedRecording.put("androidID", this.androidID);
		if (this.originalUUID == null) {
			encodedRecording.put("originalUUID", null);
		} else {
			encodedRecording.put("originalUUID", this.originalUUID.toString());
		}
		return encodedRecording;
	}

	/**
	 * Write the Recording to file in a subdirectory of the recordings
	 * directory named as <uuid>.json
	 */
	public void write() throws IOException {
		JSONObject encodedRecording = this.encode();

		FileIO.writeJSONObject(new File(
				getRecordingsPath(), this.getUUID().toString() + "/metadata.json"),
				encodedRecording);
	}

	/**
	 * Read a recording from the file containing JSON describing the Recording
	 *
	 * @param	uuid	The uuid of the recording to be read.
	 */
	public static Recording read(UUID uuid) throws IOException {
		JSONObject jsonObj = FileIO.readJSONObject(
				new File(getRecordingsPath(), uuid.toString() + "/metadata.json"));
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
		String dateString = (String) jsonObj.get("date");
		if (dateString == null) {
			throw new IOException("Null date in the JSON file.");
		}
		Date date;
		try {
			date = new StandardDateFormat().parse(dateString);
		} catch (ParseException e) {
			throw new IOException(e);
		}
		JSONArray languageArray = (JSONArray) jsonObj.get("languages");
		if (languageArray == null) {
			throw new IOException("Null languages in the JSON file.");
		}
		List<Language> languages = Language.decodeJSONArray(languageArray);
		JSONArray speakerUUIDArray = (JSONArray) jsonObj.get("speakersUUIDs");
		if (speakerUUIDArray == null) {
			throw new IOException("Null speakersUUIDs in the JSON file.");
		}
		List<UUID> speakersUUIDs = Speaker.decodeJSONArray(speakerUUIDArray);
		String androidID = (String) jsonObj.get("androidID");
		if (androidID == null) {
			throw new IOException("Null androidID in the JSON file.");
		}
		UUID originalUUID;

		if (jsonObj.get("originalUUID") == null) {
			originalUUID = null;
		} else {
			originalUUID = UUID.fromString((String) jsonObj.get("originalUUID"));
		}
		Recording recording = new Recording(
				uuid, name, date, languages, speakersUUIDs, androidID, originalUUID);
		return recording;
	}

	/**
	 * Read all recordings from file
	 *
	 * @return	A list of the users found in the users directory.
	 */
	public static List<Recording> readAll() {
		// Get a list of all the UUIDs of users in the "recordings" directory.
		List<String> recordingUUIDs =
				Arrays.asList(getRecordingsPath().list());

		// Get the recordings data from the metadata.json files.
		List<Recording> recordings = new ArrayList<Recording>();
		for (String recordingUUID : recordingUUIDs) {
			try {
				recordings.add(Recording.read(UUID.fromString(recordingUUID)));
			} catch (IOException e) {
				// Couldn't read that recording for whateve rreason (perhaps
				// json file wasn't formatted correctly). Let's just ignore
				// that user.
			}
		}
		return recordings;
	}

	/**
	 * Compares the given object with the Recording, and returns true if the
	 * Recordings uuid, name, date, languages, androidID and originalUUID are
	 * equal
	 *
	 * @return	true if the uuid, name, date, languages, androidID and
	 * originalUUID are equal; false otherwise.
	 */
	 public boolean equals(Object obj) {
	 	if (obj == null) {return false;}
		if (obj == this) {return true;}
		if (obj.getClass() != getClass()) {return false;}
		Recording rhs = (Recording) obj;
		return new EqualsBuilder()
				.append(uuid, rhs.uuid)
				.append(name, rhs.name)
				.append(date, rhs.date)
				.append(languages, rhs.languages)
				.append(speakersUUIDs, rhs.speakersUUIDs)
				.append(androidID, rhs.androidID)
				.append(originalUUID, rhs.originalUUID)
				.isEquals();
	 }


	/**
	 * UUID mutator.
	 */
	private void setUUID(UUID uuid) {
		if (uuid == null) {
			throw new IllegalArgumentException(
					"Recording UUID cannot be null.");
		}
		this.uuid = uuid;
	}

	/**
	 * Name mutator.
	 */
	private void setName(String name) {
		this.name = name;
	}

	/**
	 * Date mutator.
	 */
	private void setDate(Date date) {
		if (date == null) {
			throw new IllegalArgumentException(
					"Recording date cannot be null.");
		}
		this.date = date;
	}

	/**
	 * languages mutator.
	 */
	private void setLanguages(List<Language> languages) {
		if (languages == null) {
			throw new IllegalArgumentException(
					"Recording languages cannot be null. " +
					"Set as an empty List<Language> instead.");
		}
		this.languages = languages;
	}

	/**
	 * Add's another language to the Recording's language list
	 *
	 * @param	language	The language to be added to the Recording's list of
	 * languages.
	 */
	private void addLanguage(Language language) {
		if (language == null) {
			throw new IllegalArgumentException(
					"A language for the recording cannot be null");
		}
		this.languages.add(language);
	}

	/**
	 * Speakers mutator.
	 */
	private void setSpeakersUUIDs(List<UUID> speakersUUIDs) {
		if (speakersUUIDs == null) {
			throw new IllegalArgumentException(
					"Recording speakersUUIDs cannot be null. " +
					"Set as an empty List<UUID> instead.");
		}
		this.speakersUUIDs = speakersUUIDs;
	}

	/**
	 * Add's another speaker to the Recording's speaker list
	 *
	 * @param	speaker	The speaker to be added to the Recording's list of
	 * speaker.
	 */
	private void addSpeakerUUID(Speaker speaker) {
		if (speaker == null) {
			throw new IllegalArgumentException(
					"A speaker for the recording cannot be null");
		}
		this.speakersUUIDs.add(speaker.getUUID());
	}

	/**
	 * androidID mutator
	 */
	private void setAndroidID(String androidID) {
		if (androidID == null) {
			throw new IllegalArgumentException(
					"The androidID for the recording cannot be null");
		}
		this.androidID = androidID;
	}

	/**
	 * originalUUID mutator.
	 */
	private void setOriginalUUID(UUID originalUUID) {
		this.originalUUID = originalUUID;
	}

	/**
	 * Get the applications recordings directory
	 *
	 * @return	A File representing the path of the recordings directory
	 */
	private static File getRecordingsPath() {
		File path = new File(FileIO.getAppRootPath(), "recordings");
		path.mkdirs();
		return path;
	}


	/**
	 * The recording's UUID.
	 */
	private UUID uuid;

	/**
	 * The recording's name.
	 */
	private String name;

	/**
	 * The recording's date.
	 */
	private Date date;

	/**
	 * The languages of the recording.
	 */
	private List<Language> languages;

	/**
	 * The speakers of the recording.
	 */
	private List<UUID> speakersUUIDs;

	/**
	 * The Android ID of the device that the recording was made on.
	 */
	private String androidID;

	/**
	 * The UUID of the original of the recording if it is a respeaking.
	 */
	private UUID originalUUID;

}
