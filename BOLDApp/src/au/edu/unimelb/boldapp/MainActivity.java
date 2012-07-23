package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.util.Log;

public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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
		Log.i("okokok", "yes");
		Intent intent = new Intent(this, RecordActivity.class);
		Log.i("okokok", "yes0");
		startActivity(intent);
	}

	public void goToUserSelection(View view){
		Intent intent = new Intent(this, UserSelectionActivity.class);
		startActivity(intent);
	}
}
