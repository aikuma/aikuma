package edu.au.unimelb.boldapp;

import java.util.UUID;

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
	 * uuid mutator
	 */
	public setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * creator mutator
	 */
	public setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * name mutator
	 */
	public setName(String name) {
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
}
