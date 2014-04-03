/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.model;

import android.util.Log;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.IdUtils;
import org.lp20.aikuma.util.StandardDateFormat;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import static junit.framework.Assert.assertTrue;

/**
 * The class that stores the metadata of a recording, including it's ID,
 * creator's ID, name, date, originalID, and languages.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Recording {

	/**
	 * The constructor used when first creating a Recording.
	 *
	 * @param	wavUUID	the temporary UUID of the WAV in question.
	 * @param	name	The recording's name.
	 * @param	date	The date of creation.
	 * @param	languages	The languages associated with the recording
	 * @param	speakersIds	The IDs of the speakers associated with the
	 * recording
	 * @param	androidID	The android ID of the device that created the
	 * recording
	 * @param	originalId	The ID of the recording that this recording
	 * is a respeaking of
	 * @param	sampleRate	The sample rate of the recording.
	 * @param	durationMsec	The duration of the recording in milliseconds.
	 */
	public Recording(UUID wavUUID, String name, Date date,
			List<Language> languages, List<String> speakersIds,
			String androidID, String originalId, long sampleRate,
			int durationMsec) {
		this.wavUUID = wavUUID;
		setName(name);
		setDate(date);
		setLanguages(languages);
		setSpeakersIds(speakersIds);
		setAndroidID(androidID);
		setSampleRate(sampleRate);
		setDurationMsec(durationMsec);
		setOriginalId(originalId);
		// If there isn't an original Id, ie this is an original
		if (originalId == null) {
			setOriginalId(createOriginalId());
		} else {
			// Then we must generate the 4 digit respeaking ID.
			setRespeakingId(IdUtils.randomDigitString(4));
		}
		setFilenamePrefix(determineFilenamePrefix());
	}

	/**
	 * The constructor used when reading in an existing Recording.
	 *
	 * @param	name	The recording's name.
	 * @param	date	The date of creation.
	 * @param	languages	The languages associated with the recording
	 * @param	speakersIds	The IDs of the speakers associated with the
	 * recording
	 * @param	androidID	The android ID of the device that created the
	 * recording
	 * @param	originalId	The ID of the recording that this recording
	 * is a respeaking of
	 * @param	respeakingId	The ID of the recording that this recording
	 * is a respeaking of
	 * @param	sampleRate	The sample rate of the recording.
	 * @param	durationMsec	The duration of the recording in milliseconds.
	 */
	public Recording(String name, Date date,
			List<Language> languages, List<String> speakersIds,
			String androidID, String originalId, String respeakingId,
			long sampleRate, int durationMsec) {
		setName(name);
		setDate(date);
		setLanguages(languages);
		setSpeakersIds(speakersIds);
		setAndroidID(androidID);
		setSampleRate(sampleRate);
		setDurationMsec(durationMsec);
		setOriginalId(originalId);
		setRespeakingId(respeakingId);
		setFilenamePrefix(determineFilenamePrefix());
	}

	private String determineFilenamePrefix() {
		// Build up the filename prefix
		StringBuilder filenamePrefix = new StringBuilder();
		filenamePrefix.append(getOriginalId());
		filenamePrefix.append("-");
		filenamePrefix.append(getSpeakersIds().get(0));
		filenamePrefix.append("-");
		if (isOriginal()) {
			filenamePrefix.append("source");
		} else {
			filenamePrefix.append("respeaking-");
			filenamePrefix.append(respeakingId);
		}
		return filenamePrefix.toString();
	}

	/**
	 * Returns true if the Recording is an original; false if respeaking
	 *
	 * @return	True if the recording is an original.
	 */
	public boolean isOriginal() {
		return respeakingId.length() > 0;
	}

	// Moves a WAV file with a temporary UUID from a no-sync directory to
	// its rightful place in the connected world of Aikuma, with a proper name
	// and where it will find it's best friend - a JSON metadata file.
	private void importWav(UUID wavUUID, String filenamePrefix)
			throws IOException {
		File wavFile = new File(getNoSyncRecordingsPath(),
				wavUUID + ".wav");
		FileUtils.moveFile(sourceFile,
				new File(getRecordingsPath(), getOriginalId() + "/" +
						filenamePrefix + ".wav"));
	}

	// Create an original ID (the prefix for recordings)
	private String createOriginalId() {
		return IdUtils.sampleFromAlphabet(8, "abcdefghijklmnopqrstuvwxyz");
	}

	/**
	 * Returns a File that refers to the actual recording file.
	 *
	 * @return	The file the recording is stored in.
	 */
	public File getFile() {
		return new File(getRecordingsPath(), getOriginalId() + "/"
				+ filenamePrefix + ".wav");
	}

	/**
	 * Name accessor; returns an empty string if the name is null
	 *
	 * @return	The name of the recording.
	 */
	public String getName() {
		if (name != null) {
			return name;
		} else {
			return "";
		}
	}

	public Date getDate() {
		return date;
	}

	public List<Language> getLanguages() {
		return languages;
	}

	/**
	 * Returns the first language code as a string, or an empty string if there
	 * is none.
	 *
	 * @return	The language code of the first language associated with the
	 * recording.
	 */
	public String getFirstLangCode() {
		if (getLanguages().size() > 0) {
			return getLanguages().get(0).getCode();
		} else {
			return "";
		}
	}

	/**
	 * Returns the name and language of the recording in a single string.
	 *
	 * @return	The name and langugage of the recording in a string.
	 */
	public String getNameAndLang() {
		if (getFirstLangCode().equals("")) {
			return getName();
		} else {
			return getName() + " (" + getFirstLangCode() + ")";
		}
	}

	/**
	 * speakersIds accessor.
	 *
	 * @return	A list of IDs representing the speakers of the recording.
	 */
	public List<String> getSpeakersIds() {
		return speakersIds;
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
	 *
	 * @return	The Andorid of the device that made the recording.
	 */
	public String getAndroidID() {
		return androidID;
	}

	/**
	 * originalId accessor.
	 *
	 * @return	The Id of the original recording that this is a respeaking
	 * of.
	 */
	public String getOriginalId() {
		return originalId;
	}

	public String getFilenamePrefix() {
		return filenamePrefix;
	}

	/**
	 * sampleRate accessor
	 *
	 * @return	The sample rate of the recording as a long.
	 */
	public long getSampleRate() {
		return sampleRate;
	}

	/**
	 * durationMsec accessor
	 *
	 * @return	The duration of the recording in milliseconds as an int.
	 */
	public int getDurationMsec() {
		return durationMsec;
	}

	/**
	 * Returns true if the Recording is an original; false if respeaking
	 *
	 * @return	True if the recording is an original.
	 */

	/**
	 * Encode the Recording as a corresponding JSONObject.
	 *
	 * @return	A JSONObject instance representing the Recording;
	 */
	public JSONObject encode() {
		JSONObject encodedRecording = new JSONObject();
		encodedRecording.put("name", this.name);
		encodedRecording.put("date", new StandardDateFormat().format(this.date));
		encodedRecording.put("languages", Language.encodeList(languages));
		JSONArray speakersIdsArray = new JSONArray();
		for (String id : speakersIds) {
			speakersIdsArray.add(id.toString());
		}
		encodedRecording.put("speakersIds", speakersIdsArray);
		encodedRecording.put("androidID", this.androidID);
		encodedRecording.put("sampleRate", getSampleRate());
		encodedRecording.put("durationMsec", getDurationMsec());
		encodedRecording.put("originalId", this.originalUUID);
		encodedRecording.put("respeakingId", this.respeakingId);
		return encodedRecording;
	}

	/**
	 * Write the Recording to file in a subdirectory of the recordings and move
	 * the recording WAV data to that directory
	 *
	 * @throws	IOException	If the recording metadata could not be written.
	 */
	public void write() throws IOException {
		// Ensure the directory exists
		File dir = new File(getRecordingsPath(), getOriginalId());
		dir.mkdir();


		// Import the wave file into the new recording directory.
		importWav(wavUUID, filenamePrefix.toString());

		JSONObject encodedRecording = this.encode();


		// Write the json metadata.
		FileIO.writeJSONObject(new File(
				getRecordingsPath(), getOriginalId() + "/" +
						filenamePrefix + ".json"),
				encodedRecording);
	}

	/**
	 * Deletes the JSON File associated with the recording.
	 *
	 * @return	true if successful; false otherwise.
	 */
	 /*
	public boolean delete() {
		File file = new File(getRecordingsPath(), this.getUUID().toString() +
				".json");
		if (!isOriginal()) {
			File mapFile = new File(getRecordingsPath(),
					this.getUUID().toString() + ".map");
			boolean result;
			result = mapFile.delete();
			if (!result) {
				return false;
			}
		}
		return file.delete();
	}
	*/

	/**
	 * Returns this recordings original.
	 *
	 ' @return	The original recording
	 */
	public Recording getOriginal() {
		File originalDir = new File(getRecordingsPath(), getOriginalId());

		// Filter for files that are source metadata
		File[] originalMetadataFileArray = f.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				String[] splitFilename = filename.split("[-.]");
				if (splitFilename[2].equals("source") &&
					splitFilename[3].equals("metadata") &&
					filename.endsWith(".json")) {
					return true;
				}
			}
		});

		assertTrue(originalMetadataFileArray.size == 1);

		return Recording.read(originalMetadataFileArray[0]);
	}

	/**
	 * Read a recording from the file containing JSON describing the Recording
	 *
	 * @param	file	The file containing the metadata of the recording.
	 * @return	A Recording object corresponding to the json file.
	 * @throws	IOException	If the recording metadata cannot be read.
	 */
	public static Recording read(File file) throws IOException {
		JSONObject jsonObj = FileIO.readJSONObject(file);
		String originalIdString = (String) jsonObj.get("originalId");
		if (originalIdString == null) {
			throw new IOException("Null originalId in the JSON file.");
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
		JSONArray speakerIdArray = (JSONArray) jsonObj.get("speakersIds");
		if (speakerIdArray == null) {
			throw new IOException("Null speakersIds in the JSON file.");
		}
		List<String> speakersIds = Speaker.decodeJSONArray(speakerIdArray);
		String androidID = (String) jsonObj.get("androidID");
		if (androidID == null) {
			throw new IOException("Null androidID in the JSON file.");
		}
		String respeakingId = (String) jsonObj.get("respeakingId");
		if (respeakingId == null) {
			throw new IOException("Null respeakingId in the JSON file.");
		}

		long sampleRate;
		if (jsonObj.get("sampleRate") == null) {
			sampleRate = -1;
		} else {
			sampleRate = (Long) jsonObj.get("sampleRate");
		}

		int durationMsec;
		if (jsonObj.get("durationMsec") == null) {
			durationMsec = -1;
			Log.i("duration", "reading: null");
		} else {
			durationMsec = ((Long) jsonObj.get("durationMsec")).intValue();
			Log.i("duration", "reading: " + durationMsec);
		}
		Recording recording = new Recording(name, date, languages, speakersIds,
				androidID, originalId, respeakingId, sampleRate, (Integer)
				durationMsec);
		return recording;
	}

	/**
	 * Returns a list of all the respeakings of this Recording.
	 *
	 * @return	A list of all the respeakings of the recording.
	 */
	public List<Recording> getRespeakings() {
		List<Recording> allRecordings = readAll();
		List<Recording> respeakings = new ArrayList();
		for (Recording recording : allRecordings) {
			if (!recording.isOriginal()) {
				if (recording.getOriginalUUID().equals(getUUID())) {
					respeakings.add(recording);
				}
			}
		}
		return respeakings;
	}

	/**
	 * Read all recordings from file
	 *
	 * @return	A list of all the recordings in the Aikuma directory.
	 */
	public static List<Recording> readAll() {

		List<Recording> recordings = new ArrayList<Recording>();

		// Constructs a list of directories in the recordings directory.
		File[] recordingPathFiles = getRecordingsPath.listFiles();
		for (File f : recordingPathFiles) {
			if (f.isDirectory()) {
				// For each of those subdirectories, creates a list of files
				// within that end in .json
				File[] originalIdDirFiles = f.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String filename) {
						return filename.endsWith(".json");
					}
				});

				// Iterate over those recording metadata files and add the
				// recordings they refer to to the recordings list
				for (File jsonFile : originalIdDirFiles) {
					try {
						recordings.add(Recording.read(jsonFile));
					} catch (IOException e) {
						// Couldn't read that recording for whateve rreason
						// (perhaps json file wasn't formatted correctly).
						// Let's just ignore that user.
					}
				}
			}
		}

		return recordings;
	}

	/**
	 * Compares the given object with the Recording, and returns true if the
	 * Recording's name, date, languages, androidID, originalId and
	 * respeakingId are equal
	 *
	 * @param	obj	The object to be compared.
	 * @return	true most fields are the same; false otherwise
	 */
	 public boolean equals(Object obj) {
	 	if (obj == null) {return false;}
		if (obj == this) {return true;}
		if (obj.getClass() != getClass()) {return false;}
		Recording rhs = (Recording) obj;
		return new EqualsBuilder()
				.append(name, rhs.name)
				.append(date, rhs.date)
				.append(languages, rhs.languages)
				.append(speakersIds, rhs.speakersIds)
				.append(androidID, rhs.androidID)
				.append(originalId, rhs.originalId)
				.append(respeakingId, rhs.respeakingId)
				.append(sampleRate, rhs.sampleRate)
				.isEquals();
	 }

	/**
	 * Name mutator.
	 */
	private void setName(String name) {
		this.name = name;
	}

	// Sets the date; the date cannot be null.
	private void setDate(Date date) {
		if (date == null) {
			throw new IllegalArgumentException(
					"Recording date cannot be null.");
		}
		this.date = date;
	}

	// Sets the languages 
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

	// Sets the speakers Ids, but won't accept a null list (empty lists are
	// fine).
	private void setSpeakersIds(List<String> speakersIds) {
		if (speakersIds == null) {
			throw new IllegalArgumentException(
					"Recording speakersIds cannot be null. " +
					"Set as an empty List<String> instead.");
		}
		this.speakersIds = speakersIds;
	}

	/**
	 * Add's another speaker to the Recording's speaker list
	 *
	 * @param	speaker	The speaker to be added to the Recording's list of
	 * speaker.
	 */
	private void addSpeakerId(Speaker speaker) {
		if (speaker == null) {
			throw new IllegalArgumentException(
					"A speaker for the recording cannot be null");
		}
		this.speakersIds.add(speaker.getId());
	}

	// Sets the android ID but won't accept a null string.
	private void setAndroidID(String androidID) {
		if (androidID == null) {
			throw new IllegalArgumentException(
					"The androidID for the recording cannot be null");
		}
		this.androidID = androidID;
	}

	private void setOriginalId(String originalId) {
		this.originalId = originalId;
	}

	private void setFilenamePrefix(String filenamePrefix) {
		this.filenamePrefix = filenamePrefix;
	}

	private void setSampleRate(long sampleRate) {
		this.sampleRate = sampleRate;
	}

	private void setRespeakingId(String respeakingId) {
		this.respeakingId = respeakingId;
	}

	private void setDurationMsec(int durationMsec) {
		this.durationMsec = durationMsec;
	}

	/**
	 * Get the applications recordings directory
	 *
	 * @return	A File representing the path of the recordings directory
	 */
	public static File getRecordingsPath() {
		File path = new File(FileIO.getAppRootPath(), "recordings");
		path.mkdirs();
		return path;
	}

	/**
	 * Get the applications recording directory that isn't synced.
	 *
	 * @return	A File representing the path of the recordings directory in the
	 * no-sync Aikuma directory.
	 */
	public static File getNoSyncRecordingsPath() {
		File path = new File(FileIO.getNoSyncPath(), "recordings");
		path.mkdirs();
		return path;
	}

	/**
	 * Indicates that this recording is allowed to be synced by moving it to a
	 * directory that the SyncUtil synchronizes.
	 *
	 * @param	id	The ID of the recording to sync.
	 * @throws	IOException	If it cannot be moved to the synced directory.
	 */
	/*
	public static void enableSync(String id) throws IOException {
		File wavFile = new File(getNoSyncRecordingsPath(), uuid + ".wav");
		FileUtils.moveFileToDirectory(wavFile, getRecordingsPath(), false);
	}
	*/

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
	private List<String> speakersIds;

	/**
	 * The Android ID of the device that the recording was made on.
	 */
	private String androidID;

	/**
	 * The UUID of the original of the recording if it is a respeaking.
	 */
	private UUID originalUUID;

	/**
	 * The sample rate of the recording in Hz
	 */
	private long sampleRate;

	/**
	 * The duration of the recording in seconds (floored)
	 */
	private int durationMsec;

	// The UUID of the source WAV.
	private UUID wavUUID;

	// The respeaking ID that is at the end of the filename prefix.
	private String respeakingUUID;

	// The filename prefix that comes before .wav, .json etc.
	private String filenamePrefix;

}
