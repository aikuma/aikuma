/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.model;

import java.io.File;

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
	public static final String METADATA_SUFFIX = "-metadata.json";
	/** */
	public static final String MAPPING_SUFFIX = "-mapping.txt";
	/** */
	public static final String TRANSCRIPT_SUFFIX = "-transcript.txt";
	/** */
	public static final String SAMPLE_SUFFIX = "-preview.wav";
	
	/**
	 * Constructor of FileModel
	 * 
	 * @param versionName	versionName of the file-format
	 * @param ownerId		OwnerID(UserID) of the file
	 * @param id			Id of the file
	 * @param type			Type of the file(speaker, mapping, ...)
	 * @param format		Format of the file(jpg, mp4, wav, json, txt)
	 */
	public FileModel(
			String versionName, String ownerId, String id, String type, String format) {
		setVersionName(versionName);
		setOwnerId(ownerId);
		this.id = id;
		this.fileType = type;
		this.format = format;
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
		
		if(ext.equals("json"))			//metadata
			return null;
		else if(ext.equals("jpg")) {	//speaker small image
			return new FileModel(splitCloudId[0], splitCloudId[3], splitCloudId[5], "speaker", ext);
		} else if(ext.equals("txt")) {	//mapping, transcript
			String[] splitName = fileName.split("-");
			return new FileModel(splitCloudId[0], splitCloudId[3], fileName, splitName[splitName.length-1], ext);
		} else {						//source, respeaking, preview
			String[] splitName = fileName.split("-");
			if(splitName[splitName.length-1].equals("preview")) {
				return new FileModel(splitCloudId[0], splitCloudId[3], fileName, "preview", ext);
			} else {
				return new FileModel(splitCloudId[0], splitCloudId[3], fileName, splitName[2], ext);
			}
		}
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
		this.fileType = "speaker";
		this.format = "jpg";
	}
	
	/**
	 * Getter of the file ID
	 * @return	the ID of this file-model
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Getter of the file ID + extension (used in GoogleCloudService)
	 * @return	The ID + extension of the file-model
	 */
	public String getIdExt() {
		if(fileType.equals("speaker")) {
			return (id + "-image-small.jpg");
		} else {
			return (id + getExtension());
		}
	}
	
	/**
	 * Getter of the file's metadata ID + extension
	 * @return	The metadata ID + extension of the file-model
	 */
	public String getMetadataIdExt() {
		if(format.equals("jpg") || (format.equals("wav") && !fileType.equals("preview")))
			return (id + METADATA_SUFFIX);
		return null;
	}
	
	/**
	 * Returns an identifier used in cloud-storage
	 * @param option	0: file(small-image/recording), 1: metadata
	 * @return	a relative-path of recording to 'aikuma/'
	 */
	public String getCloudIdentifier(int option) {
		if(option != 0 && option != 1)
			return null;
		
		String ownerIdDirName = IdUtils.getOwnerDirName(ownerId);
		String ownerDirStr = (versionName + "/" + 
				ownerIdDirName.substring(0, 1) + "/" + 
				ownerIdDirName.substring(0, 2) + "/" + ownerId + "/");
		
		String suffix;
		if(fileType.equals("speaker")) {
			if(option == 0)
				suffix = "-image-small.jpg";
			else
				suffix = METADATA_SUFFIX;
			
			return (ownerDirStr + Speaker.PATH + getId() + "/" + getId() + suffix);
		} else {
			if(option == 0)
				suffix = getExtension();
			else if(format.equals("txt") || fileType.equals("preview"))	// No metadata for transcript/mapping/preview
				return null;
			else
				suffix = METADATA_SUFFIX;
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
		if(fileType.equals("speaker")) {
			itemPath = new File(
					FileIO.getOwnerPath(versionName, ownerId), Speaker.PATH);
			itemPath.mkdirs();
			
			if(option == 0)
				suffix = "-image-small.jpg";
			else
				suffix = METADATA_SUFFIX;
			
			return new File(itemPath, getId() + "/" + getId() + suffix);
		} else {
			itemPath = new File(
					FileIO.getOwnerPath(versionName, ownerId), Recording.PATH);
			itemPath.mkdirs();
			
			if(option == 0)
				suffix = getExtension();
			else if(format.equals("txt") || fileType.equals("preview"))	// No metadata for transcript/mapping/preview
				return null;
			else
				suffix = METADATA_SUFFIX;
			String groupId = getId().split("-")[0];
			
			return new File(itemPath, groupId + "/" + getId() + suffix);
		}
		
	}

	public String getFileType() {
		return fileType;
	}
	
	public String getFormat() {
		return format;
	}
	
	public String getOwnerId() {
		return ownerId;
	}
	
	public String getVersionName() {
		return versionName;
	}
	
	// Sets the versionName(v0x)
	protected void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	
	// Sets the ownerId(Google account)
	protected void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}	
	
	private String getExtension() {
		if(format.equals("mp4")) {
			return ".mp4";
		} else if(format.equals("jpg")) {
			return ".jpg";
		} else if(format.equals("txt")) {
			return ".txt";
		} else {
			return ".wav";
		}
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
	 */
	protected String id;
	
	/**
	 * The filetype (speaker, mapping, ...)
	 */
	protected String fileType;

	/**
	 * vnd.wave / mp4 / jpg / txt
	 */
	protected String format;
}
