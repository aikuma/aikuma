/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.model;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.IdUtils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The file modeled from the viewpoint of GoogleCloud
 * (The parent class of Recording and Speaker / 
 * This can encapsulate the transcript/mapping as well)
 *
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class FileModel implements Parcelable {

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
	
	/**
	 * A list of file-types 
	 */
	//public enum FileType { RECORDING, SPEAKER, OTHER };
	public enum FileType { SOURCE, RESPEAKING, TRANSLATION, SPEAKER, METADATA, PREVIEW, TRANSCRIPT, MAPPING };
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
	
	/**
	 * Constructor of FileModel
	 * 
	 * @param versionName	versionName of the file-format
	 * @param ownerId		OwnerID(UserID) of the file
	 * @param id			Id of the file
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
		String[] splitCloudId = cloudIdentifier.split("\\/");
		
		int index = splitCloudId[6].lastIndexOf('.');
		String fileName = splitCloudId[6].substring(0, index);
		String ext = splitCloudId[6].substring(index+1);
		
		String[] splitName = fileName.split("-");
		String fileType = splitName[splitName.length-1];
		
		if(StringUtils.isNumeric(fileType))		//Derivative recordings(respeaking, interpret)
			fileType = splitName[splitName.length-2];
		
		if(fileType.equals("small")) {	//speaker small image
			return new FileModel(splitCloudId[0], splitCloudId[3], splitCloudId[5], SPEAKER_TYPE, ext);
		} else if(fileType.equals(METADATA_TYPE)) {
			return null;
		} else if(fileTypeSet.contains(fileType)) {
			return new FileModel(splitCloudId[0], splitCloudId[3], fileName, fileType, ext);
		} else {
			return null;
		}
		
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
		case SOURCE:
			break;
		case RESPEAKING:
			break;
		case TRANSLATION:
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
			return (id + "-image-small.jpg");
		} else {
			return (id + "." + getExtension());
		}
	}
	
	/**
	 * Getter of the file's metadata ID + extension
	 * @return	The metadata ID + extension of the file-model
	 */
	public String getMetadataIdExt() {
		if(fileType.equals(SPEAKER_TYPE) || fileType.equals(SOURCE_TYPE) || 
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
		
		String ownerIdDirName = IdUtils.getOwnerDirName(ownerId);
		String ownerDirStr = (versionName + "/" + 
				ownerIdDirName.substring(0, 1) + "/" + 
				ownerIdDirName.substring(0, 2) + "/" + ownerId + "/");
		
		String suffix;
		if(fileType.equals(SPEAKER_TYPE)) {
			if(option == 0)
				suffix = "-image-small.jpg";
			else
				suffix = getSuffixExt(versionName, FileType.METADATA);
			
			return (ownerDirStr + Speaker.PATH + getId() + "/" + getId() + suffix);
		} else {
			if(option == 0)
				suffix = "." + getExtension();
			else if(!(fileType.equals(SOURCE_TYPE) || fileType.equals(RESPEAKING_TYPE)
					|| fileType.equals(TRANSLATION_TYPE)))	// No metadata for transcript/mapping/preview
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
		if(fileType.equals(SPEAKER_TYPE)) {
			itemPath = new File(
					FileIO.getOwnerPath(versionName, ownerId), Speaker.PATH);
			itemPath.mkdirs();
			
			if(option == 0)
				suffix = "-image-small.jpg";
			else
				suffix = getSuffixExt(versionName, FileType.METADATA);
			
			return new File(itemPath, getId() + "/" + getId() + suffix);
		} else {
			itemPath = new File(
					FileIO.getOwnerPath(versionName, ownerId), Recording.PATH);
			itemPath.mkdirs();
			
			if(option == 0)
				suffix = "." + getExtension();
			else if(!(fileType.equals(SOURCE_TYPE) || fileType.equals(RESPEAKING_TYPE)
					|| fileType.equals(TRANSLATION_TYPE)))	// No metadata for transcript/mapping/preview
				return null;
			else
				suffix = getSuffixExt(versionName, FileType.METADATA);
			String groupId = getId().split("-")[0];
			
			return new File(itemPath, groupId + "/" + getId() + suffix);
		}
		
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
