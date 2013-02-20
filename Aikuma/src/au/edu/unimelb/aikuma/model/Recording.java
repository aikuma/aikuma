package au.edu.unimelb.aikuma.model;

import java.util.Date;
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
}
