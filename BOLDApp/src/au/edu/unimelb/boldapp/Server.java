package au.edu.unimelb.aikuma;

import android.os.Parcel;
import android.os.Parcelable;

public class Server implements Parcelable{
	private String ipAddress;
	private String username;
	private String password;

	public Server(String ipAddress, String username, String password) {
		setIPAddress(ipAddress);
		setUsername(username);
		setPassword(password);
	}

	public void setIPAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIPAddress() {
		return this.ipAddress;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(getIPAddress());
		out.writeString(getUsername());
		out.writeString(getPassword());
	}

	public static final Parcelable.Creator<Server> CREATOR =
			new Parcelable.Creator<Server>() {
		public Server createFromParcel(Parcel in) {
			return new Server(in);
		}
		public Server[] newArray(int size) {
			return new Server[size];
		}
	};

	public Server(Parcel in) {
		setIPAddress(in.readString());
		setUsername(in.readString());
		setPassword(in.readString());
	}
}
