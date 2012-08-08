package au.edu.unimelb.boldapp;

import java.util.UUID;

/**
 * The class that contains user data.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class User {

	/**
	 * The user's UUID.
	 */
	private UUID uuid;
	/**
	 * The user's name.
	 */
	private String name;

	/**
	 * Default constructor.
	 *
	 * @param	uuid	The new user's UUID.
	 * @param	name	The new user's name.
	 */
	public User(UUID uuid, String name) {
		setUuid(uuid);
		setName(name);
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getUuid() {
		return this.uuid;
	}
	public String getName() {
		return this.name;
	}
}
