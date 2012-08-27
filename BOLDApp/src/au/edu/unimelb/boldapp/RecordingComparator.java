package au.edu.unimelb.boldapp;

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
		} else {
			//this.compareBy = "date";
			this.compareBy = "alphabetical";
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
		//if (compareBy.equals("alphabetical")) {
		//	return lhs.getName().compareTo(rhs.getName());
		//} else if (compareBy.equals("date")) {
		//	return lhs.getDate().compare(rhs.getDate());
		//}
		return lhs.getName().compareTo(rhs.getName());
	}
}
