package au.edu.melbuni.boldapp.models;

import java.util.Map;

import org.json.simple.JSONValue;

import au.edu.melbuni.boldapp.persisters.Persister;

public abstract class Model {
	
	public static Model fromHash(Persister persister, Map<String, Object> hash) {
		return null;
	}
	public abstract Map<String, Object> toHash();
	
	@SuppressWarnings({ "unchecked" })
	public static Model fromJSON(Persister persister, String data) {
		return fromHash(persister, (Map<String, Object>) JSONValue.parse(data));
	}
	public String toJSON() {
		return JSONValue.toJSONString(toHash());
	}
	
}
