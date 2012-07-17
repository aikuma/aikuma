package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class RecordActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
