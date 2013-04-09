package org.lp20.aikuma.model;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class LanguageTest extends TestCase {

	/**
	 * Ensures that one language can be encoded successfully.
	 */
	public void testEncodeLanguage() {
		Language language = new Language("English", "eng");
		JSONObject encodedLanguage = language.encode();
		assertEquals("{\"code\":\"eng\",\"name\":\"English\"}",
				encodedLanguage.toString());
	}

	/**
	 * Ensures that a list of two languages can be encoded correctly.
	 */
	public void testEncodeLanguages() throws Exception {
		Language l1 = new Language("Alekano", "gah");
		Language l2 = new Language("Usarufa", "usa");
		List<Language> languages = new ArrayList<Language>();
		languages.add(l1);
		languages.add(l2);
		JSONArray encodedLanguages = Language.encodeList(languages);
		assertEquals(
				"[{\"code\":\"gah\",\"name\":\"Alekano\"}" +
				",{\"code\":\"usa\",\"name\":\"Usarufa\"}]",
				encodedLanguages.toString());
	}

	/**
	 * Ensures that the decodeJSONArray method functions as expected.
	 */
	public void testDecodeLanguages() {
		Language l1 = new Language("Alekano", "gah");
		Language l2 = new Language("Usarufa", "usa");
		List<Language> languages = new ArrayList<Language>();
		languages.add(l1);
		languages.add(l2);
		JSONArray encodedLanguages = Language.encodeList(languages);
		assertEquals(languages, Language.decodeJSONArray(encodedLanguages));
	}
}
