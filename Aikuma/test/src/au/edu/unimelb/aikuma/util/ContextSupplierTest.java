package au.edu.unimelb.aikuma.util;

import android.test.AndroidTestCase;
import android.util.Log;

public class ContextSupplierTest extends AndroidTestCase {

	public void testGetAndroidID() {
		Log.i("GetAndroidIDTest", ContextSupplier.getAndroidID());
		assertTrue(ContextSupplier.getAndroidID() != null);
	}
}
