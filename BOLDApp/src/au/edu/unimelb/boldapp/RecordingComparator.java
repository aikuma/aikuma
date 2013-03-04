package au.edu.unimelb.aikuma;

import java.util.Comparator;

/**
 * The comparator used to compare recordings.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class RecordingComparator implements Comparator<Recording> {
	/**
	 * String that indicates how the recordings are to be compared.
	 */
	private String compareBy;
	
	/**
	 * Default Constructor.
	 *
	 * @param	compareBy	String representing how the recordings should be
	 * compared (alphabetical or by date).
	 */
	public RecordingComparator(String compareBy) {
		if (compareBy.equals("alphabetical")) {
			this.compareBy = compareBy;
		} else if (compareBy.equals("date")) {
			this.compareBy = "date";
		} else if (compareBy.equals("likes")) {
			this.compareBy = "likes";
		}
	}

	/**
	 * Compares the two recordings to determine their relative ordering.
	 *
	 * @param	lhs	a Recording
	 * @param	rhs	a second Recording to compare with lhs
	 * @return	an integer < 0 if lhs is less than rhs, 0 if they are equal,
	 * and > 0 if lhs is greater than rhs.
	 */
	public int compare(Recording lhs, Recording rhs) {
		if (compareBy.equals("alphabetical")) {
			return lhs.getName().compareTo(rhs.getName());
		} else if (compareBy.equals("date")) {
			// Else compare by date
			return lhs.getDate().compareTo(rhs.getDate());
		} else if (compareBy.equals("likes")) {
			// rhs - lhs because we want to order from most likes to least
			return rhs.getLikes() - lhs.getLikes();
		} else {
			// There's no specified way to compare things, so everything is
			// equal.
			return 0;
		}
	}
}
