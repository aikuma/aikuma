package au.edu.unimelb.boldapp;

import android.test.AndroidTestCase;
import android.util.Log;
import com.google.common.base.Charsets;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class LangCodeTest extends AndroidTestCase {

	public void testLoad() throws Exception {
		//AssetManager am = context.getAssets();
		//InputStream is = am.open("iso-639-3_20120816.tab");
		InputStream is = getContext().getResources().openRawResource(
				R.raw.iso_639_3);
		StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer, Charsets.UTF_8);
		String inputString = writer.toString();
		Map<String,String> map = new HashMap<String,String>();
		String[] lines = inputString.split("\n");
		for (String line : lines) {
			String[] elements = line.split("(?=\t)");
			map.put(elements[0].trim(), elements[6].trim());
		}
		Log.i("text", map.get("aaa"));
	}

}
