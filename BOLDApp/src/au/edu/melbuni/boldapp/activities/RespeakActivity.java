package au.edu.melbuni.boldapp.activities;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import au.edu.melbuni.boldapp.Demo;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Sounder;
import au.edu.melbuni.boldapp.SpeechController;
import au.edu.melbuni.boldapp.ThresholdSpeechController;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.persisters.Persister;

public class RespeakActivity extends BoldActivity {

	boolean listening = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		configureView(savedInstanceState);
		installBehavior(savedInstanceState);
	}

	@Override
	public void configureView(Bundle savedInstanceState) {
		super.configureView(savedInstanceState);

		setContent(R.layout.respeak);
	};

	public void installBehavior(Bundle savedInstanceState) {
		final ImageButton respeakButton = (ImageButton) findViewById(R.id.respeakButton);
		final MediaController player = (MediaController) findViewById(R.id.respeakMediaController);
		
//		Media
		
		// TouchDelegate touchDelegate = new TouchDelegate(new Rect(0, 0,
		// Resources.getSystem().getDisplayMetrics().widthPixels,
		// Resources.getSystem().getDisplayMetrics().heightPixels),
		// respeakButton) {
		// @Override
		// public boolean onTouchEvent(MotionEvent event) {
		// LogWriter.log("touchingggg!");
		// return true;
		// }
		// };
		// respeakButton.getRootView().setTouchDelegate(touchDelegate);

		respeakButton.setOnLongClickListener(new View.OnLongClickListener() {
			final SpeechController recognizer = new ThresholdSpeechController();
			
			@Override
			public boolean onLongClick(View view) {
				if (!listening) {
					String sourceFilename = Persister.getBasePath()
							+ "timelines/" + Demo.getUUIDString() + "/segments/0";
					String targetFilename = "respeaking.wav"; // Demo.getUUIDString() + ".wav";
					
					recognizer.listen(sourceFilename,
							targetFilename,
							new OnCompletionListener() {

								@Override
								public void onCompletion(Sounder sounder) {
									listening = false;
									respeakButton.getBackground()
											.clearColorFilter();
								}

							});
					respeakButton.getBackground().setColorFilter(Color.GREEN,
							Mode.MULTIPLY);
					listening = true;
				} else {
					recognizer.stop();
					listening = false;
					respeakButton.getBackground().clearColorFilter();
				}
				return false;
			}
		});
	}
}