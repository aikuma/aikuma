/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.util;

import java.text.SimpleDateFormat;

/**
 * A wrapper class for SimpleDateFormat whose default constructor applies the
 * non-localized pattern corresponding to the ISO 8601 international standard
 * date format.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class StandardDateFormat extends SimpleDateFormat {
	/**
	 * Constructor that specifies the date format.
	 */
	public StandardDateFormat() {
		super("yyyy-MM-dd'T'HH:mm:ss'Z'");
		super.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
	}
}
