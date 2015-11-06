/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.model;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Parcel;
import android.util.Log;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.IdUtils;
import org.lp20.aikuma.util.ImageUtils;
import org.lp20.aikuma.util.StandardDateFormat;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import static junit.framework.Assert.assertTrue;

/**
 * The class that stores the metadata of a recording, including it's ID,
 * creator's ID, name, date, group ID, and languages.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Recording extends FileModel {

	// String tag for debugging
	private static final String TAG = "Recording";
	
	/** sample file duration in sec */
	public static final long SAMPLE_SEC = 15;
	
	/**
	 * The constructor used when first creating a Recording.
	 *
	 * @param	recordingUUID	the temporary UUID of the recording in question.
	 * 							(recording can be wav or movie(mp4))
	 * @param	imageUUID		the temporary UUID of the recording-image
	 * @param	name	The recording's name.
	 * @param	comments	The optional free text string
	 * @param	date	The date of creation.
	 * @param	versionName	The recording's version(v0x)
	 * @param	ownerId	The recording owner's ID(Google account)
	 * @param	languages	The languages associated with the recording (used to determine fileType)
	 * @param	deviceName	The model name of the device
	 * @param	androidID	The android ID of the device that created the
	 * recording
	 * @param	groupId	The ID of the group of recordings this recording
	 * belongs in (Some source recording and respeakings/commentaries)
	 * @param	sourceVerId	The version-ID of the source recording of this recording
	 * @param	sampleRate	The sample rate of the recording.
	 * @param	durationMsec	The duration of the recording in milliseconds.
	 * @param	format	The mime type
	 * @param	bitsPerSample	The bits per sample of the audio
	 * @param	numChannels	The number of channels of the audio
	 * @param	latitude The location data
	 * @param	longitude The location data
	 */
	public Recording(UUID recordingUUID, UUID imageUUID, String name, String comments, Date date,
			String versionName, String ownerId,
			List<Language> languages,
			String deviceName, String androidID, String groupId, String sourceVerId,
			long sampleRate, int durationMsec, String format, int numChannels, 
			int bitsPerSample, Double latitude, Double longitude) {
		super(versionName, ownerId, null, null, format);	// id, fileType is defined by Recording class
		this.recordingUUID = recordingUUID;
		this.imageUUID = imageUUID;
		setName(name);
		setComments(comments);
		setDate(date);
		setDeviceName(deviceName);
		setAndroidID(androidID);
		setSampleRate(sampleRate);
		setDurationMsec(durationMsec);
		setGroupId(groupId);
		this.sourceVerId = sourceVerId;
		this.numChannels = numChannels;
		this.bitsPerSample = bitsPerSample;
		this.latitude = latitude;
		this.longitude = longitude;
		// If there isn't an group Id, ie this is an original
		if (groupId == null) {
			setGroupId(createGroupId());
			setRespeakingId("");
		} else {
			// Then we must generate the 4 digit respeaking ID.
			setRespeakingId(IdUtils.randomDigitString(6));
		}
		setFileType(sourceVerId, languages);
		setId(determineId(fileType));
	}

	/**
	 * The constructor used when reading in an existing Recording.
	 *
	 * @param	name	The recording's name.
	 * @param	date	The date of creation.
	 * @param	versionName	The recording's version(v0x)
	 * @param	ownerId	The recording owner's ID(Google account)
	 * @param	sourceVerId	The source's version and Id (if not respeaking, null)
	 * @param	format		The file format
	 * @param	fileType	The file type
	 * @param	androidID	The android ID of the device that created the
	 * recording
	 * @param	groupId	The ID of the group of recordings this recording
	 * belongs in (Some source recording and respeakings/commentaries)
	 * @param	respeakingId	The ID of the recording that this recording
	 * is a respeaking of
	 * @param	sampleRate	The sample rate of the recording.
	 * @param	durationMsec	The duration of the recording in milliseconds.
	 */
	public Recording(String name, Date date,
			String versionName, String ownerId, String sourceVerId,
			String androidID, String groupId, String respeakingId,
			long sampleRate, int durationMsec, String format, String fileType) {
		super(versionName, ownerId, null, null, format);	// id, fileType is defined by Recording class
		setName(name);
		setDate(date);
		setAndroidID(androidID);
		setSampleRate(sampleRate);
		setDurationMsec(durationMsec);
		setGroupId(groupId);
		setRespeakingId(respeakingId);
		this.sourceVerId = sourceVerId;
		this.fileType = fileType;
		setId(determineId(fileType));
	}

	private String determineId(String fileType) {
		// Build up the filename prefix
		StringBuilder id = new StringBuilder();
		id.append(getGroupId());
		id.append("-");
		id.append(getOwnerId());
		id.append("-");
		if (isOriginal()) {
			id.append(SOURCE_TYPE);
		} else {
			id.append(fileType);
			id.append("-");
			id.append(respeakingId);
		}
		return id.toString();
	}

	/**
	 * Returns true if the Recording is an original; false if respeaking
	 *
	 * @return	True if the recording is an original.
	 */
	public boolean isOriginal() {
		return respeakingId == null || respeakingId.length() == 0;
	}

	// Moves a WAV file with a temporary UUID from a no-sync directory to
	// its rightful place in the connected world of Aikuma, with a proper name
	// and where it will find it's best friend - a JSON metadata file.
	private void importWav(UUID wavUUID, String id) throws IOException {
		importWav(wavUUID + "." + AUDIO_EXT, id + "." + AUDIO_EXT);
	}
	
	private void importWav(String wavUUIDExt, String idExt)
			throws IOException {
		File wavFile = new File(getNoSyncRecordingsPath(), wavUUIDExt);
		Log.i(TAG, "importwav: " + wavFile.length());
		FileUtils.moveFile(wavFile, this.getFile(idExt));
		Log.i(TAG, wavFile.getAbsolutePath() + " move to " + this.getFile(idExt).getAbsolutePath());
	}
	
	// Moves a video File from a no-sync directory to its rightful place
	private void importMov(UUID videoUUID, String id) 
			throws IOException {
		File movFile = new File(getNoSyncRecordingsPath(), videoUUID + "." + VIDEO_EXT);
		FileUtils.moveFile(movFile, this.getFile());
		Log.i(TAG, movFile.getAbsolutePath() + " move to " + this.getFile().getAbsolutePath());
	}
	
	// Moves a recording-image file from a no-sync directory to its rightful place
	private void importImage(UUID imageUUID) throws IOException {
		// First import the full sized image
		File imageFile = ImageUtils.getNoSyncImageFile(imageUUID);
		FileUtils.moveFile(imageFile, getImageFile());
	}

	// Similar to importWav, except for the mapping file.
	private void importMapping(UUID wavUUID, String id)
			throws IOException {
		File mapFile = new File(getNoSyncRecordingsPath(), wavUUID + ".map");
		FileUtils.moveFile(mapFile, getMapFile());
	}

	// Create a group ID (the prefix for recordings)
	private String createGroupId() {
		return IdUtils.sampleFromAlphabet(ITEM_ID_LEN, "abcdefghijklmnopqrstuvwxyz");
	}

	/**
	 * Returns a File that refers to the actual recording file.
	 *
	 * @return	The file the recording is stored in.
	 */
	public File getFile() {
		return getFile(id + "." + extension);
	}
	
	private File getFile(String idExt) {
		return new File(getRecordingsPath(), getGroupId() + "/"
				+ idExt);
	}
	
	/**
	 * Returns a File that refers to the recording's transcript file.
	 *
	 * @return	The transcript file of the recording.
	 */
	public File getTranscriptFile() {
		File f = new File(getRecordingsPath(), getGroupId() + "/"
				+ getTranscriptId() + "." + JSON_EXT);
		if(f.exists())
			return f;
		else
			return null;
	}
	
	/**
	 * Returns a File that refers to the recording's preview file.
	 *
	 * @return	The preview(sample) file of the recording.
	 */
	public File getPreviewFile() {
		File f = new File(getRecordingsPath(), getGroupId() + "/"
				+ getPreviewId() + "." + AUDIO_EXT);
		if(f.exists())
			return f;
		else
			return null;
	}
	
	/**
	 * Returns a File that refers to the recording's image file
	 * 
	 * @return The recording-image file, which might not exist
	 */
	public File getImageFile() {
		return new File(getRecordingsPath(), getGroupId() + "/"
				+ getImageId() + "." + IMAGE_EXT);
	}

	/**
	 * Gets the recording's image.
	 *
	 * @return	A Bitmap object.
	 * @throws	IOException	If the image cannot be retrieved.
	 */
	public Bitmap getImage() throws IOException {
		return ImageUtils.retrieveFromFile(getImageFile());
	}

	
	/**
	 * Returns a File that refers to the respeaking's mapping file.
	 *
	 * @return	The mapping file of the respeaking.
	 */
	public File getMapFile() {
		if(isOriginal())
			return null;
		return new File(getRecordingsPath(), getGroupId() + "/" +
				id + getSuffixExt(versionName, FileType.MAPPING));
	}
	
	
	public String getMapId() {
		return (id + MAPPING_SUFFIX);
	}
	
	public String getPreviewId() {
		return (id + SAMPLE_SUFFIX);
	}
	
	public String getImageId() {
		return (id + IMAGE_SUFFIX);
	}
	
	public String getTranscriptId() {
		return (getGroupId() + "-" + getOwnerId() + TRANSCRIPT_SUFFIX);
	}
	
	/**
	 * Returns an identifier used in cloud-storage
	 * @return	a relative-path of recording to 'aikuma/'
	 */
	public String getCloudIdentifier() {
		String ownerDirStr = (versionName + "/" + ownerId + "/");
		return (ownerDirStr + PATH + getGroupId() + "/" + id + "." + extension);
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
	
	/**
	 * Comments accessor; returns an empty string if the comments is null
	 * 
	 * @return	The comments of the recording
	 */
	public String getComments() {
		if (comments == null)
			return "";
		return comments;
	}

	public Date getDate() {
		return date;
	}

	/**
	 * Languages accessor; return a list of languages associated with the recording
	 * It looks for all language tag files under aikuma folder 
	 * 
	 * @return	a list of languages
	 */
	public List<Language> getLanguages() {
		if(languagesBuffer.size() != 0)
			return languagesBuffer;
		
		final Map<String, String> languageCodeMap = Aikuma.getLanguageCodeMap();
		
		filterTags(LANGUAGE_TAG_TYPE, new TagProcessor() {
			@Override
			public void processTag(String verGroupOwnerStr, String tagStr) {
				if(tagStr.length() > 7 && tagStr.startsWith("iso639_")) {
					String langCode = tagStr.substring(7);
					languagesBuffer.add(
							new Language(languageCodeMap.get(langCode), langCode));
				} else {
					languagesBuffer.add(new Language(tagStr, ""));
				}
			}
			
		});
		
		return languagesBuffer;
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
		if(speakersIdsBuffer.size() != 0)
			return speakersIdsBuffer;
		
		filterTags(SPEAKER_TAG_TYPE, new TagProcessor() {
			@Override
			public void processTag(String verGroupOwnerStr, String tagStr) {
				String tagCreator = verGroupOwnerStr.substring(Recording.ITEM_ID_LEN + 5);
				speakersCreatorsBuffer.add(tagCreator);
				speakersIdsBuffer.add(tagStr);
			}	
		});
		
		return speakersIdsBuffer;
	}
	
	/**
	 * speakers' file-models accessor.
	 * @return	a list of File-model instances of the recording's speakers
	 *          (Speakers will always have the same version with the recording)
	 */
	public List<Speaker> getSpeakers() {
		if(speakersIdsBuffer.size() == 0) {
			getSpeakersIds();
		}
		Log.i(TAG, speakersIdsBuffer.toString());
		//List<FileModel> speakers = new ArrayList<FileModel>();
		List<Speaker> speakers = new ArrayList<Speaker>();
		for(int i = 0; i < speakersIdsBuffer.size(); i++) {
			String creatorId = speakersCreatorsBuffer.get(i);
			String speakerId = speakersIdsBuffer.get(i);
			//speakers.add(new FileModel(this.versionName, creatorId, speakerId, SPEAKER_TYPE, IMAGE_EXT));
			try {
				speakers.add(Speaker.read(this.versionName, creatorId, speakerId));
			} catch(IOException e) {
				Log.e(TAG, speakerId + " file can't be read under the folder of " + creatorId);
			}
		}
		
		return speakers;
	}
	
	/**
	 * Get a list of OLAC tag strings associated with the recording
	 * 
	 * @return	a list of OLAC tag strings
	 */
	public List<String> getOLACTagStrings() {
		if(olacTagStringsBuffer.size() != 0)
			return olacTagStringsBuffer;
		
		olacTagStringsBuffer = new ArrayList<String>();
		
		filterTags(OLAC_TAG_TYPE, new TagProcessor() {
			@Override
			public void processTag(String verGroupOwnerStr, String tagStr) {
				olacTagStringsBuffer.add(tagStr);
			}
		});
		
		return olacTagStringsBuffer;
	}
	
	/**
	 * Get a list of custom tag strings associated with the recording
	 * 
	 * @return	a list of custom tag strings
	 */
	public List<String> getCustomTagStrings() {
		if(customTagStringsBuffer.size() != 0)
			return customTagStringsBuffer;
		
		customTagStringsBuffer = new ArrayList<String>();
		
		filterTags(CUSTOM_TAG_TYPE, new TagProcessor() {
			@Override
			public void processTag(String verGroupOwnerStr, String tagStr) {
				customTagStringsBuffer.add(tagStr);
			}
		});
		
		return customTagStringsBuffer;
	}
	
	/** 
	 * Get a list of FileModels of all tags associated with the recording
	 * 
	 * @return	a list of FileModel structures of all tags
	 */
	public List<FileModel> getTags() {
		final List<FileModel> tagFileModels = new ArrayList<FileModel>();
		
		filterTags(ALL_TAG_TYPE, new TagProcessor() {
			@Override
			public void processTag(String verGroupOwnerStr, String tagFileName) {
				String[] splitVerGroupOwnerStr = verGroupOwnerStr.split("-");
				if(splitVerGroupOwnerStr.length > 3) {
					for(int i = 3; i < splitVerGroupOwnerStr.length; i++) {
						splitVerGroupOwnerStr[2] += splitVerGroupOwnerStr[i];
					}
				}
				tagFileModels.add(new FileModel(splitVerGroupOwnerStr[0], 
						splitVerGroupOwnerStr[2], tagFileName, FileModel.TAG_TYPE, ""));
			}
			
		});
		
		return tagFileModels;
	}
	
	/**
	 * Returns true if the recording is a movie file
	 * (Currently movie file is only stored with .mp4 extension
	 * 
	 * @return	true if this is a movie
	 */
	public boolean isMovie() {
		return this.format.equals("mp4");
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
	 * groupId accessor.
	 *
	 * @return	The Id of the group this recording belongs in.
	 * of.
	 */
	public String getGroupId() {
		return groupId;
	}

	public String getRespeakingId() {
		return respeakingId;
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
	 * Encode the Recording as a corresponding JSONObject.
	 *
	 * @return	A JSONObject instance representing the Recording;
	 */
	public JSONObject encode() {
		JSONObject encodedRecording = new JSONObject();
		encodedRecording.put(NAME_KEY, this.name);
		encodedRecording.put(COMMENTS_KEY, this.comments);
		encodedRecording.put(DATE_KEY, new StandardDateFormat().format(this.date));
		encodedRecording.put(VERSION_KEY, this.versionName);
		encodedRecording.put(USER_ID_KEY, this.ownerId);
		encodedRecording.put(DEVICE_NAME_KEY, deviceName);
		encodedRecording.put(ANDROID_ID_KEY, this.androidID);
		encodedRecording.put(SAMPLERATE_KEY, getSampleRate());
		encodedRecording.put(DURATION_MSEC_KEY, getDurationMsec());
		encodedRecording.put(ITEM_ID_KEY, this.groupId);
		encodedRecording.put(RESPEAKING_ID_KEY, this.respeakingId);
		encodedRecording.put(SOURCE_VER_ID_KEY, this.sourceVerId);
		if(latitude != null && longitude != null) {
			JSONArray locationData = new JSONArray();
			locationData.add(latitude);
			locationData.add(longitude);
			encodedRecording.put(LOCATION_KEY, locationData);
		} else {
			encodedRecording.put(LOCATION_KEY, null);
		}
		
		encodedRecording.put(FILE_TYPE_KEY, getFileType());

		encodedRecording.put(FORMAT_KEY, this.format);
		encodedRecording.put(BITS_PER_SAMPLE_KEY, this.bitsPerSample);
		encodedRecording.put(NUM_CHANNELS_KEY, this.numChannels);
		return encodedRecording;
	}

	/**
	 * Make an index-file at the application root path
	 * 
	 * @param srcVerId		(source's versionName)-(source's ID)
	 * @param respkVerId	(respeaking's versionName)-(respeaking's ID)
	 * @throws IOException	thrown by read/write function
	 */
	private static void index(String srcVerId, String respkVerId) throws IOException {
		File indexFile = new File(FileIO.getAppRootPath(), "index.json");
		JSONObject indices = null;
		try {
			indices = FileIO.readJSONObject(indexFile);
		} catch (IOException e) {
			indices = new JSONObject();
		}
		
		String srcId = srcVerId.substring(4);
		String[] splitRespeakName = respkVerId.split("-");
		String val = splitRespeakName[0] + "-" + splitRespeakName[1] + "-" + splitRespeakName[2];
		
		JSONArray values = (JSONArray) indices.get(srcId);
		if(values == null) {
			values = new JSONArray();
		}
		if(!values.contains(val)) {
			values.add(val);
			indices.put(srcId, values);
			FileIO.writeJSONObject(indexFile, indices);
		}
	}
	
	/**
	 * Write the recording's metadata string to path
	 * 
	 * @param path				Path to a file of the metadata
	 * @param metadataJSONStr	JSON string of metadata
	 * @throws IOException		thrown by write/index functions
	 */
	public static void write(File path, String metadataJSONStr) throws IOException {
		FileIO.write(path, metadataJSONStr);
		
		JSONParser parser = new JSONParser();
		JSONObject jsonObj;
		try {
			jsonObj = (JSONObject) parser.parse(metadataJSONStr);
		} catch (org.json.simple.parser.ParseException e) {
			throw new IOException(e);
		}
		
		
		String jsonSrcVerId = (String) jsonObj.get(SOURCE_VER_ID_KEY);
		// if the recording is a respeaking.
		if(jsonSrcVerId != null) {
			String jsonVerName = (String) jsonObj.get(VERSION_KEY);
			String jsonGroupId = (String) jsonObj.get(ITEM_ID_KEY);
			String jsonOwnerId = (String) jsonObj.get(USER_ID_KEY);
			
			// Write the index file
			index(jsonSrcVerId, jsonVerName + "-" + jsonGroupId + "-" + jsonOwnerId);
		}
	}
	
	/**
	 * Write the Recording to file in a subdirectory of the recordings and move
	 * the recording WAV data to that directory
	 *
	 * @throws	IOException	If the recording metadata could not be written.
	 */
	public void write() throws IOException {
		// Ensure the directory exists
		File dir = new File(getRecordingsPath(), getGroupId());
		dir.mkdir();
		Log.i(TAG, "write: " + dir.getAbsolutePath());
		// Import the wave file into the new recording directory.
		if(this.isMovie()) {
			importMov(recordingUUID, getId());
		} else {
			Log.i("COPY", recordingUUID.toString());
			importWav(recordingUUID, getId());	
		}
		

		// if the recording is original
		if (isOriginal()) {
			// Import the sample wave file into the new recording directory
			String suffixExt = getSuffixExt(versionName, FileType.PREVIEW);
			importWav(recordingUUID + suffixExt, getId() + suffixExt);
			if(imageUUID != null)	// image is optional
				importImage(imageUUID);
		} else {
			// Try and import the mapping file
			importMapping(recordingUUID, getId());
			
			// Write the index file
			index(sourceVerId, getVersionName() + "-" + getId());
		}

		JSONObject encodedRecording = this.encode();


		// Write the json metadata.
		FileIO.writeJSONObject(new File(
				getRecordingsPath(), getGroupId() + "/" +
						id + getSuffixExt(versionName, FileType.METADATA)),
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
	 * @return	The original recording
	 * @throws	IOException	If there is an issue reading the originals JSON
	 * file
	 */
	public Recording getOriginal() throws IOException {
		String[] splitSourceName = sourceVerId.split("-");
		File ownerDir = 
				FileIO.getOwnerPath(splitSourceName[0], splitSourceName[2]);
		File groupDir = 
				new File(getRecordingsPath(ownerDir), getGroupId());

		// Filter for files that are source metadata
		File[] groupMetadataFileArray = groupDir.listFiles(
				new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				String[] splitFilename = filename.split("-");
				if (splitFilename[2].equals("source") &&
					filename.endsWith(getSuffixExt(versionName, FileType.METADATA))) {
					return true;
				}
				return false;
			}
		});

		assertTrue(groupMetadataFileArray.length == 1);

		return Recording.read(groupMetadataFileArray[0]);
	}

	/**
	 * Read a recording of the given ID 
	 * (assuming that the file and aikuma are in the same version)
	 * 
	 * @param 	id			The recording's ID
	 * @return	A recording object corresponding to the ID
	 * @throws IOException	if the recording metadata file cannot be read
	 */
	public static Recording read(String id) throws IOException {
		String[] splitId = id.split("-");
		return Recording.read(AikumaSettings.getLatestVersion(), splitId[1], id);
	}
	
	/**
	 * Read a recording corresponding to the given filename prefix.
	 *
	 * @param	verName			The recording's versionName
	 * @param	ownerAccount	The recording's ownerID
	 * @param	id				The recording's ID
	 * @return	A Recording object corresponding to the json file.
	 * @throws	IOException	If the recording metadata cannot be read.
	 */
	public static Recording read(String verName, String ownerAccount, 
			String id) throws IOException {
		String groupId = getGroupIdFromId(id);
		File ownerDir = FileIO.getOwnerPath(verName, ownerAccount);
		File metadataFile = 
				new File(getRecordingsPath(ownerDir),
						groupId + "/" + id + getSuffixExt(verName, FileType.METADATA));
		Log.i(TAG, metadataFile.getAbsolutePath());
		return read(metadataFile);
	}

	/**
	 * Read a recording from the file containing JSON describing the Recording
	 *
	 * @param	metadataFile	The file containing the metadata of the recording.
	 * @return	A Recording object corresponding to the json file.
	 * @throws	IOException	If the recording metadata cannot be read.
	 */
	public static Recording read(File metadataFile) throws IOException {
		JSONObject jsonObj = FileIO.readJSONObject(metadataFile);
		
		return read(jsonObj);
	}
	
	/**
	 * Read a recording from JSON object describing the Recording
	 *
	 * @param	jsonObj			JSON Object containing the metadata of the recording.
	 * @return	A Recording object corresponding to the json object.
	 * @throws	IOException	If the recording metadata doesn't exist.
	 */
	public static Recording read(JSONObject jsonObj) throws IOException {
		String groupId = (String) jsonObj.get(ITEM_ID_KEY);

		if (groupId == null) {
			throw new IOException("Null groupId in the JSON file.");
		}
		String name = (String) jsonObj.get(NAME_KEY);
		String dateString = (String) jsonObj.get(DATE_KEY);
		if (dateString == null) {
			throw new IOException("Null date in the JSON file.");
		}
		Date date;
		try {
			if(dateString.matches("^\\d{8}$")) {
				date = new SimpleDateFormat("yyyyMMdd").parse(dateString);
			} else {
				date = new StandardDateFormat().parse(dateString);
			}
			
		} catch (ParseException e) {
			throw new IOException(e);
		}
		String versionName = (String) jsonObj.get(VERSION_KEY);
		String ownerId = (String) jsonObj.get(USER_ID_KEY);
		String sourceVerId = (String) jsonObj.get(SOURCE_VER_ID_KEY);
		String format = (String) jsonObj.get(FORMAT_KEY);
		String fileType = (String) jsonObj.get(FILE_TYPE_KEY);
		String androidID = (String) jsonObj.get(ANDROID_ID_KEY);
		if (androidID == null) {
			throw new IOException("Null androidID in the JSON file.");
		}
		String respeakingId = (String) jsonObj.get(RESPEAKING_ID_KEY);
//		if (respeakingId == null) { // can be null when the metadata is read from cloud-fullText
//			throw new IOException("Null respeakingId in the JSON file.");
//		}

		long sampleRate;
		if (jsonObj.get(SAMPLERATE_KEY) == null) {
			sampleRate = -1;
		} else {
			sampleRate = (Long) jsonObj.get(SAMPLERATE_KEY);
		}

		int durationMsec;
		if (jsonObj.get(DURATION_MSEC_KEY) == null) {
			if(jsonObj.get(DURATION_CLOUD_KEY) == null) {
				durationMsec = -1;
				Log.i(TAG, "reading: null");
			} else {
				String durationRangeStr = (String) jsonObj.get(DURATION_CLOUD_KEY);
				durationMsec = DurRange.valueOf(durationRangeStr).getValue();
			}
			
		} else {
			durationMsec = ((Long) jsonObj.get(DURATION_MSEC_KEY)).intValue();
			Log.i(TAG, "reading: " + durationMsec);
		}
		Recording recording = new Recording(name, date, versionName, ownerId, 
				sourceVerId, /*languages, speakersIds, */androidID, groupId, 
				respeakingId, sampleRate, (Integer) durationMsec, format, fileType);
		return recording;
	}

	/**
	 * Returns a list of all the respeakings of this Recording. Ff it is a
	 * respeaking it will return an list of the other respeakings in the
	 * group.
	 * (If this is called by a respeaking, empty arraylist is returned)
	 *
	 * @return	A list of all the respeakings of the recording. 
	 * 			(an error can return an empty list)
	 *          (respekaings will always have the same version with original)
	 */
	public List<Recording> getRespeakings() {
		List<File> groupDirList = new ArrayList<File>();
		List<Recording> respeakings = new ArrayList<Recording>();
		if(!this.isOriginal()) return respeakings;
		
		File indexFile = new File(FileIO.getAppRootPath(), "index.json");
		JSONObject indices = null;
		try {
			indices = FileIO.readJSONObject(indexFile);
		} catch (IOException e1) {
			// TODO: How to deal with no index-file?
			Log.e(TAG, "getRespeakings(): error in reading index file");
			indices = new JSONObject();
			//return respeakings;
		}
		
		// Collect directories where related respeakings exist form index file
		JSONArray respkList = (JSONArray) indices.get(getId());
		if(respkList == null) {
			// TODO: How to deal with no index-file?
			respkList = new JSONArray();
			//return respeakings;
		}
		
		for (int i = 0; i < respkList.size(); i++) {
			String[] splitRespkName = ((String)respkList.get(i)).split("-");
			if(!splitRespkName[0].matches(versionName))
				continue;
			
			File ownerDir = 
					FileIO.getOwnerPath(splitRespkName[0], splitRespkName[2]);
			File groupDir = 
					new File(getRecordingsPath(ownerDir), splitRespkName[1]);
			//Check if this index is created for derivative recordings, not for tags 
			if(groupDir.exists())	
				groupDirList.add(groupDir);
		}
		// For the respeakings existing in the same folder of original
		File currentDir = new File(getRecordingsPath(), getGroupId());
		if(!groupDirList.contains(currentDir)) {
			groupDirList.add(currentDir);
		}
		
		// Read metadata files
		for(File groupDir: groupDirList) {

			File[] groupDirMetaFiles = groupDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.endsWith(getSuffixExt(versionName, FileType.METADATA));
				}
			});
	
			if(groupDirMetaFiles != null) {
				Recording recording;
				for (File recordingMetaFile : groupDirMetaFiles) {
					try {
						recording = Recording.read(recordingMetaFile);
						if (!recording.isOriginal()) {
							respeakings.add(recording);
						}
					} catch (IOException e) {
						// Well we can't read the recordings metadata file, so just
						// continue on.
					}
				}
			}
			
		}
		
		return respeakings;
	}
	
	/**
	 * read all tags in the device
	 * @return	a list of FileModel structures of all tags
	 */
	public static List<FileModel> readAllTags() {
		List<FileModel> tags = new ArrayList<FileModel>();
		
		// Get a list of version directories
		final String currentVersionName = AikumaSettings.getLatestVersion();
		File[] versionDirs = 
				FileIO.getAppRootPath().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				//return filename.startsWith("v") && filename.substring(1).matches("\\d+");
				return filename.matches(currentVersionName);
			}	
		});

		for(File f1 : versionDirs) {
			String versionName = f1.getName();
			//File[] firstHashDirs = f1.listFiles();
			//for(File f2 : firstHashDirs) {
				//File[] secondHashDirs = f2.listFiles();
				//for(File f3 : secondHashDirs) {
					File[] ownerIdDirs = f1.listFiles();
					for(File f : ownerIdDirs) {
						if(f.getName().equals(AdminFileModel.ADMIN))
							continue;
						addTagsInDir(versionName, tags, f);
					}
				//}
			//}
		}

		return tags;		
	}
	
	private static void addTagsInDir(final String versionName, 
			List<FileModel> tags, File ownerDir) {
		// Constructs a list of directories in the recordings directory.
		File[] recordingPathFiles = getTagsPath(ownerDir).listFiles();
		
		if (recordingPathFiles == null) {
			return;
		}
			
		for (File f : recordingPathFiles) {
			if (f.isDirectory()) {	// f is a item directory under social/
				File[] tagFiles = f.listFiles();

				for (File tagFile : tagFiles) {
					FileModel fm = new FileModel(versionName, ownerDir.getName(), 
							tagFile.getName(), FileModel.TAG_TYPE, "");
					tags.add(fm);
				}
			}
		}
	}

	/**
	 * Read all recordings
	 *
	 * @return	A list of all the recordings in the Aikuma directory.
	 */
	public static List<Recording> readAll() {
		return readAll(null);
	}
	
	/**
	 * Read all recordings of the user
	 *
	 * @param userId	The user's ID
	 * @return	A list of all the user's recordings in the Aikuma directory.
	 */
	public static List<Recording> readAll(String userId) {

		List<Recording> recordings = new ArrayList<Recording>();
		
		// Get a list of version directories
		final String currentVersionName = AikumaSettings.getLatestVersion();
		File[] versionDirs = 
				FileIO.getAppRootPath().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				//return filename.startsWith("v") && filename.substring(1).matches("\\d+");
				return filename.matches(currentVersionName);
			}	
		});

		for(File f1 : versionDirs) {
			String versionName = f1.getName();
			//File[] firstHashDirs = f1.listFiles();
			//for(File f2 : firstHashDirs) {
				//File[] secondHashDirs = f2.listFiles();
				//for(File f3 : secondHashDirs) {
					File[] ownerIdDirs = f1.listFiles();
					for(File f : ownerIdDirs) {
						String dirName = f.getName();
						if(dirName.equals(AdminFileModel.ADMIN))
							continue;
						
						if(userId == null || dirName.equals(userId)) {
							Log.i(TAG, "readAll: " + f.getPath());
							addRecordingsInDir(versionName, recordings, f);
						}
						
					}
				//}
			//}
		}

		return recordings;
	}

	private static void addRecordingsInDir(final String versionName, 
			List<Recording> recordings, File dir) {
		// Constructs a list of directories in the recordings directory.
		File[] recordingPathFiles = getRecordingsPath(dir).listFiles();
		
		if (recordingPathFiles == null) {
			return;
		}
			
		for (File f : recordingPathFiles) {
			if (f.isDirectory()) {
				// For each of those subdirectories, creates a list of files
				// within that end in .json
				File[] groupDirFiles = f.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String filename) {
						return filename.endsWith(getSuffixExt(versionName, FileType.METADATA));
					}
				});

				// Iterate over those recording metadata files and add the
				// recordings they refer to to the recordings list
				for (File jsonFile : groupDirFiles) {
					try {
						Recording rec = Recording.read(jsonFile);
						recordings.add(rec);
					} catch (IOException e) {
						// Couldn't read that recording for whateve rreason
						// (perhaps json file wasn't formatted correctly).
						// Let's just ignore that user.
						Log.e(TAG, "read exception(" + e.getMessage() + "): " + jsonFile.getName());
					}
				}
			}
		}
	}
	
	/**
	 * Index all recordings and create a index file
	 * 
	 * @throws IOException	while writing a index file
	 */
	public static void indexAll() throws IOException {
		File indexFile = new File(FileIO.getAppRootPath(), "index.json");
		
		JSONObject indices = new JSONObject();
		List<String> recordingIds = new ArrayList<String>();
		JSONObject itemDirPaths = new JSONObject();

		// Get a list of version directories
		File[] versionDirs = 
				FileIO.getAppRootPath().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.startsWith("v") && filename.substring(1).matches("\\d+");
			}	
		});

		for(File f1 : versionDirs) {
			//File[] firstHashDirs = f1.listFiles();
			//for(File f2 : firstHashDirs) {
				//File[] secondHashDirs = f2.listFiles();
				//for(File f3 : secondHashDirs) {
					File[] ownerIdDirs = f1.listFiles();
					for(File f4 : ownerIdDirs) {						
						if(f4.getName().equals(AdminFileModel.ADMIN))
							continue;
						
						Log.i(TAG, "indexAll: " + f4.getPath());
						List<File> itemDirsList = new ArrayList<File>(
								Arrays.asList(getRecordingsPath(f4).listFiles()));
						itemDirsList.addAll(Arrays.asList(getTagsPath(f4).listFiles()));
						
						for(File f5 : itemDirsList) {
							
							if (f5.isDirectory()) {
								// Record the path to this item folder
								String itemName = f5.getName();
								
								JSONArray values = (JSONArray) itemDirPaths.get(itemName);
								String val = f1.getName() + "-" + itemName + "-" + f4.getName();
								if(values == null) {
									values = new JSONArray();
								}
								if(!values.contains(val)) {
									values.add(val);
									itemDirPaths.put(f5.getName(), values);
								}
																					
								// Record all the recordings (original: derivative, tag / derivative: tag)
								File[] files = f5.listFiles(new FilenameFilter() {
									public boolean accept(File dir, String filename) {
										return filename.endsWith(AUDIO_EXT) || filename.endsWith(VIDEO_EXT);
									}
								});
								for(File f : files) {
									String fileName = f.getName();
									String srcId = fileName.substring(0, fileName.lastIndexOf('.'));
									String tempId = srcId.substring(0, srcId.length()-7);
									if(srcId.endsWith(SOURCE_TYPE) || tempId.endsWith(RESPEAKING_TYPE) ||
											tempId.endsWith(TRANSLATION_TYPE)) {
										// At most one source file can exist in one item folder
										if(!recordingIds.contains(srcId))
											recordingIds.add(srcId);
									}	
								}
								
							}
						}
					}
				//}
			//}
		}
		
		for(String srcId : recordingIds) {
			String itemId = srcId.split("-")[0];
			indices.put(srcId, itemDirPaths.get(itemId));
		}
		FileIO.writeJSONObject(indexFile, indices);
	}
	
	/**
	 * Updates all recording metadata files of versionNum
	 * 1. Update or create fields existing in newJSONFields
	 * 2. After 1, Change field keys existing in newJSONKeys(oldkey -> newkey)
	 * 
	 * @param versionNum		obsolete file-format's version
	 * @param newJSONFields		Map structure of new field-pairs(key:value)
	 * @param newJSONKeys		Map structure of new key-pairs(oldkey:newkey)
	 */
	public static void updateAll(Integer versionNum, 
			Map<String, Object> newJSONFields, Map<String, String> newJSONKeys) {
		if(newJSONFields == null && newJSONKeys == null)
			return;
		
		switch(versionNum) {
		case 0:
			File[] firstHashDirs = new File(FileIO.getAppRootPath(), "v01").listFiles();
			
			for(File f2 : firstHashDirs) {
				File[] secondHashDirs = f2.listFiles();
				for(File f3 : secondHashDirs) {
					File[] ownerIdDirs = f3.listFiles();
					for(File f : ownerIdDirs) {
						Log.i(TAG, "updateAll: " + f.getPath());
						
						updateMetadataInDir(f, "v01", newJSONFields, newJSONKeys);	
					}
				}
			}
			return;
		}
	}
	
	private static void updateMetadataInDir(File dir, final String versionName,
			Map<String, Object> newJSONFields, Map<String, String> newJSONKeys) {
		boolean isFields = (newJSONFields != null);
		boolean isKeys = (newJSONKeys != null);
		// Constructs a list of directories in the recordings directory.
		File[] recordingPathFiles = getRecordingsPath(dir).listFiles();
		
		if (recordingPathFiles == null) {
			return;
		}
		
		for (File f : recordingPathFiles) {
			if (f.isDirectory()) {
				// For each of those subdirectories, creates a list of files
				// within that end in .json
				File[] groupDirFiles = f.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String filename) {
						return filename.endsWith(getSuffixExt(versionName, FileType.METADATA));
					}
				});

				// Iterate over those recording metadata files and add the
				// recordings they refer to to the recordings list
				for (File jsonFile : groupDirFiles) {
					try {
						JSONObject newJSONObject = 
								FileIO.readJSONObject(jsonFile);
						
						if(isFields) {
							newJSONObject.putAll(newJSONFields);
						}
						if(isKeys) {
							for(String oldKey : newJSONKeys.keySet()) {
								Object val = newJSONObject.remove(oldKey);
								if(val != null) {
									String newKey = newJSONKeys.get(oldKey);
									newJSONObject.put(newKey, val);
								}
							}
						}
							
						FileIO.writeJSONObject(jsonFile, newJSONObject);
					} catch (IOException e) {
						Log.e(TAG, "Metadata update failed: " + e.toString());
					}
				}
			}
		}
	}
	
	/**
	 * Compares the given object with the Recording, and returns true if the
	 * Recording's name, date, languages, androidID, groupId and
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
				.append(comments, rhs.comments)
				.append(date, rhs.date)
				.append(androidID, rhs.androidID)
				.append(groupId, rhs.groupId)
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
	
	private void setComments(String comments) {
		this.comments = comments;
	}

	// Sets the date; the date cannot be null.
	private void setDate(Date date) {
		if (date == null) {
			throw new IllegalArgumentException(
					"Recording date cannot be null.");
		}
		this.date = date;
	}
	
	// Sets the file-type
	private void setFileType(String sourceVerId, List<Language> languages) {
		if (sourceVerId == null) {
			this.fileType = SOURCE_TYPE;
		} else {
			// Then this is either a respeaking or a translation.
			try {
				if (languages.equals(getOriginal().getLanguages())) {
					this.fileType = RESPEAKING_TYPE;
				} else {
					this.fileType = TRANSLATION_TYPE;
				}
			} catch (IOException e) {
				// There is an issue reading the original. A type won't be
				// written.
			}
		}
	}

	private void setDeviceName(String deviceName) {
		if (deviceName == null) {
			throw new IllegalArgumentException(
					"The model name cannot be null");
		}
		this.deviceName = deviceName.toUpperCase();
	}
	
	// Sets the android ID but won't accept a null string.
	private void setAndroidID(String androidID) {
		if (androidID == null) {
			throw new IllegalArgumentException(
					"The androidID for the recording cannot be null");
		}
		this.androidID = androidID;
	}

	private void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/*
	private void setId(String id) {
		this.id = id;
	}*/

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
	 * @param	ownerDir	A File representing the path of owner's directory
	 * @return	A File representing the path of the recordings directory
	 */
	public static File getRecordingsPath(File ownerDir) {
		File path = new File(ownerDir, PATH);
		path.mkdirs();
		return path;
	}

	/**
	 * Get the recording owner's directory
	 * 
	 * @return	A file representing the path of the recording owner's dir
	 */
	public File getRecordingsPath() {
		File path = new File(
				FileIO.getOwnerPath(versionName, ownerId), PATH);
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
		File path = new File(FileIO.getNoSyncPath(), PATH);
		path.mkdirs();
		return path;
	}
	
	/**
	 * Get the directory of the tags created by the owner
	 * 
	 * @param ownerDir		The owner directory 
	 * @return				The owner's tag directory
	 */
	public static File getTagsPath(File ownerDir) {
		File path = new File(ownerDir, TAG_PATH);
		path.mkdirs();
		return path;
	}
	
	/**
	 * Get this recording owner's tag directory
	 * @return	the tag directory
	 */
	public File getTagsPath() {
		File path = new File(
				FileIO.getOwnerPath(versionName, ownerId), TAG_PATH);
		path.mkdirs();
		return path;
	}

	/**
	 * Returns the groupId given an id
	 *
	 * @param	id	The filename prefix of the file whose
	 * groupId we seek
	 * @return	The corresponding group ID.
	 */
	public static String getGroupIdFromId(String id) {
		String[] splitId = id.split("-");
		assertTrue(splitId.length >= 3);
		return splitId[0];
	}
	
	/**
	 * Returns the recording's relative path, given a cloud-Identifier of Recording
	 * 
	 * @param cloudIdentifier	Cloud-Identifier of Recording
	 * @return					Relative-path
	 */
	public static String getRelPath(String cloudIdentifier) {
		return cloudIdentifier.substring(0, cloudIdentifier.lastIndexOf('/')-Recording.ITEM_ID_LEN);
	}


	/**
	 * Star the recording with this phone's androidID
	 * 
	 * @param currentVerName	Current aikuma's version
	 * @param userId			ID of an user who liked this recording
	 * @throws	IOException	In case there is an issue writing the star file.
	 * Note that this will not be thrown if the file already exists.
	 */
	public void star(String currentVerName, String userId) throws IOException {
		String androidID = Aikuma.getAndroidID();
		File starFile = new File(FileIO.getOwnerPath(currentVerName, userId), 
				"/social/" + getId() + "-like");
		starFile.getParentFile().mkdirs();
		starFile.createNewFile();
	}

	/**
	 * Flag the recording with this phone's androidID
	 *
	 * @param currentVerName	Current aikuma's version
	 * @param userId			ID of an user who dis-liked this recording
	 * @throws	IOException	In case there is an issue writing the flag file.
	 * Note that this will not be thrown if the file already exists.
	 */
	public void flag(String currentVerName, String userId) throws IOException {
		String androidID = Aikuma.getAndroidID();
		File flagFile = new File(FileIO.getOwnerPath(currentVerName, userId), 
				"/social/"  + getId() + "-report");
		flagFile.getParentFile().mkdirs();
		flagFile.createNewFile();
	}
	
	/**
	 * Make the archived recording's metadata
	 * @param backupDate	when upload finished
	 * @param downloadUrl	url where the recording file can be downloaded
	 * @throws	IOException	In case of an issue writing the archiveMetadata file.
	 * Note that this will not be thrown if the file already exists.
	 */
	public void archive(String backupDate, String downloadUrl) throws IOException {
		JSONObject archiveMetadata = new JSONObject();
		archiveMetadata.put("name", this.name);
		archiveMetadata.put("recording", this.groupId);
		archiveMetadata.put("backupDate", backupDate);
		archiveMetadata.put("download_url", downloadUrl);
		
		FileIO.writeJSONObject(new File(getRecordingsPath(), 
				getGroupId() + "/" + id + "-archive.json"),
				archiveMetadata);
	}
	
	/**
	 * Tag the recording
	 * 
	 * @param tagType		The kinds of tag (Language, Speaker, OLAC, Custom)
	 * @param tagStr		The contents of the tag
	 * @param userId		The user creating the tag
	 * @return the tag-file name or NULL if it fails
	 * @throws IOException	In case of an issue writing the tag file
	 */
	public String tag(TagType tagType, String tagStr, String userId) throws IOException {
		String tagOwnerId = AikumaSettings.getCurrentUserId();
		if(tagOwnerId == null) {
			Log.e(TAG, "Tagging fails because the creator is not specified");
			return null;
		}
		
		String tagTypeSuffix = "";
		switch(tagType) {
		case LANGUAGE:
			tagTypeSuffix = LANGUAGE_TAG_TYPE;
			languagesBuffer.clear();
			break;
		case SPEAKER:
			tagTypeSuffix = SPEAKER_TAG_TYPE;
			speakersCreatorsBuffer.clear();
			speakersIdsBuffer.clear();
			break;
		case OLAC:
			tagTypeSuffix = OLAC_TAG_TYPE;
			olacTagStringsBuffer.clear();
			break;
		case CUSTOM:
			tagTypeSuffix = CUSTOM_TAG_TYPE;
			customTagStringsBuffer.clear();
			break;
		}

		tagStr = tagStr.trim();
		tagStr = tagStr.replaceAll("\\s+", " ");
		tagStr = tagStr.replace(' ', '_');
		// Tag can only be created for the recordings having the same version with Aikuma version
		File tagFile = new File(FileIO.getOwnerPath(this.versionName, userId), 
				TAG_PATH + getGroupId() + "/" + 
				getId() + "-" + tagTypeSuffix + "-" + tagStr);
		tagFile.getParentFile().mkdirs();
		tagFile.createNewFile();
		
		index(getVersionName() + "-" + getId(), 
				AikumaSettings.getLatestVersion() + "-" + getGroupId() + "-" + tagOwnerId);
		
		return tagFile.getName();
	}
	
	private void filterTags(String tagTypeStr, TagProcessor tagProcessor) {
		List<String> selectedVerGroupOwners = new ArrayList<String>();
		List<File> groupDirList = new ArrayList<File>();
		
		File indexFile = new File(FileIO.getAppRootPath(), "index.json");
		JSONObject indices = null;
		try {
			indices = FileIO.readJSONObject(indexFile);
		} catch (IOException e1) {
			// TODO: How to deal with no index-file?
			Log.e(TAG, "getRespeakings(): error in reading index file");
			indices = new JSONObject();
		}
		
		// Collect directories where related respeakings exist form index file
		JSONArray verGroupOwnerList = (JSONArray) indices.get(getId());
		if(verGroupOwnerList == null) {
			// TODO: How to deal with no index-file?
			verGroupOwnerList = new JSONArray();
		}
		
		for (int i = 0; i < verGroupOwnerList.size(); i++) {
			String verGroupOwnerStr = (String) verGroupOwnerList.get(i);
			String[] splitVerGroupOwnerStr = verGroupOwnerStr.split("-");
			if(!splitVerGroupOwnerStr[0].matches(versionName))	// tag version needs to match recording version
				continue;
			
			File ownerDir = 
					FileIO.getOwnerPath(splitVerGroupOwnerStr[0], splitVerGroupOwnerStr[2]);
			File groupDir = 
					new File(getTagsPath(ownerDir), splitVerGroupOwnerStr[1]);
					
			if(groupDir.exists()) {
				selectedVerGroupOwners.add(verGroupOwnerStr);
				groupDirList.add(groupDir);
			}
				
		}
		
		// Process all the tag files 
		for(int i = 0; i < groupDirList.size(); i++) {
			String verGroupOwnerStr = selectedVerGroupOwners.get(i);
			File groupDir = groupDirList.get(i);
			
			File[] tagFiles = groupDir.listFiles();
			for(File tagFile : tagFiles) {
				String tagTypeSuffix = "";
				String tagStr = "";
				String[] splitName = tagFile.getName().split("-");
				
				if(fileType.equals(SOURCE_TYPE) && 
						splitName[2].equals(SOURCE_TYPE)) {
					tagTypeSuffix = splitName[3];
					tagStr = splitName[4];
				} else if(fileType.equals(splitName[2]) && 
						respeakingId.equals(splitName[3])) {
					tagTypeSuffix = splitName[4];
					tagStr = splitName[5];
				} else
					continue;
				
				if(tagTypeStr.equals(ALL_TAG_TYPE)) {
					tagProcessor.processTag(verGroupOwnerStr, tagFile.getName());
				} else if(tagTypeSuffix.equals(tagTypeStr)) {
					tagProcessor.processTag(verGroupOwnerStr, tagStr);
				}
			}
		}
		
	}

	/**
	 * Tells us whether this phone has already starred the Recording
	 *
	 * @param currentVerName	Current aikuma's version
	 * @param userId			ID of an user who liked this recording
	 * @return	true if a star file with this androidID is present; false
	 * otherwise
	 */
	public boolean isStarredByThisPhone(String currentVerName, String userId) {
		String androidID = Aikuma.getAndroidID();
		File starFile = 
				new File(FileIO.getOwnerPath(currentVerName, userId), "/social/" +
						getId() + "-like");
		return starFile.exists();
	}

	/**
	 * Tells us whether this phone has already flagged the Recording
	 *
	 * @param currentVerName	Current aikuma's version
	 * @param userId			ID of an user who dis-liked this recording
	 * @return	true if a flag file with this androidID is present; false
	 * otherwise
	 */
	public boolean isFlaggedByThisPhone(String currentVerName, String userId) {
		String androidID = Aikuma.getAndroidID();
		File flagFile = 
				new File(FileIO.getOwnerPath(currentVerName, userId), "/social/" +
						getId() + "-report");
		return flagFile.exists();
	}
	
	/**
	 * Tells us whether this recording has been archived
	 *
	 * @return	true if a archiveMetadata file is present; false otherwise
	 */
	public boolean isArchived() {
		File archiveMetaFile = new File(getRecordingsPath(), 
				getGroupId() + "/" + getId() + "-archive.json");
		return archiveMetaFile.exists();
	}

	/**
	 * Gives the number of stars this recording has received.
	 *
	 * @return	The number of stars this recording has recieved
	 */
	public int numStars() {
		// TODO: New file-storage model needs to be decided (1. create index / 2. put these in app-root folder)
		File starDir = new File(FileIO.getOwnerPath(versionName, ownerId), 
				"/social/" +
				getGroupId() + "/" + getId());
		File[] starFiles = starDir.listFiles(
				new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".star")) {
					return true;
				}
				return false;
			}
		});
		if (starFiles == null) {
			return 0;
		}
		return starFiles.length;
	}

	/**
	 * Gives the number of flags this recording has received.
	 *
	 * @return	The number of flags this recording has recieved
	 */
	public int numFlags() {
		// TODO: New file-storage model needs to be decided (1. create index / 2. put these in app-root folder)
		File socialDir = new File(FileIO.getOwnerPath(versionName, ownerId), 
				"/social/" +
				getGroupId() + "/" + getId());
		File[] flagFiles = socialDir.listFiles(
				new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".flag")) {
					return true;
				}
				return false;
			}
		});
		if (flagFiles == null) {
			return 0;
		}
		return flagFiles.length;
	}

	/**
	 * Gives the number of times this recording has been views.
	 *
	 * @return	The number of times this recording has been viewed.
	 */
	public int numViews() {
		//TODO: New file-storage model for the number of views need to be decided
		File socialDir = new File(FileIO.getOwnerPath(versionName, ownerId),
				"/views/" +
				getGroupId() + "/" + getId());
		File[] flagFiles = socialDir.listFiles();
		if (flagFiles == null) {
			return 0;
		}
		return flagFiles.length;
	}

	/**
	 * Indicates that this recording is allowed to be synced by moving it to a
	 * directory that the SyncUtil synchronizes.
	 *
	 * @return	A transcript of this recording
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
	 * Returns the transcript for this recording, or an empty transcript.
	 *
	 * @return	A transcript of this recording
	 */
	public TempTranscript getTranscript() {
		// Find all the transcript files
		File recordingDir = new File(getRecordingsPath(), getGroupId());
		File[] transcriptFiles = recordingDir.listFiles(
				new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				if (filename.split("-")[2].equals("transcript")) {
					Log.i(TAG, "filename: " + filename);
					Log.i(TAG, "split[2]: " +
							filename.split("-")[2]);
					return true;
				}
				return false;
			}
		});

		if(transcriptFiles == null)
			return null;
		
		// Take the first one
		for (File transcriptFile : transcriptFiles) {
			Log.i(TAG, "transcriptFile: " + transcriptFile);
			try {
				return new TempTranscript(this, transcriptFile);
			} catch (IOException e) {
				continue;
			}
		}

		// Just return an empty transcript
		// return new TempTranscript();
		return null;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Creates a Parcel object representing the Recording.
	 *
	 * @param	out	The parcel to be written to
	 * @param	_flags	Unused additional flags about how the object should be
	 * written.
	 */
	public void writeToParcel(Parcel out, int _flags) {
		//TODO: IF needed later
	}
	
	// The temporary UUID of the image before it gets renamed appropriately.
	private UUID imageUUID;
	
	/**
	 * The recording's name.
	 */
	private String name;
	
	/**
	 * The optional free string
	 */
	private String comments;
	
	/**
	 * The recording's date.
	 */
	private Date date;

	private List<Language> languagesBuffer = new ArrayList<Language>();

	private List<String> speakersIdsBuffer = new ArrayList<String>();
	private List<String> speakersCreatorsBuffer = new ArrayList<String>();
	
	private List<String> olacTagStringsBuffer = new ArrayList<String>();
	private List<String> customTagStringsBuffer = new ArrayList<String>();
	
	/**
	 * The device model name
	 */
	private String deviceName;
	
	/**
	 * The Android ID of the device that the recording was made on.
	 */
	private String androidID;

	/**
	 * The ID that represents the group of recordings.
	 */
	private String groupId;

	/**
	 * The sample rate of the recording in Hz
	 */
	private long sampleRate;

	/**
	 * The duration of the recording in seconds (floored)
	 */
	private int durationMsec;

	// The UUID of the source WAV.
	private UUID recordingUUID;

	// The respeaking ID that is at the end of the filename prefix.
	private String respeakingId;

	// The ID of a recording, which is of one of the following structures:
	// (inherited from super-class)
	//
	//		<groupId>-<ownerId>-source (for originals)
	//		<groupId>-<ownerId>-<respeaking type>-<respeakingId> (for
	//		respeakings, transcriptions, commentaries, etc.)
	// private String id;

	//The ID of the source recording.
	private String sourceVerId;

	// Some info regarding the recording format.
	//private String format;
	private int bitsPerSample;
	private int numChannels;
	
	//Location data
	private Double latitude;
	private Double longitude;
	
	/**
	 * Relative path where recording files are stored
	 */
	public static final String PATH = "items/";
	/** Relative path where tag files are stored */
	public static final String TAG_PATH = "social/";
	/** the length of item_id */
	public static final int ITEM_ID_LEN = 16;
	
	/**
	 * Keys of the recording metadata fields
	 */
	public static final String NAME_KEY = "title";
	/** */
	public static final String COMMENTS_KEY = "comments";
	/** */
	public static final String DATE_KEY = "date";
	/** */
	public static final String DEVICE_NAME_KEY = "device";
	/** */
	public static final String ANDROID_ID_KEY = "android_id";
	/** */
	public static final String SAMPLERATE_KEY = "samplerate";
	/** */
	public static final String DURATION_MSEC_KEY = "duration_msec";
	/** */
	public static final String ITEM_ID_KEY = "item_id";
	/** */
	public static final String RESPEAKING_ID_KEY = "suffix";
	/** */
	public static final String SOURCE_VER_ID_KEY = "source";
	/** */
	public static final String LOCATION_KEY = "location";
	/** */
	public static final String FILE_TYPE_KEY = "file_type";
	/** */
	public static final String FORMAT_KEY = "format";
	/** */
	public static final String BITS_PER_SAMPLE_KEY = "bits_per_sample";
	/** */
	public static final String NUM_CHANNELS_KEY = "num_channels";
	
	/** 
	 * Keys used/tweaked in cloud fullText metadata fields
	 */
	public static final String ITEM_ID_PREFIX_KEY = "item_pre";
	/** */
	public static final String DURATION_CLOUD_KEY = "dur";
	/** 4 range of durations (used for fullText-indesing/search) */
	public enum DurRange {
		/** S: < 3min, M: 3-10min, L:10-30min, XL: > 30min */
		S(-2), M(-3), L(-4), X(-5);
		private final int value;
		private DurRange(int val) {this.value = val; }
		/** @return the integer represenation value   */
		public int getValue() { return this.value; }
		/** @return min integer representation value(S:-2) */
		public static int getMinValue() { return DurRange.S.value; }
		/** @return max integer representation value(XL:-5) */
		public static int getMaxValue() { return DurRange.X.value; }
		/** Factory function creating DurRange from miliSeconds 
		 *  @param	durMsec	MiliSeconds
		 *  @return	DurRange instance
		 */
		public static DurRange fromDurationMsec(int durMsec) {
			if(durMsec > 1800000) {
				return DurRange.X;
			} else if(durMsec > 600000) {
				return DurRange.L;
			} else if(durMsec > 180000) {
				return DurRange.M;
			} else if(durMsec >= 0) {
				return DurRange.S;
			} else {
				return null;
			}
		}
	}
	
	private static Set<String> fieldKeySet;
	static {
		fieldKeySet = new HashSet<String>();
		fieldKeySet.add(NAME_KEY);
		fieldKeySet.add(COMMENTS_KEY);
		fieldKeySet.add(DATE_KEY);
		fieldKeySet.add(VERSION_KEY);
		fieldKeySet.add(USER_ID_KEY);
		fieldKeySet.add(DEVICE_NAME_KEY);
		fieldKeySet.add(ANDROID_ID_KEY);
		fieldKeySet.add(SAMPLERATE_KEY);
		fieldKeySet.add(DURATION_MSEC_KEY);
		fieldKeySet.add(ITEM_ID_KEY);
		fieldKeySet.add(RESPEAKING_ID_KEY);
		fieldKeySet.add(SOURCE_VER_ID_KEY);
		fieldKeySet.add(LOCATION_KEY);
		fieldKeySet.add(FILE_TYPE_KEY);
		fieldKeySet.add(FORMAT_KEY);
		fieldKeySet.add(BITS_PER_SAMPLE_KEY);
		fieldKeySet.add(NUM_CHANNELS_KEY);
	}
	
	/** A list of tag types */
	public enum TagType { LANGUAGE, SPEAKER, OLAC, CUSTOM };
	/** */
	public static final String LANGUAGE_TAG_TYPE = "language";
	/** */
	public static final String SPEAKER_TAG_TYPE = "speaker";
	/** */
	public static final String OLAC_TAG_TYPE = "olac";
	/** */
	public static final String CUSTOM_TAG_TYPE = "custom";
	/** */
	public static final String ALL_TAG_TYPE = "alltags";
	
	private static Set<String> tagTypeSet;
	static {
		tagTypeSet = new HashSet<String>();
		tagTypeSet.add(LANGUAGE_TAG_TYPE);
		tagTypeSet.add(SPEAKER_TAG_TYPE);
		tagTypeSet.add(OLAC_TAG_TYPE);
		tagTypeSet.add(CUSTOM_TAG_TYPE);	// The filename has the format(speakerID-image-small)
	}
	
	
	/**
	 * A function pointer passed to filterTags()
	 */
	private interface TagProcessor {
		/**
		 * Interface function to process collected tags
		 * @param verGroupOwnerStr	a string of (version)-(Group ID)-(Owner ID)
		 * @param tagContentStr		a string of tag content
		 */
		public void processTag(String verGroupOwnerStr, String tagContentStr);
	}
}
