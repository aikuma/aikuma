package au.edu.melbuni.boldapp.activities;

import android.os.Bundle;
import android.view.View;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Sounder;
import au.edu.melbuni.boldapp.ThresholdSpeechController;
import au.edu.melbuni.boldapp.extensions.ColorfulImageButton;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.persisters.Persister;

public class RespeakActivity extends BoldActivity {

	boolean listening = false;

	final ThresholdSpeechController recognizer = new ThresholdSpeechController();

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

	@Override
	protected void onDestroy() {
		// Stop the recognizer.
		//
		recognizer.stop();
		super.onDestroy();
	}

	public void installBehavior(Bundle savedInstanceState) {
		final ColorfulImageButton respeakButton = (ColorfulImageButton) findViewById(R.id.respeakButton);
//		final Button respeakBackButton = (Button) findViewById(R.id.respeakBackButton);

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
			@Override
			public boolean onLongClick(View view) {
				if (!respeakButton.isActivated()) {
					String selectedFilename = Bundler
							.getCurrentRespeakOriginal().getRelativeFilename();
					String sourceFilename = Persister.getBasePath()
							+ "respeak_originals/" + selectedFilename;
					String targetFilename = Persister.getBasePath()
							+ "respeak_" + selectedFilename;

					// Start listening (opens a new recording).
					//
					recognizer.listen(sourceFilename, targetFilename,
							new OnCompletionListener() {

								@Override
								public void onCompletion(Sounder sounder) {
									recognizer.stop();
									respeakButton.deactivate();
								}

							});
					respeakButton.activate();
				} else {
					recognizer.stop();
					respeakButton.deactivate();
				}
				return false;
			}
		});

//		respeakBackButton.setOnLongClickListener(new OnLongClickListener() {
//
//			@Override
//			public boolean onLongClick(View v) {
//				if (listening) {
//					recognizer.rewind(1000);
//				}
//				return false;
//			}
//		});
	}
}