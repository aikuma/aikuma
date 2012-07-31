package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import au.edu.unimelb.boldapp.audio.Recorder;

public class RecordActivity extends Activity {
	private Boolean isRecording;
	private Recorder recorder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		isRecording = false;
		//recorder = new Recorder();
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
		isRecording = !isRecording;
		//recorder.listen("/mnt/sdcard/bold/recordings/target_file.wav");
		Log.i("yoyoyo", isRecording.toString());
	}
}
