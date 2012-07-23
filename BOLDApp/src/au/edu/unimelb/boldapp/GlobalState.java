package au.edu.unimelb.boldapp;

public abstract class GlobalState {
	private static User currentUser;

	public static User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}
}
