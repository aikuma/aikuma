package au.edu.unimelb.boldapp;

import java.util.UUID;
import java.util.Date;

/**
 * Class that stores a recording's metadata information as in the JSON file.
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
	private User creator;

	/**
	 * The name of the recording
	 */
	private String name;

	/**
	 * The date and time of creation
	 */
	private Date date;

	/**
	 * Default Constructor
	 */
	 public Recording(UUID uuid, User creator, String name , Date date) {
	 	setUuid(uuid);
		setCreator(creator);
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
	 * creator mutator
	 */
	public void setCreator(User creator) {
		this.creator = creator;
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
	 * creator accessor
	 */
	public User getCreator() {
		return this.creator;
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
