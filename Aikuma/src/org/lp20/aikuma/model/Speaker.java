/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.model;

import android.graphics.Bitmap;
import android.os.Parcelable;
import android.os.Parcel;
import android.util.Log;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.IdUtils;
import org.lp20.aikuma.util.ImageUtils;
import org.lp20.aikuma.util.StandardDateFormat;

/**
 * The class that stores the data pertaining to a speaker who has contributed
 * to a recording.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Speaker extends FileModel implements Comparable<Speaker> {

	// String tag for debugging
	private static final String TAG = "Speaker";
	
	/**
	 * The constructor used when first creating a Speaker.
	 *
	 * Note that it doesn't include an ID argument, as that will be generated.
	 * For any tasks involving reading Speakers, use the constructor that takes
	 * an ID argument
	 *
	 * @param	name		The name of the speaker
	 * @param	comments	The optional free text string
	 * @param	date		The date of creation.
	 * @param	versionName	The speaker-metadata's version(v0x)
	 * @param	ownerId	The speaker owner's ID(Google account)
	 */
	public Speaker(String name, String comments, Date date,
			String versionName, String ownerId) {
		super(versionName, ownerId, null, SPEAKER_TYPE, IMAGE_EXT);
		setName(name);
		setComments(comments);
		setDate(date);
		setId(createId(name));
		setVersionName(versionName);
		setOwnerId(ownerId);
	}

	/**
	 * The constructor used when reading an existing speaker.
	 *
	 * @param	name	The name of the speaker
	 * @param	id	The 8+ char string identifier of the speaker.
	 * @param versionName	Current aikuma's version
	 * @param ownerId		Current user's ID
	 */
	public Speaker(String name, String id,
			String versionName, String ownerId) {
		super(versionName, ownerId, null, SPEAKER_TYPE, IMAGE_EXT);
		setName(name);
		setId(id);
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
	 * Encodes the Speaker object as a corresponding JSONObject.
	 *
	 * @return	A JSONObject instance representing the Speaker.
	 */
	public JSONObject encode() {
		JSONObject encodedSpeaker = new JSONObject();
		encodedSpeaker.put(NAME_KEY, this.name);
		encodedSpeaker.put(COMMENTS_KEY, this.comments);
		encodedSpeaker.put(DATE_KEY, new StandardDateFormat().format(this.date));
		encodedSpeaker.put(SPEAKER_ID_KEY, this.id);
		encodedSpeaker.put(VERSION_KEY, this.versionName);
		encodedSpeaker.put(USER_ID_KEY, this.ownerId);
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
				"/" + getId() + getSuffixExt(versionName, FileType.METADATA)), encodedSpeaker);
	}

	/**
	 * Read a Speaker from the file containing the JSON describing the speaker
	 *
	 * @param verName	The speaker-data's version
	 * @param ownerAccount	Account ID of the speaker's owner
	 * @param	id	The ID of the speaker.
	 * @return	A Speaker object corresponding to the given speaker ID.
	 * @throws	IOException	If the speaker metadata cannot be read from file.
	 */
	public static Speaker read(String verName, String ownerAccount, 
			String id) throws IOException {
		File ownerDir = FileIO.getOwnerPath(verName, ownerAccount);
		JSONObject jsonObj = FileIO.readJSONObject(
				new File(getSpeakersPath(ownerDir), id + "/" + id + getSuffixExt(verName, FileType.METADATA)));
		String name = (String) jsonObj.get(NAME_KEY);
		String versionName = (String) jsonObj.get(VERSION_KEY);
		String ownerId = (String) jsonObj.get(USER_ID_KEY);
		return new Speaker(name, /*languages, */id, versionName, ownerId);
	}

	/**
	 * Read all users from file
	 *
	 * @return	A list of the users found in the users directory.
	 *//*
	public static List<Speaker> readAll() {
		// Get the user data from the metadata.json files.
		List<Speaker> speakers = new ArrayList<Speaker>();

		// Get a list of all the IDs of users in the "users" directory.
		String[] speakerIDArray = getSpeakersPath().list();
		if (speakerIDArray == null) {
			return speakers;
		}

		List<String> speakerIDs = Arrays.asList(speakerIDArray);
		
		for (String speakerID : speakerIDs) {
			try {
				speakers.add(Speaker.read(speakerID));
			} catch (IOException e) {
				// Couldn't read that user for whatever reason (perhaps JSON
				// file wasn't formatted correctly). Lets just ignore that user.
			}
		}
		return speakers;
	}*/
	
	/**
	 * Read all speakers
	 *
	 * @return	A list of all speakers in the Aikuma directory.
	 */
	public static List<Speaker> readAll() {
		return readAll(null);
	}

	/**
	 * Read all speakers of the user
	 *
	 * @param userId	The user's ID
	 * @return	A list of all the user's speakers in the Aikuma directory.
	 */
	public static List<Speaker> readAll(String userId) {
		List<Speaker> speakers = new ArrayList<Speaker>();

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
			//File[] firstHashDirs = f1.listFiles();
			//for(File f2 : firstHashDirs) {
				//File[] secondHashDirs = f2.listFiles();
				//for(File f3 : secondHashDirs) {
					File[] ownerIdDirs = f1.listFiles();
					for(File f : ownerIdDirs) {
						String dirName = f.getName();
						if(dirName.equals(AdminFileModel.ADMIN))
							continue;
						
						Log.i(TAG, "readAll: " + f.getPath());
						
						if(userId == null || dirName.equals(userId))
							addSpeakersInDir(speakers, f, f1.getName(), f.getName());
					}
				//}
			//}
		}

		return speakers;
	}

	private static void addSpeakersInDir(List<Speaker> speakers, File dir, 
			String verName, String ownerAccount) {
		// Constructs a list of directories in the speakers directory.
		List<String> speakerIDs = Arrays.asList(getSpeakersPath(dir).list());
		
		for (String speakerID : speakerIDs) {
			try {
				speakers.add(Speaker.read(verName, ownerAccount, speakerID));
			} catch (IOException e) {
				// Couldn't read that user for whatever reason (perhaps JSON
				// file wasn't formatted correctly). Lets just ignore that user.
				Log.e(TAG, "read exception: " + speakerID);
			}
		}
		
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
				//.append(id, rhs.id)
				.append(name, rhs.name)
				.append(ownerId, rhs.ownerId)
				.isEquals();
	}

	/**
	 * Compares the given speaker with this speaker
	 *
	 * @param	that	Speaker object compared with this object
	 * @return			compare result of speaker
	 */
	public int compareTo(Speaker that) {
		return name.compareTo(that.getName());
	}
	
	/**
	 * Provides a string representation of the speaker.
	 *
	 * @return	A string representation of the Speaker
	 */
	public String toString() {
		String s = getId().toString() + ", " + getName();// + ", " + getLanguages().toString();
		return s;
	}
	 
	 /**
	  * Get the application's speakers directory
	  * 
	  * @param ownerDir	A File representing the path of owner's directory
	  * @return	A File representing the path of the recordings directory
	  */
	 public static File getSpeakersPath(File ownerDir) {
		File path = new File(ownerDir, PATH);
		path.mkdirs();
		return path;
	 }

	 /**
	  * Get the speaker owner's directory
	  * 
	  * @return	A file representing the path of the speaker owner's dir
	  */
	 private File getSpeakersPath() {
			File path = new File(
					FileIO.getOwnerPath(versionName, ownerId), PATH);
			path.mkdirs();
			return path;
	 }
	
	/*	
	private void setId(String id) {
		Log.i("setId", "set Id: " + id);
		this.id = id;
	}*/

	/**
	 * Sets the name of the Speaker.
	 *
	 * @param	name	A String object representing the Speaker's name.
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
		out.writeString(versionName);
		out.writeString(ownerId);
		out.writeString(id.toString());
		out.writeString(name);
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
		super(in);
		setId(in.readString());
		setName(in.readString());
		List<Language> languages = new ArrayList<Language>();
		in.readTypedList(languages, Language.CREATOR);
	}

	// Creates a purely numeric speaker ID
	private String createId(String name) {
		// Generate 12 random uppercase alphabets.
		return IdUtils.sampleFromAlphabet(SPEAKER_ID_LEN, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
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
	 * The name of the Speaker.
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

	
	/**
	 * Relative path where speaker files are stored
	 */
	public static final String PATH = "speakers/";
	/** the length of speaker_id */
	public static final int SPEAKER_ID_LEN = 12;
	
	/**
	 * Keys of the speaker metadata fields
	 */
	public static final String NAME_KEY = "name";
	/** */
	public static final String COMMENTS_KEY = "comments";
	/** */
	public static final String DATE_KEY = "date";
	/** */
	public static final String SPEAKER_ID_KEY = "id";
	
	/** 
	 * Keys used/tweaked in cloud fullText metadata fields
	 */
	public static final String SPEAKER_ID_PREFIX_KEY = "spk_pre";
	
	private static Set<String> fieldKeySet;
	static {
		fieldKeySet = new HashSet<String>();
		fieldKeySet.add(NAME_KEY);
		fieldKeySet.add(COMMENTS_KEY);
		fieldKeySet.add(DATE_KEY);
		fieldKeySet.add(SPEAKER_ID_KEY);
		fieldKeySet.add(VERSION_KEY);
		fieldKeySet.add(USER_ID_KEY);
	}
}
