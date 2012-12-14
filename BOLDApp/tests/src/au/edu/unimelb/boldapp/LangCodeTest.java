package au.edu.unimelb.boldapp;

import android.test.AndroidTestCase;
import android.util.Log;
import com.google.common.base.Charsets;
import java.io.File;
import java.io.FileInputStream;;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class LangCodeTest extends AndroidTestCase {

	/*
	public void testLoadSerializeTimes() throws Exception {
		long startTime = System.nanoTime();
		Map map =
				FileIO.initialLoadLangCodes(
				getContext().getResources().openRawResource(R.raw.iso_639_3));
		long endTime = System.nanoTime();
		Log.i("duration", "first loading time: " + (endTime -
				startTime)/1000000000.0);

		startTime = System.nanoTime();
		FileOutputStream fos = new FileOutputStream(new
				File(FileIO.getAppRootPath(), "lang_codes"));
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(map);
		oos.close();
		fos.close();
		endTime = System.nanoTime();
		Log.i("duration", "writing time: " + (endTime - startTime)/1000000000.0);

		startTime = System.nanoTime();
		FileInputStream fis = new FileInputStream(new
				File(FileIO.getAppRootPath(), "lang_codes"));
		ObjectInputStream ois = new ObjectInputStream(fis);
		Map newMap = (Map) ois.readObject();
		ois.close();
		fis.close();
		endTime = System.nanoTime();
		Log.i("duration", "reading time: " + (endTime - startTime)/1000000000.0);
		assertEquals(map, newMap);

	}

	public void testLoadSerialize() throws Exception {
		assertEquals(null, GlobalState.getLangCodeMap());
		FileIO.loadLangCodes(
				getContext().getResources().openRawResource(R.raw.iso_639_3));
		Thread.sleep(6000);
		assertTrue(GlobalState.getLangCodeMap() != null);
	}
	*/

	public void testReadLangCodes() throws Exception {
		Map map = FileIO.readLangCodes(getContext().getResources());
		assertEquals("usa", map.get("Usarufa"));
		assertEquals("gah", map.get("Alekano"));
	}

	public void testLoadLangCodeMap() {
		GlobalState.setLangCodeMap(null);
		Map map = GlobalState.getLangCodeMap(getContext().getResources());
		assertEquals("usa", map.get("Usarufa"));
		assertEquals("gah", map.get("Alekano"));
	}

	public void testLoadLangCodeMap2() {
		GlobalState.setLangCodeMap(null);
		Map map = GlobalState.getLangCodeMap(getContext().getResources());
		map = GlobalState.getLangCodeMap(getContext().getResources());
		assertEquals("usa", map.get("Usarufa"));
		assertEquals("gah", map.get("Alekano"));
	}
}
