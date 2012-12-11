package au.edu.unimelb.boldapp;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import com.google.common.base.Charsets;
import java.io.InputStream;
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

	public void testLoad() throws Exception {
		mActivity.loadLangCodes();
	}

}
