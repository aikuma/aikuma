package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import android.util.Log;

public class MainActivity extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	/**
		Thread.setDefaultUncaughtExceptionHandler(
				new CustomExceptionHandler());
	**/
		setContentView(R.layout.main);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	public void goToRecord(View view) {
		Intent intent = new Intent(this, RecordActivity.class);
		startActivity(intent);
	}

	public void goToUserSelection(View view){
		Intent intent = new Intent(this, UserSelectionActivity.class);
		startActivity(intent);
	}
}
