package au.edu.unimelb.aikuma.model;

import au.edu.unimelb.aikuma.Aikuma;
import au.edu.unimelb.aikuma.util.FileIO;
import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The class that stores the metadata of a recording, including it's UUID,
 * creator's UUID, name, date, originalUUID (if applicable), and languages.
 */
public class Recording {

	/**
	 * UUID accessor.
	 */
	public UUID getUUID() {
		return uuid;
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
	 * The minimal constructor
	 */
	public Recording() {
		setUUID(UUID.randomUUID());
		setDate(new Date());
		setLanguages(new ArrayList<Language>());
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
		setAndroidID(Aikuma.getAndroidID());
	}

	/**
	 * Constructs a new Recording using a specified UUID, name, date,
	 * languages and android ID
	 *
	 * @param	uuid	the recording's UUID.
	 * @param	name	The recording's name.
	 * @param	date	The date of creation.
	 */
	public Recording(UUID uuid, String name, Date date,
			List<Language> languages, String androidID) {
		setUUID(uuid);
		setName(name);
		setDate(date);
		setLanguages(languages);
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
			List<Language> languages, String androidID, UUID originalUUID) {
		setUUID(uuid);
		setName(name);
		setDate(date);
		setLanguages(languages);
		setAndroidID(androidID);
		setOriginalUUID(originalUUID);
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
	 * The Android ID of the device that the recording was made on.
	 */
	private String androidID;

	/**
	 * The UUID of the original of the recording if it is a respeaking.
	 */
	private UUID originalUUID;

}
