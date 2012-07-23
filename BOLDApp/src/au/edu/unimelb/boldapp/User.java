package au.edu.unimelb.boldapp;

import java.util.UUID;

public class User {

	private UUID uuid;
	private String name;

	public User(UUID uuid, String name) {
		setUuid(uuid);
		this.name = name;
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
