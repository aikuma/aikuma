package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(
				new CustomExceptionHandler());
        setContentView(R.layout.main);
    }
}
