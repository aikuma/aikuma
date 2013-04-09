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
	public StandardDateFormat() {
		super("yyyy-MM-dd HH:mm:ss.SSSZ");
	}
}
