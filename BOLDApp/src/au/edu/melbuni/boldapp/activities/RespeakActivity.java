package au.edu.melbuni.boldapp.activities;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Sounder;
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
		final Button respeakBackButton = (Button) findViewById(R.id.respeakBackButton);

		final ThresholdSpeechController recognizer = new ThresholdSpeechController();

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
				if (!listening) {
					String selectedFilename = Bundler
							.getCurrentRespeakOriginal().getRelativeFilename();
					String sourceFilename = Persister.getBasePath()
							+ "respeak_originals/" + selectedFilename;
					String targetFilename = Persister.getBasePath()
							+ "respeak_" + selectedFilename;

					recognizer.listen(sourceFilename, targetFilename,
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

		respeakBackButton.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (listening) {
					recognizer.rewind(1000);
				}
				return false;
			}
		});
	}
}