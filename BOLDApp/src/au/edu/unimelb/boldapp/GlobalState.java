package au.edu.unimelb.boldapp;

public abstract class GlobalState {
	private static User currentUser;

	public static User getCurrentUser() {
		return currentUser;
	}

	public static void setCurrentUser(User currentUser) {
		GlobalState.currentUser = currentUser;
	}
}
