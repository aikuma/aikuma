package au.edu.melbuni.boldapp;

import au.edu.melbuni.boldapp.persisters.Persister;

/*
 * TODO This class needs to be removed. It's just for demo purposes.
 */
public class Demo {

	public static String getSoundfilePathWithoutExtension() {
		return Persister.getBasePath()
				+ "timelines/" + getUUIDString() + "/segments/0";
	}
	
	public static String getUUIDString() {
//		return "3711a772-078c-4cfe-a2ab-4d4a7dce2d06";
		return "00000000-0000-0000-0000-000000000000";
	}
	
}
