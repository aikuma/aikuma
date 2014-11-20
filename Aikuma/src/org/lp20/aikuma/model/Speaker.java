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
import java.util.List;
import java.util.Random;
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

/**
 * The class that stores the data pertaining to a speaker who has contributed
 * to a recording.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Speaker implements Parcelable{

	// String tag for debugging
	private static final String TAG = "Speaker";
	
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
	 * @param	versionName	The speaker-metadata's version(v0x)
	 * @param	ownerId	The speaker owner's ID(Google account)
	 */
	public Speaker(UUID imageUUID, String name, List<Language> languages,
			String versionName, String ownerId) {
		this.imageUUID = imageUUID;
		setName(name);
		setId(createId(name));
		setLanguages(languages);
		setVersionName(versionName);
		setOwnerId(ownerId);
	}

	private void importImage(UUID imageUUID) throws IOException {
		// First import the full sized image
		File imageFile = ImageUtils.getNoSyncImageFile(imageUUID);
		FileUtils.moveFile(imageFile,
				new File(getSpeakersPath(),
						getId() + "/" + getId() + "-image.jpg"));

		// Then import the small image
		File smallImageFile = ImageUtils.getNoSyncSmallImageFile(imageUUID);
		FileUtils.moveFile(smallImageFile,
				new File(getSpeakersPath(),
						getId() + "/" + getId() + "-image-small.jpg"));
	}

	/**
	 * The constructor used when reading an existing speaker.
	 *
	 * @param	name	The name of the speaker
	 * @param	languages	A list of languages of the speaker.
	 * @param	id	The 8+ char string identifier of the speaker.
	 * @param versionName	Current aikuma's version
	 * @param ownerId		Current user's ID
	 */
	public Speaker(String name, List<Language> languages, String id,
			String versionName, String ownerId) {
		setName(name);
		setLanguages(languages);
		setId(id);
		setVersionName(versionName);
		setOwnerId(ownerId);
	}

	public String getId() {
		return this.id;
	}
	
	public String getOwnerId() {
		return ownerId;
	}
	
	public String getVersionName() {
		return versionName;
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
	 * Get the Speaker's image as a File.
	 * @return A File containing the Speaker's image.
	 */
	public File getImageFile() {
		return new File(getSpeakersPath(), getId() + "/" + getId() + "-image.jpg");
	}
	
	/**
	 * Get the small version of Speaker's image as a File.
	 * @return A File containing the Speaker's small image.
	 */
	public File getSmallImageFile() {
		return new File(getSpeakersPath(), getId() + "/" + getId() + "-image-small.jpg");
	}
	
	/**
	 * Get the Speaker's json metadata as a File
	 * @return A File containing the Speaker's json metadata
	 */
	public File getMetadataFile() {
		return new File(getSpeakersPath(), getId() + "/" + getId() + "-metadata.json");
	}

	/**
	 * Returns a identifier used in cloud-storage
	 * @param option	0: image, 1: small-image, 2: metadata
	 * @return			a relative-path of speaker to 'aikuma/'
	 */
	public String getCloudIdentifier(int option) {
		if(option < 0 || option > 2)
			return null;
		
		String suffix = "";
		if(option == 0)
			suffix = "-image.jpg";
		else if(option == 1)
			suffix = "-image-small.jpg";
		else
			suffix = "-metadata.json";
		
		String ownerIdDirName = IdUtils.getOwnerDirName(ownerId);
		String ownerDirStr = (versionName + "/" + 
				ownerIdDirName.substring(0, 1) + "/" + 
				ownerIdDirName.substring(0, 2) + "/" + ownerId + "/");
		return (ownerDirStr + PATH + getId() + "/" + getId() + suffix);
	}
	
	/**
	 * Gets the Speaker's image.
	 *
	 * @return	A Bitmap object.
	 * @throws	IOException	If the image cannot be retrieved.
	 */
	public Bitmap getImage() throws IOException {
		return ImageUtils.retrieveFromFile(getImageFile());
	}

	/**
	 * Gets the small version of the Speaker's image.
	 *
	 * @return	A Bitmap object.
	 * @throws	IOException	If the image cannot be retrieved.
	 */
	public Bitmap getSmallImage() throws IOException {
		return ImageUtils.retrieveFromFile(getSmallImageFile());
	}

	/**
	 * Gets the small version of the Speaker's image.
	 *
	 * @param	verName		The version name of the recording
	 * @param	ownerAccount OwnerID of the recording
	 * @param	speakerId	The ID of the speaker whose image is to be fetched.
	 * @return	A Bitmap object.
	 * @throws	IOException	If the image cannot be retrieved.
	 */
	public static Bitmap getSmallImage(String verName, String ownerAccount,
			String speakerId) throws IOException {
		File ownerDir = FileIO.getOwnerPath(verName, ownerAccount);
		return ImageUtils.retrieveFromFile(
				new File(getSpeakersPath(ownerDir),
						speakerId + "/" + speakerId + "-image-small.jpg"));
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
		encodedSpeaker.put("version", this.versionName);
		encodedSpeaker.put("user_id", this.ownerId);
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
		// Move the image to the Speaker directory with an appropriate name
		importImage(imageUUID);

		JSONObject encodedSpeaker = this.encode();

		FileIO.writeJSONObject(new File(getSpeakersPath(), getId() +
				"/" + getId() + "-metadata.json"), encodedSpeaker);
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
				new File(getSpeakersPath(ownerDir), id + "/" + id + "-metadata.json"));
		String name = (String) jsonObj.get("name");
		JSONArray languageArray = (JSONArray) jsonObj.get("languages");
		if (languageArray == null) {
			throw new IOException("Null languages in the JSON file.");
		}
		List<Language> languages = Language.decodeJSONArray(languageArray);
		String versionName = (String) jsonObj.get("version");
		String ownerId = (String) jsonObj.get("user_id");
		return new Speaker(name, languages, id, versionName, ownerId);
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
	 * Read current-user's all speakers from file
	 *
	 * @return	A list of all the speakers in the Aikuma directory.
	 */
	public static List<Speaker> readAll() {
		String ownerId = AikumaSettings.getCurrentUserId();
		List<Speaker> speakers = new ArrayList<Speaker>();

		// Get a list of version directories
		File[] versionDirs = 
				FileIO.getAppRootPath().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.startsWith("v") && filename.substring(1).matches("\\d+");
			}	
		});

		for(File f1 : versionDirs) {
			File[] firstHashDirs = f1.listFiles();
			for(File f2 : firstHashDirs) {
				File[] secondHashDirs = f2.listFiles();
				for(File f3 : secondHashDirs) {
					File[] ownerIdDirs = f3.listFiles();
					for(File f : ownerIdDirs) {
						Log.i(TAG, "readAll: " + f.getPath());
						
						if(f.getName().equals(ownerId))
							addSpeakersInDir(speakers, f, f1.getName(), ownerId);
					}
				}
			}
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
	
	// Sets the versionName(v0x)
	private void setVersionName(String versionName) {
		this.versionName = versionName;
	}
		
	// Sets the ownerId(Google account)
	private void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
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
		out.writeString(versionName);
		out.writeString(ownerId);
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
		setVersionName(in.readString());
		setOwnerId(in.readString());
	}

	// Creates a purely numeric speaker ID
	private String createId(String name) {
		// Generate 12 random uppercase alphabets.
		return IdUtils.sampleFromAlphabet(12, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
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
	 * The speaker's format version
	 */
	private String versionName;
	
	/**
	 * The speaker's owner ID
	 */
	private String ownerId;
	
	/**
	 * The languages of the Speaker.
	 */
	private List<Language> languages;

	// The temporary UUID of the image before it gets renamed appropriately.
	private UUID imageUUID;
	
	/**
	 * Relative path where speaker files are stored
	 */
	public static final String PATH = "speakers/";
}
