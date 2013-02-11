package au.edu.unimelb.aikuma;

import android.os.Parcelable;
import android.os.Parcel;

public class Language implements Parcelable {

	private String name;
	private String code;

	public void setName(String name) {
		this.name = name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return this.name;
	}

	public String getCode() {
		return this.code;
	}

	public Language(String name, String code) {
		setName(name);
		setCode(code);
	}

	public String toString() {
		return getName() + " : " + getCode();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(getName());
		out.writeString(getCode());
	}

	public static final Parcelable.Creator<Language> CREATOR = 
			new Parcelable.Creator<Language>() {
		public Language createFromParcel(Parcel in) {
			return new Language(in);
		}

		public Language[] newArray(int size) {
			return new Language[size];
		}
	};

	public Language(Parcel in) {
		setName(in.readString());
		setCode(in.readString());
	}

}
