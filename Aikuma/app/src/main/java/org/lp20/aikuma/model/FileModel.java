/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.lp20.aikuma.util.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The file modeled from the viewpoint of GoogleCloud
 * (The parent class of Recording and Speaker / 
 * This can encapsulate the transcript/mapping as well)
 *
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class FileModel implements Parcelable {

	private static final String TAG = FileModel.class.getSimpleName();
	
	private static final String CLOUD_ID_FORMAT = "^v\\d{2}\\/.+\\/.+\\/.+\\/.+$";
	/**
	 *  Suffix of metadata, maaping and transcript
	 */
	protected static final String METADATA_SUFFIX = "-metadata";
	/** */
	public static final String MAPPING_SUFFIX = "-mapping";
	/** */
	public static final String TRANSCRIPT_SUFFIX = "-transcript";
	/** */
	public static final String SAMPLE_SUFFIX = "-preview";
	/** */
	public static final String IMAGE_SUFFIX = "-image";
	
	/**
	 * A list of file-types 
	 */
	//public enum FileType { RECORDING, SPEAKER, OTHER };
	public enum FileType { SOURCE, RESPEAKING, TRANSLATION, SPEAKER, METADATA, PREVIEW, TRANSCRIPT, MAPPING, TAG, SEGMENT };
	/** */
	public static final String SOURCE_TYPE = "source";
	/** */
	public static final String RESPEAKING_TYPE = "respeak";
	/** */
	public static final String TRANSLATION_TYPE = "interpret";
	/** */
	public static final String SPEAKER_TYPE = "speaker";
	/** */
	public static final String METADATA_TYPE = "metadata";
	/** */
	public static final String PREVIEW_TYPE = "preview";
	/** */
	public static final String TRANSCRIPT_TYPE = "transcript";
	/** */
	public static final String MAPPING_TYPE = "mapping";
	/** */
	public static final String TAG_TYPE = "social";
	/** */
	public static final String SEGMENT_TYPE = "segment";
	/** */
	public static final String IMAGE_TYPE = "image";
	
	private static Set<String> fileTypeSet;
	static {
		fileTypeSet = new HashSet<String>();
		fileTypeSet.add(SOURCE_TYPE);
		fileTypeSet.add(RESPEAKING_TYPE);
		fileTypeSet.add(TRANSLATION_TYPE);
		fileTypeSet.add(SPEAKER_TYPE);	// The filename has the format(speakerID-image-small)
		fileTypeSet.add(METADATA_TYPE);
		fileTypeSet.add(PREVIEW_TYPE);
		fileTypeSet.add(TRANSCRIPT_TYPE);
		fileTypeSet.add(MAPPING_TYPE);
		fileTypeSet.add(TAG_TYPE);
		fileTypeSet.add(SEGMENT_TYPE);
		fileTypeSet.add(IMAGE_TYPE);
	}
	
	/**
	 * A list of file formats
	 */
	public enum FileFormat { WAV, MP4, JPG, JSON, TXT };
	/** */
	public static final String AUDIO_EXT = "wav";
	/** */
	public static final String VIDEO_EXT = "mp4";
	/** */
	public static final String IMAGE_EXT = "jpg";
	/** */
	public static final String JSON_EXT = "json";
	/** */
	public static final String TEXT_EXT = "txt";
	
	/**
	 * Keys of the metadata fields
	 */
	public static final String USER_ID_KEY = "user_id";
	/** */
	public static final String VERSION_KEY = "version";
	/** */
	public static final String DATA_STORE_URI_KEY = "data_store_uri";
	/** */
    public static final String DATE_CLOUD_KEY = "dat";
    /** */
    public static final String MULTI_META_CLOUD_DEFAULT_VAL = "mutli";

	/**
	 * Constructor of FileModel
	 * 
	 * @param versionName	versionName of the file-format
	 * @param ownerId		OwnerID(UserID) of the file
	 * @param id			Id of the file (File's name)
	 * @param type			Type of the file(speaker, mapping, ...)
	 * @param format		Extension of the file(jpg, mp4, vnd.wave, json, txt)
	 */
	public FileModel(
			String versionName, String ownerId, String id, String type, String format) {
		setVersionName(versionName);
		setOwnerId(ownerId);
		setId(id);
		this.fileType = type;
		this.format = format;
		if(format.equals("vnd.wave")) // vnd.wave is input by Recording constructor
			this.extension = AUDIO_EXT;
		else
			this.extension = format;
	}
	
	/**
	 * Factory function creating an instance of FileModel from cloud-ID
	 * @param cloudIdentifier	Cloud-identifier of the file
	 * @return	an instance of FileModel
	 */
	public static FileModel fromCloudId(String cloudIdentifier) {
		// (version)/(hash1)/(hash2)/(ownerID)/(items)/(itemID)/(FileName)
		String[] splitCloudId = cloudIdentifier.split("\\/");
		
		int index = splitCloudId[4].lastIndexOf('.');
		String fileName;
		String ext;
		
		if(splitCloudId[2].equals(TAG_TYPE)) {	//tags
			fileName = splitCloudId[4];
			ext = "";
			return new FileModel(splitCloudId[0], splitCloudId[1], fileName, TAG_TYPE, ext);
		}
		
		fileName = splitCloudId[4].substring(0, index);
		ext = splitCloudId[4].substring(index+1);
		
		String[] splitName = fileName.split("-");
		String fileType = splitName[splitName.length-1];
		
		if(StringUtils.isNumeric(fileType))		//Derivative recordings(respeaking, interpret)
			fileType = splitName[splitName.length-2];
		
		if(fileType.equals(METADATA_TYPE) && 
				fileName.length() == Speaker.SPEAKER_ID_LEN + METADATA_SUFFIX.length()) {	//speaker small image
			return new FileModel(splitCloudId[0], splitCloudId[1], splitCloudId[3], SPEAKER_TYPE, ext);
		} else if(fileType.equals(METADATA_TYPE)) {
			return null;
		} else if(fileTypeSet.contains(fileType)) {
			return new FileModel(splitCloudId[0], splitCloudId[1], fileName, fileType, ext);
		} else {
			return null;
		}
		
	}
	
	/**
	 * Checks if the string is in the format of cloud-identifier
	 * 
	 * @param cloudIdentifier	Cloud-Identifier of a file
	 * @return					true if it's in correct format
	 */
	public static boolean checkCloudFormat(String cloudIdentifier) {
		return cloudIdentifier.matches(CLOUD_ID_FORMAT);
	}
	
	/**
	 * Returns (suffix).(ext) for metadata, mapping, transcript, preview
	 * 			null		  for other types
	 * 
	 * @param versionName	Version
	 * @param type			METADATA | MAPPING | TRANSCRIPT | PREVIEW
	 * @return a string of "(suffix).(ext)"
	 */
	public static String getSuffixExt(String versionName, FileType type) {
		String suffixExt = null;
		
		// For a current version
		switch(type) {
		case TAG:
			break;
		case SOURCE:
			break;
		case RESPEAKING:
			break;
		case TRANSLATION:
			break;
		case SEGMENT:
			break;
		case SPEAKER:
			break;
		case METADATA:
			suffixExt = METADATA_SUFFIX + "." + JSON_EXT;
			break;
		case PREVIEW:
			suffixExt = SAMPLE_SUFFIX + "." + AUDIO_EXT;
			break;
		case TRANSCRIPT:
			if(versionName.equals("v01")) {
				suffixExt = TRANSCRIPT_SUFFIX + "." + TEXT_EXT;
			} else {
				suffixExt = TRANSCRIPT_SUFFIX + "." + JSON_EXT;
			}
			break;
		case MAPPING:
			if(versionName.equals("v01")) {
				suffixExt = MAPPING_SUFFIX + "." + TEXT_EXT;
			} else {
				suffixExt = MAPPING_SUFFIX + "." + JSON_EXT;
			}
			break;
		}
		
		return suffixExt;
	}
	
	/**
	 * Check the file type embedded in the cloudIdentifier
	 * 
	 * @param cloudIdentifier		Cloud-Identifier of the file
	 * @param versionName			The file's version
	 * @param type					Type to check
	 * @return						true if the file is the type, else false
	 */
	public static boolean checkType(String cloudIdentifier, String versionName, FileType type) {
		switch(type) {
		case SEGMENT:
			return cloudIdentifier.substring(0, cloudIdentifier.length()-12).endsWith(SEGMENT_TYPE);
		default:
			return cloudIdentifier.endsWith(FileModel.getSuffixExt(versionName, type));
		}
	}
	
	/**
	 * Returns the recording's relative path, given a cloud-Identifier of Recording
	 * 
	 * @param cloudIdentifier	Cloud-Identifier of Recording
	 * @return					Relative-path
	 */
	public static String getRelPath(String cloudIdentifier) {
		int groupPathPos = cloudIdentifier.lastIndexOf('/');
		return cloudIdentifier.substring(0, cloudIdentifier.lastIndexOf('/', groupPathPos-1) + 1);
	}
	
	/**
	 * Constructor for parcel (only used by Speaker)
	 * @param in	Parcel where the speaker will be created
	 */
	protected FileModel(Parcel in) {
		String versionName = in.readString();
		String ownerId = in.readString();
		setVersionName(versionName);
		setOwnerId(ownerId);
		this.fileType = SPEAKER_TYPE;
		this.extension = IMAGE_EXT;
	}
	
	/**
	 * Getter of the file ID
	 * @return	the ID of this file-model
	 */
	public String getId() {
		return id;
	}
	
	protected void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Getter of the file ID + extension (used in GoogleCloudService)
	 * @return	The ID + extension of the file-model
	 */
	public String getIdExt() {
		if(fileType.equals(SPEAKER_TYPE)) {
			return (id + getSuffixExt(versionName, FileType.METADATA));
		} else if(fileType.equals(TAG_TYPE)) {
			return id;
		} else {
			return (id + "." + getExtension());
		}
	}
	
	/**
	 * Getter of the file's metadata ID + extension
	 * @return	The metadata ID + extension of the file-model
	 */
	public String getMetadataIdExt() {
		if(fileType.equals(SPEAKER_TYPE) || fileType.equals(SOURCE_TYPE) || fileType.equals(SEGMENT_TYPE) ||
				fileType.equals(RESPEAKING_TYPE) || fileType.equals(TRANSLATION_TYPE))
			return (id + getSuffixExt(versionName, FileType.METADATA));
		return null;
	}
	
	/**
	 * Returns an identifier used in cloud-storage
	 * @param option	0: file(small-image/recording), 
	 * 					1: metadata (return null if metadata doesn't exist)
	 * @return	File's cloudId (a relative-path of recording to 'aikuma/')
	 */
	public String getCloudIdentifier(int option) {
		if(option != 0 && option != 1)
			return null;
		
		String ownerDirStr = (versionName + "/" + ownerId + "/");
		
		String suffix;
		if(fileType.equals(TAG_TYPE)) {
            String groupId = getId().split("-")[0];

            if(option == 0) {
                return (ownerDirStr + Recording.TAG_PATH + groupId + "/" + getId());
            } else {
                suffix = "." + Recording.AUDIO_EXT;
                int tagValStart = id.lastIndexOf('-');
                int tagKeyStart = id.substring(0, tagValStart).lastIndexOf('-');
                String sourceCloudId = id.substring(0, tagKeyStart);

                return (ownerDirStr + Recording.PATH + groupId + "/" + sourceCloudId + suffix);
            }
		} else if(fileType.equals(SPEAKER_TYPE)) {
			suffix = getSuffixExt(versionName, FileType.METADATA);
			
			return (ownerDirStr + Speaker.PATH + getId() + "/" + getId() + suffix);
		} else {
			if(option == 0)
				suffix = "." + getExtension();
			else if(!(fileType.equals(SOURCE_TYPE) || fileType.equals(RESPEAKING_TYPE)
					|| fileType.equals(TRANSLATION_TYPE) || fileType.equals(SEGMENT_TYPE)))	// No metadata for transcript/mapping/preview
				return null;
			else
				suffix = getSuffixExt(versionName, FileType.METADATA);
			String groupId = getId().split("-")[0];
			
			return (ownerDirStr + Recording.PATH + groupId + "/" + getId() + suffix);
		}
	}
	
	/**
	 * Returns a File that refers to the item's file/metadata-file.
	 * 
	 * @param option	0: file, 1: metadata-file
	 * @return	The file/metadata-file of the item
	 */
	public File getFile(int option) {
		if(option != 0 && option != 1)
			return null;
		
		File itemPath;
		String suffix;
		if(fileType.equals(TAG_TYPE)) {
			if(option != 0)
				return null;
			
			itemPath = new File(
					FileIO.getOwnerPath(versionName, ownerId), Recording.TAG_PATH);
			itemPath.mkdirs();
			
			String groupId = getId().split("-")[0];
			return new File(itemPath, groupId + "/" + getId());
		} else if(fileType.equals(SPEAKER_TYPE)) {
			itemPath = new File(
					FileIO.getOwnerPath(versionName, ownerId), Speaker.PATH);
			itemPath.mkdirs();

			suffix = getSuffixExt(versionName, FileType.METADATA);
			
			return new File(itemPath, getId() + "/" + getId() + suffix);
		} else {
			itemPath = new File(
					FileIO.getOwnerPath(versionName, ownerId), Recording.PATH);
			itemPath.mkdirs();
			
			if(option == 0)
				suffix = "." + getExtension();
			else if(!(fileType.equals(SOURCE_TYPE) || fileType.equals(RESPEAKING_TYPE)
					|| fileType.equals(TRANSLATION_TYPE) || fileType.equals(SEGMENT_TYPE)))	// No metadata for transcript/mapping/preview
				return null;
			else
				suffix = getSuffixExt(versionName, FileType.METADATA);
			String groupId = getId().split("-")[0];
			
			return new File(itemPath, groupId + "/" + getId() + suffix);
		}
		
	}

	/**
	 * Get all of the recording's tags as a map(tagType, tagContent)
	 * (Used for fullText-indexing in GoogleCloudService)
	 * 
	 * @return	Map of the recording's tags
	 */
	public Map<String, String> getAllTagMapStrs() {
		//List<String> tagStrs = new ArrayList<String>();
		Map<String, String> tagStrs = new HashMap<String, String>();
		String respeakingId = "";
		
		if(!(fileType.equals(SOURCE_TYPE) || fileType.equals(SEGMENT_TYPE) ||
				fileType.equals(RESPEAKING_TYPE) || fileType.equals(TRANSLATION_TYPE)))
			return tagStrs;
		if(!fileType.equals(SOURCE_TYPE))
			respeakingId = getId().split("-")[3];
		
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
					new File(Recording.getTagsPath(ownerDir), splitVerGroupOwnerStr[1]);
					
			if(groupDir.exists()) {
				selectedVerGroupOwners.add(verGroupOwnerStr);
				groupDirList.add(groupDir);
			}
				
		}
		
		// Process all the tag files 
		for(int i = 0; i < groupDirList.size(); i++) {
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
						respeakingId.equals(splitName[3])) { // Respeaking/Interpret
					tagTypeSuffix = splitName[4];
					tagStr = splitName[5];
				} else
					continue;
				
				String val = tagStrs.get(tagTypeSuffix);
				if(val != null) {
					tagStrs.put(tagTypeSuffix, val + "|" + tagStr);
				} else {
					tagStrs.put(tagTypeSuffix, tagStr);
				}
				//tagStrs.add(tagTypeSuffix + "__" + tagStr);
				/*
				if(tagTypeSuffix.equals(Recording.SPEAKER_TAG_TYPE)) {
					
				} else if(tagTypeSuffix.equals(Recording.LANGUAGE_TAG_TYPE)) {
					
				} else if(tagTypeSuffix.equals(Recording.OLAC_TAG_TYPE)) {
					
				} else if(tagTypeSuffix.equals(Recording.CUSTOM_TAG_TYPE)) {
					
				}*/
			}
		}
		return tagStrs;
	}
	
	/**
	 * Get the file's type
	 * TODO: 'respeaking' needs to be changed later to 'respeak'. 'comment','interpret' can be added later
	 * @return	the File-type (source, respeaking, translation, preview, speaker, mapping, transcript)
	 */
	public String getFileType() {
		return fileType;
	}
	
	/**
	 * Get the file's format (only used by Recording now)
	 * @return	the File-format can be (vnd.wave, mp4)
	 */
	public String getFormat() {
		return format;
	}
	
	public String getOwnerId() {
		return ownerId;
	}
	
	public String getVersionName() {
		return versionName;
	}
	
	/**
	 * Sets the versionName(v0x)
	 * @param versionName a string
	 */
	protected void setVersionName(String versionName) {
		if(versionName == null)
			throw new IllegalArgumentException("version should not be null");
		this.versionName = versionName;
	}
	
	// Sets the ownerId(Google account)
	protected void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}	
	
	/**
	 * Get the file's extension
	 * @return	the File-extension (wav, mp4, jpg, json, txt)
	 */
	private String getExtension() {
		return extension;
		/*
		if(format.equals("mp4")) {
			return ".mp4";
		} else if(format.equals("jpg")) {
			return ".jpg";
		} else if(fileType!= null && fileType.equals("other")) {
			return "." + format;
		} else {
			return ".wav";
		}*/
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO: implemented later if needed
	}
	
	/**
	 * The (recording/speaker)'s format version
	 */
	protected String versionName;
	
	/**
	 * The (recording/speaker)'s owner ID
	 */
	protected String ownerId;
	
	/**
	 * The ID of the (recording/speaker).
	 * SpeakerID = 12 upper-case alphabets
	 * RecordingID = (item_id)-(user_id)-suffices
	 */
	protected String id;
	
	/**
	 * The filetype (speaker, mapping, ...)
	 */
	protected String fileType;
	//protected FileType fileType;

	/**
	 * wav / mp4 / jpg / txt
	 */
	protected String extension;
	
	/**
	 * vnd.wave (only used by Recording)
	 */
	protected String format;
	//protected FileFormat format;
}
