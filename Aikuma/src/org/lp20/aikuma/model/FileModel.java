package org.lp20.aikuma.model;

import java.io.File;

import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.IdUtils;

import android.os.Parcel;
import android.os.Parcelable;

public class FileModel implements Parcelable {

	protected static final String metadataSuffix = "-metadata.json";
	
	protected static final String mappingSuffix = "-mapping.txt";
	
	protected static final String transcriptSuffix = "-transcript.txt";
	
	public FileModel(
			String versionName, String ownerId, String id, String type, String format) {
		setVersionName(versionName);
		setOwnerId(ownerId);
		this.id = id;
		this.fileType = type;
		this.format = format;
	}
	
	/**
	 * Constructor for parcel (only used by Speaker)
	 * @param in
	 */
	protected FileModel(Parcel in) {
		String versionName = in.readString();
		String ownerId = in.readString();
		setVersionName(versionName);
		setOwnerId(ownerId);
		this.fileType = "speaker";
		this.format = "jpg";
	}
	
	
	public String getId() {
		return id;
	}
	
	public String getVerIdFormat() {
		if(fileType.equals("speaker")) {
			String verId = getVersionName() + "-" + getId() + "-" + getOwnerId() + getExtension();
			return verId;
		} else {	
			// recording-type (source, respeaking, translation) and other-type (mapping, transcript)
			String verId = getVersionName() + "-" + getId() + getExtension();
			return verId;
		}
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
				suffix = metadataSuffix;
			
			return (ownerDirStr + Speaker.PATH + getId() + "/" + getId() + suffix);
		} else {
			if(option == 0)
				suffix = getExtension();
			else if(format.equals("txt"))
				return null;
			else
				suffix = metadataSuffix;
			String groupId = getId().split("-")[0];
			
			return (ownerDirStr + Recording.PATH + groupId + "/" + getId() + suffix);
		}
	}
	
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
				suffix = metadataSuffix;
			
			return new File(itemPath, getId() + "/" + getId() + suffix);
		} else {
			itemPath = new File(
					FileIO.getOwnerPath(versionName, ownerId), Recording.PATH);
			itemPath.mkdirs();
			
			if(option == 0)
				suffix = getExtension();
			else if(format.equals("txt"))
				return null;
			else
				suffix = metadataSuffix;
			String groupId = getId().split("-")[0];
			
			return new File(itemPath, groupId + "/" + getId() + suffix);
		}
		
	}

	public String getFileType() {
		return fileType;
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
	
	protected String fileType;

	// vnd.wave / mp4 / jpg / txt
	protected String format;
}
