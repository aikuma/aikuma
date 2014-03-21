package org.lp20.aikuma.util;

import java.util.Random;

/**
 * Offers methods to create random sequences of characters.
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
			sample.append(alphabet.charAt(r.nextInt(n)));
		}

		Log.i("sampleFromAlphabet", "sampling " + k + "from " + alphabet +
				", yielding " + sample);
		return sample.toString();
	}
}
