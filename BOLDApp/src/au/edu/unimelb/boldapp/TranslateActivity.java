package au.edu.unimelb.boldapp;

import java.util.UUID;

import android.os.Bundle;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.widget.ImageButton;
import android.view.View;
import android.util.Log;

import au.edu.unimelb.boldapp.audio.Respeaker;
import au.edu.unimelb.boldapp.sensors.ShakeDetector;

/**
 * The activity that allows one to translate audio
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class TranslateActivity extends RespeakActivity {
  
	/**
	 * Called when the activity starts.
	 *
	 * Generates a UUID for the recording, prepares the file and creates a
	 * metadata file that includes the name and UUID of the user who made the
	 * recording.
	 *
   * TODO There is a lot of duplicate code here and in the superclass.
   *
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.recordingNamePrefix = "Translation of ";

		//Get the original from the intent
		Intent intent = getIntent();
		UUID originalUUID = (UUID) intent.getExtras().get("recordingUUID");
		this.original = GlobalState.getRecordingMap().get(originalUUID);
		setContentView(R.layout.respeak);

		startedRespeaking = false;
		respeaking = false;
		respeaker = new Respeaker();

		this.uuid = UUID.randomUUID();

		respeaker.prepare(
				FileIO.getAppRootPath() + FileIO.getRecordingsPath()
				+ originalUUID.toString() + ".wav",
				FileIO.getAppRootPath() + FileIO.getRecordingsPath()
				+ uuid.toString() + ".wav",
				FileIO.getAppRootPath() + FileIO.getRecordingsPath()
				+ uuid.toString() + ".map");

		respeaker.player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer _player) {
				ImageButton respeakButton = (ImageButton) 
						findViewById(R.id.Respeak);
				ImageButton pauseButton = (ImageButton) 
						findViewById(R.id.Pause);
				pauseButton.setVisibility(View.INVISIBLE);
				respeakButton.setVisibility(View.INVISIBLE);
				respeaker.stop();
			}
		});
    
    /**
     * Note: Currently we're only installing the ShakeDetector
     * in the TranslateActivity.
     *
     * We do not detect when somebody picks the phone up.
     *
     */
    shakeDetector = new ShakeDetector(this, 3.3f, true, true, false) {
       @Override
       public void shaken(float acceleration) {
         respeaker.rewind(500);
       }
     };
    shakeDetector.start();
	}

	/**
	 * Cancel 
	 *
	 * @param	view	The button that was pressed
	 */
	@Override
	public void cancel(View view){
		respeaker.stop();
		FileIO.delete(FileIO.getRecordingsPath() + uuid.toString() + ".wav");
		FileIO.delete(FileIO.getRecordingsPath() + uuid.toString() + ".map");
		TranslateActivity.this.finish();
		Intent intent = new Intent(this, RecordingSelectionActivity.class);
		intent.putExtra("activity", "TranslateActivity");
		startActivity(intent);
	}
}
