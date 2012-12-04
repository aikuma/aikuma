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
	private UUID originalUuid;

	/**
	 * Default Constructor
	 */
	 public Recording(UUID uuid, UUID creatorUUID, String name, Date date, UUID
			 originalUuid) {
	 	setUuid(uuid);
		setCreatorUUID(creatorUUID);
		setName(name);
		setDate(date);
		setOriginalUuid(originalUuid);
	 }

	/**
	 * Default Constructor
	 */
	 public Recording(UUID uuid, UUID creatorUUID, String name, Date date) {
	 	setUuid(uuid);
		setCreatorUUID(creatorUUID);
		setName(name);
		setDate(date);
	 }

	/**
	 * uuid mutator
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * OriginalUUID mutator
	 */
	public void setOriginalUuid(UUID originalUuid) {
		this.originalUuid = originalUuid;
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
		return this.originalUuid;
	}

	/**
	 * originalUUID accessor
	 */
	public UUID getOriginalUUID() {
		return this.originalUuid;
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
}
