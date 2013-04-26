package org.lp20.aikuma;

import android.test.AndroidTestCase;
import android.util.Log;

public class AikumaTest extends AndroidTestCase {

	public void testGetAndroidID() {
		assertTrue(Aikuma.getAndroidID() != null);
	}
}
