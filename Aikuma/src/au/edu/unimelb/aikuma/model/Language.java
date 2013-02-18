package au.edu.unimelb.aikuma.model;

import java.util.List;
import android.os.Parcelable;
import android.os.Parcel;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

/**
 * Representation of an ISO 639-3 language
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class Language implements Parcelable {

	/**
	 * The name of the language
	 */
	private String name;

	/**
	 * The language's ISO 639-3 language code
	 */
	private String code;

	/**
	 * Name mutator
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Code mutator
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Name accessor
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Code accessor
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * Standard constructor
	 */
	public Language(String name, String code) {
		setName(name);
		setCode(code);
	}

	/**
	 * Returns a string representation of the language
	 *
	 * @return A string instance.
	 */
	public String toString() {
		return getName() + " : " + getCode();
	}

	/**
	 * Describe the kinds of special objects contained in this Parcelable's
	 * marshalled representation.
	 */
	public int describeContents() {
		return 0;
	}

	/**
	 * Makes a parcel that represents this object
	 *
	 * @param	out	The parcel to be written to.
	 * @param	_flags	Additional flags about how the object should be
	 * written.
	 */
	public void writeToParcel(Parcel out, int _flags) {
		out.writeString(getName());
		out.writeString(getCode());
	}

	/**
	 * Generates an instance of a Language given a corresponding parcel.
	 */
	public static final Parcelable.Creator<Language> CREATOR = 
			new Parcelable.Creator<Language>() {
		public Language createFromParcel(Parcel in) {
			return new Language(in);
		}

		public Language[] newArray(int size) {
			return new Language[size];
		}
	};

	/**
	 * Constructor that takes a parcel as an argument so that the class can be
	 * parcelable.
	 *
	 * @param	in	A parcel containing the information on the Language to be
	 * constructed.
	 */
	public Language(Parcel in) {
		setName(in.readString());
		setCode(in.readString());
	}

	/**
	 * Encodes the Language object as a corresponding JSONObject.
	 *
	 * @return	A representation of the language as a JSONObject.
	 */
	public JSONObject encode() {
		JSONObject encodedLanguage = new JSONObject();
		encodedLanguage.put("name", getName());
		encodedLanguage.put("code", getCode());
		return encodedLanguage;
	}

	/**
	 * Encodes a list of languages as a corresponding JSONArray object.
	 *
	 * @param	languages	A list of languages to be encoded
	 * @return	A JSONArray object.
	 */
	public static JSONArray encodeList(List<Language> languages) {
		JSONArray languageArray = new JSONArray();
		for (Language language : languages) {
			languageArray.add(language.encode());
		}
		return languageArray;
	}
}
