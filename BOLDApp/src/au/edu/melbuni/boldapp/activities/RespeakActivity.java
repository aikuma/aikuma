package au.edu.melbuni.boldapp.activities;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import au.edu.melbuni.boldapp.Demo;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Sounder;
import au.edu.melbuni.boldapp.SpeechController;
import au.edu.melbuni.boldapp.ThresholdSpeechController;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.listeners.OnSpeechListener;

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

		final SpeechController recognizer = new ThresholdSpeechController(
				new OnSpeechListener() {

					@Override
					public void onSpeech() {
						// respeakButton.getBackground().setColorFilter(Color.RED,
						// Mode.MULTIPLY);
					}

					@Override
					public void onSilence() {
						// respeakButton.getBackground().setColorFilter(Color.GREEN,
						// Mode.MULTIPLY);
					}

				});
		
		respeakButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (!listening) {
					recognizer.listen(Demo.getSoundfilePathWithoutExtension(),
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