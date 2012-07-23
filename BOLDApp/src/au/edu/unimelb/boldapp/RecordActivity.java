package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

public class RecordActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("okokok", "yes01");
		super.onCreate(savedInstanceState);
		Log.i("okokok", "yes1");
		setContentView(R.layout.record);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public void goBack(View view){
		RecordActivity.this.finish();
	}
}
