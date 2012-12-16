package au.edu.unimelb.boldapp;

import android.os.Parcelable;
import android.os.Parcel;

public class Language implements Parcelable {

	private String primaryName;
	private String code;
	private String listName;

	public void setPrimaryName(String primaryName) {
		this.primaryName = primaryName;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPrimaryName() {
		return this.primaryName;
	}

	public String getCode() {
		return this.code;
	}

	public Language(String primaryName, String code) {
		setPrimaryName(primaryName);
		setCode(code);
	}

	public String toString() {
		return getPrimaryName() + " : " + getCode();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(getPrimaryName());
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
		setPrimaryName(in.readString());
		setCode(in.readString());
	}

}
