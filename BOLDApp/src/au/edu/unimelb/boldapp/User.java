package au.edu.unimelb.boldapp;

import java.util.List;
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
	 * The user's languages
	 */
	private List<Language> languages;

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

	/**
	 * Constructor that allows for languages to be specified.
	 *
	 * @param	uuid	The new user's UUID.
	 * @param	name	The new user's name.
	 */
	public User(UUID uuid, String name, List<Language> languages) {
		setUuid(uuid);
		setName(name);
		setLanguages(languages);
	}

	/**
	 * Languages accessor.
	 */
	public List<Language> getLanguages() {
		return languages;
	}

	/**
	 * Languages mutator.
	 */
	public void setLanguages(List<Language> languages) { 
		this.languages = languages;
	}

	/**
	 * uuid mutator
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
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
	 * name accessor
	 */
	public String getName() {
		return this.name;
	}
}
