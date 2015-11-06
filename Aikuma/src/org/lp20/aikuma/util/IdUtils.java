package org.lp20.aikuma.util;

import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Offers methods to create random sequences of characters.
 * Offers methods to create an ID for a specific string
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class IdUtils {

	/**
	 * Creates a random digit string of length n
	 *
	 * @param	n	The number of digits long the string is to be.
	 * @return	A string of random digits of length n.
	 */
	public static String randomDigitString(int n) {
		Random rng = new Random();
		StringBuilder randomDigits = new StringBuilder();
		for (int i = 0; i < n; i++) {
			randomDigits.append(rng.nextInt(10));
		}
		return randomDigits.toString();
	}

	/**
	 * Randomly generate a string of length k from a given alphabet
	 *
	 * @param	k	The amount of characters to sample
	 * @param	alphabet	The string of characters to sample from.
	 * @return	A sampling of k characters from alphabet
	 */
	public static String sampleFromAlphabet(
			final int k, final String alphabet) {
		final int n = alphabet.length();

		Random rng = new Random();
		StringBuilder sample = new StringBuilder();

		for (int i = 0; i < k; i++) {
			sample.append(alphabet.charAt(rng.nextInt(n)));
		}

		Log.i("sampleFromAlphabet", "sampling " + k + "from " + alphabet +
				", yielding " + sample);
		return sample.toString();
	}
	
	/**
	 * Return the MD5 hash value for the given String
	 * 
	 * @param inputText	the given String
	 * @return	String of MD5 hash value
	 */
	public static String getMD5Hash(String inputText) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (md != null) {
			byte[] digest = md.digest(inputText.getBytes());
			return new BigInteger(1, digest).toString(16);
		}
		return null;
	}
	
	/**
	 * Get the directory name corresponding to owner's account ID
	 * 
	 * @param ownerId	account ID(xxx@domain.xxx)
	 * @return	the corresponding directory name
	 */
	public static String getOwnerDirName(String ownerId) {
		String ownerDirName =  ownerId.toLowerCase();
		//ownerDirName = ownerDirName.replaceAll("@(.*)$", "_at_$1");
		//ownerDirName = ownerDirName.replace('.', '_');
		return ownerDirName;
	}
	
}
