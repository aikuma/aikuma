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
import au.edu.melbuni.boldapp.Transcriber;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;

public class TranscribeActivity extends BoldActivity {

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
		final ImageButton transcribeButton = (ImageButton) findViewById(R.id.respeakButton);
		final SpeechController transcriber = new Transcriber();
		transcribeButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (!listening) {
					transcriber.listen(
							Demo.getSoundfilePathWithoutExtension(),
							new OnCompletionListener() {

								@Override
								public void onCompletion(Sounder sounder) {
									listening = false;
									transcribeButton.getBackground()
											.clearColorFilter();
								}

							});
					transcribeButton.getBackground().setColorFilter(Color.GREEN,
							Mode.MULTIPLY);
					listening = true;
				} else {
					transcriber.stop();
					listening = false;
					transcribeButton.getBackground().clearColorFilter();
				}
				return false;
			}
		});
	}
}