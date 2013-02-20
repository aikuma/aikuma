package au.edu.unimelb.aikuma.model;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The class that stores the metadata of a recording, including it's UUID,
 * creator's UUID, name, date, originalUUID (if applicable), and languages.
 */
public class Recording {

	/**
	 * UUID accessor.
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Name accessor.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Date accessor.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * creatorUUID accessor.
	 */
	public UUID getCreatorUUID() {
		return creatorUUID;
	}

	/**
	 * originalUUID accessor.
	 */
	public UUID getOriginalUUID() {
		return originalUUID;
	}

	/**
	 * languages accessor.
	 */
	public List<Language> getLanguages() {
		return languages;
	}

	/**
	 * UUID mutator.
	 */
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * Name mutator.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Date mutator.
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * creatorUUID mutator.
	 */
	public void setCreatorUUID(UUID creatorUUID) {
		this.creatorUUID = creatorUUID;
	}

	/**
	 * originalUUID mutator.
	 */
	public void setOriginalUUID(UUID originalUUID) {
		this.originalUUID = originalUUID;
	}

	/**
	 * languages mutator.
	 */
	public void setLanguages(List<Language> languages) {
		this.languages = languages;
	}

	/**
	 * The minimal constructor
	 *
	 * @param	uuid	the recording's UUID.
	 * @param	name	The recording's name.
	 * @param	date	The date of creation.
	 */
	public Recording(UUID uuid, String name, Date date) {
		setUUID(uuid);
		setName(name);
		setDate(date);
		setLanguages(new ArrayList<Language>());
	}

	/**
	 * The recording's UUID.
	 */
	private UUID uuid;

	/**
	 * The recording's name.
	 */
	private String name;

	/**
	 * The recording's date.
	 */
	private Date date;

	/**
	 * The UUID of the creator of the recording
	 */
	private UUID creatorUUID;

	/**
	 * The UUID of the original of the recording if it is a respeaking.
	 */
	private UUID originalUUID;

	/**
	 * The languages of the recording.
	 */
	private List<Language> languages;
}
