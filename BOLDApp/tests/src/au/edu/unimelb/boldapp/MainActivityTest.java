package au.edu.unimelb.boldapp;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class au.edu.unimelb.boldapp.MainActivityTest \
 * au.edu.unimelb.boldapp.tests/android.test.InstrumentationTestRunner
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super("au.edu.unimelb.boldapp", MainActivity.class);
    }

	public void testSomething() {
		assertTrue(true);
	}

	public void testSomething2() {
		assertTrue(true);
	}

}
