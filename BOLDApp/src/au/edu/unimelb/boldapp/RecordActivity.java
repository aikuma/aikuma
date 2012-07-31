package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.content.Intent;

public class RecordActivity extends Activity {
	private Boolean recording;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		recording = false;
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public void goBack(View view){
		RecordActivity.this.finish();
	}

	public void goToUserSelection(View view){
		Intent intent = new Intent(this, UserSelectionActivity.class);
		startActivity(intent);
	}

	public void record(View view) {
		//Toggle the recording Boolean.
		recording = !recording;
		Log.i("yoyoyo", recording.toString());
	}
}
