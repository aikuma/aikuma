package au.edu.unimelb.boldapp;

import java.util.UUID;
import java.util.Date;

/**
 * Class that stores a recording's metadata information.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Recording {
	/**
	 * UUID of the recording.
	 */
	private UUID uuid;

	/**
	 * UUID of the user who created the recording.
	 */
	private UUID creatorUUID;

	/**
	 * The name of the recording
	 */
	private String name;

	/**
	 * The date and time of creation
	 */
	private Date date;

	/**
	 * The recording that this recording is a respeaking of (if applicable;
	 * null otherwise)
	 */
	private UUID originalUUID;

	/**
	 * The language of the recording
	 */
	private Language language;

	public void setLanguage(Language language) {
		this.language = language;
	}

	public Language getLanguage() {
		return this.language;
	}

	/**
	 * Default Constructor
	 */
	public Recording(){
	}

	/**
	 * Alternative Constructor
	 */
	public Recording(UUID uuid, UUID creatorUUID, String name, Date date,
			Language language, UUID originalUUID) {
	 	setUuid(uuid);
		setCreatorUUID(creatorUUID);
		setName(name);
		setDate(date);
		setLanguage(language);
		setOriginalUuid(originalUUID);
	 }

	/**
	 * Alternative Constructor
	 */
	public Recording(UUID uuid, UUID creatorUUID, String name, Date date,
			Language language) {
		setUuid(uuid);
		setCreatorUUID(creatorUUID);
		setName(name);
		setDate(date);
		setLanguage(language);
	}

	/**
	 * uuid mutator
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * uuid mutator
	 */
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * OriginalUUID mutator
	 */
	public void setOriginalUuid(UUID originalUUID) {
		this.originalUUID = originalUUID;
	}

	/**
	 * OriginalUUID mutator
	 */
	public void setOriginalUUID(UUID originalUUID) {
		this.originalUUID = originalUUID;
	}

	/**
	 * creator mutator
	 */
	public void setCreatorUUID(UUID creatorUUID) {
		this.creatorUUID = creatorUUID;
	}

	/**
	 * name mutator
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * uuid accessor
	 */
	public UUID getUuid() {
		return this.uuid;
	}

	/**
	 * uuid accessor
	 */
	public UUID getUUID() {
		return this.uuid;
	}

	/**
	 * originalUUID accessor
	 */
	public UUID getOriginalUuid() {
		return this.originalUUID;
	}

	/**
	 * originalUUID accessor
	 */
	public UUID getOriginalUUID() {
		return this.originalUUID;
	}

	/**
	 * Tells us whether the audio is an original or a speaking/interpretation
	 * @return	true if original; false otherwise.
	 */
	public boolean isOriginal() {
		return (this.getOriginalUuid() == null);
	}

	/**
	 * creator accessor
	 */
	public UUID getCreatorUUID() {
		return this.creatorUUID;
	}

	/**
	 * name accessor
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * date mutator
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * date accessor
	 */
	public Date getDate() {
		return this.date;
	}

	public boolean hasUUID() {
		if (this.uuid != null) {
			return true;
		}
		return false;
	}

	public boolean hasName() {
		if (this.name != null) {
			return true;
		}
		return false;
	}

	public boolean hasCreatorUUID() {
		if (this.creatorUUID != null) {
			return true;
		}
		return false;
	}

	public boolean hasOriginalUUID() {
		if (this.originalUUID != null) {
			return true;
		}
		return false;
	}

	public boolean hasDate() {
		if (this.date != null) {
			return true;
		}
		return false;
	}

	public boolean hasLanguage() {
		if (this.language != null) {
			return true;
		}
		return false;
	}
}
