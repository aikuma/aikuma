package au.edu.unimelb.boldapp;

/**
 * Class to contain our (minimal number of) global variables.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public abstract class GlobalState {
	/**
	 * The user that is currently selected to be the author of new recordings
	 * and respeakings
	 */
	private static User currentUser;

	public static User getCurrentUser() {
		return currentUser;
	}

	public static void setCurrentUser(User currentUser) {
		GlobalState.currentUser = currentUser;
	}
}
