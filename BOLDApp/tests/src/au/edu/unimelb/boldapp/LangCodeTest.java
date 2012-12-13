package au.edu.unimelb.boldapp;

import android.test.ActivityInstrumentationTestCase2;
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

public class LangCodeTest extends ActivityInstrumentationTestCase2<FilterList> {

	private FilterList mActivity;

	public LangCodeTest() {
		super("au.edu.unimelb.boldapp", FilterList.class);
	}

	@Override
	protected void setUp() throws Exception {
		mActivity = getActivity();
	}

	public void testLoadSerialize() throws Exception {
		long startTime = System.nanoTime();
		Map map =
				FileIO.loadLangCodes(
				mActivity.getResources().openRawResource(R.raw.iso_639_3));
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

}
