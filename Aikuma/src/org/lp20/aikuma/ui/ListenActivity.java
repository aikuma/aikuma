/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.util.Log;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.R;
import org.lp20.aikuma.ui.sensors.ProximityDetector;
import org.lp20.aikuma.util.ImageUtils;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ListenActivity extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);
		menuBehaviour = new MenuBehaviour(this);
		fragment =
				(ListenFragment)
				getFragmentManager().findFragmentById(R.id.ListenFragment);
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		setUpRecording();
		setUpPlayer();
		setUpRespeakingImages();
		setUpRecordingInfo();
	}

	// Prepares the recording
	private void setUpRecording() {
		Intent intent = getIntent();
		String filenamePrefix = (String)
				intent.getExtras().get("filenamePrefix");
		try {
			recording = Recording.read(filenamePrefix);
			setUpRecordingName();
		} catch (IOException e) {
			//The recording metadata cannot be read, so let's wrap up this
			//activity.
			Toast.makeText(this, "Failed to read recording metadata.",
					Toast.LENGTH_LONG).show();
			ListenActivity.this.finish();
		}
	}

	// Prepares the information pertaining to the recording
	private void setUpRecordingInfo() {
		setUpRecordingName();
		LinearLayout recordingInfoView = (LinearLayout)
				findViewById(R.id.recordingInfo);
		LinearLayout originalImages = (LinearLayout)
				findViewById(R.id.originalImages);
		for (String id : recording.getSpeakersIds()) {
			originalImages.addView(makeSpeakerImageView(id));
		}
	}

	// Prepares the displayed name for the recording (including other things
	// such as duration and date.
	private void setUpRecordingName() {
		TextView nameView = (TextView) findViewById(R.id.recordingName);
		TextView dateDurationView = 
				(TextView) findViewById(R.id.recordingDateDuration);
		TextView langView = (TextView) findViewById(R.id.recordingLangCode);
		nameView.setText(recording.getNameAndLang());
		Integer duration = recording.getDurationMsec() / 1000;
		if (recording.getDurationMsec() == -1) {
			dateDurationView.setText(
					simpleDateFormat.format(recording.getDate()));
		} else {
			dateDurationView.setText(
				simpleDateFormat.format(recording.getDate()) + " (" +
				duration.toString() + "s)");
		}
	}

	// Makes the imageview for a given speaker
	private ImageView makeSpeakerImageView(String speakerId) {
		ImageView speakerImage = new ImageView(this);
		speakerImage.setAdjustViewBounds(true);
		speakerImage.setMaxHeight(40);
		speakerImage.setMaxWidth(40);
		speakerImage.setPaddingRelative(5,5,5,5);
		try {
			speakerImage.setImageBitmap(Speaker.getSmallImage(speakerId));
		} catch (IOException e) {
			// Not much can be done if the image can't be loaded.
		}
		return speakerImage;
	}

	// Set up the player
	private void setUpPlayer() {
		try {
			if (recording.isOriginal()) {
				setPlayer(new SimplePlayer(recording, true));
			} else {
				setPlayer(new InterleavedPlayer(recording));
				Button thumbRespeakingButton =
						(Button) findViewById(R.id.thumbRespeaking);
				thumbRespeakingButton.setVisibility(View.GONE);
				Button phoneRespeakingButton =
						(Button) findViewById(R.id.phoneRespeaking);
				phoneRespeakingButton.setVisibility(View.GONE);
			}
		} catch (IOException e) {
			//The player couldn't be created from the recoridng, so lets wrap
			//this activity up.
			Toast.makeText(this, "Failed to create player from recording.",
					Toast.LENGTH_LONG).show();
			ListenActivity.this.finish();
		}
	}

	// Prepares the images for the respeakings.
	private void setUpRespeakingImages() {
		List<Recording> respeakings;
		if (recording.isOriginal()) {
			respeakings = recording.getRespeakings();
		} else {
			try {
				respeakings =
						recording.getOriginal().getRespeakings();
			} catch (IOException e) {
				//If the original recording can't be loaded, then we can't
				//display any other respeaking images, so we should just return
				//now.
				return;
			}
		}
		LinearLayout respeakingImages = (LinearLayout)
				findViewById(R.id.RespeakingImages);
		for (final Recording respeaking : respeakings) {
			LinearLayout respeakingImageContainer = new LinearLayout(this);
			respeakingImageContainer.setOrientation(LinearLayout.VERTICAL);
			ImageView respeakingImage = new ImageView(this);
			respeakingImage.setAdjustViewBounds(true);
			respeakingImage.setMaxHeight(60);
			respeakingImage.setMaxWidth(60);
			respeakingImage.setPadding(5,5,5,5);
			respeakingImage.setPaddingRelative(2,2,2,2);
			if (respeaking.equals(recording)) {
				respeakingImage.setBackgroundColor(0xFFCC0000);
			}
			respeakingImage.setOnClickListener(new View.OnClickListener() {
				public void onClick(View _) {
					Intent intent = new Intent(ListenActivity.this,
							ListenActivity.class);
					intent.putExtra("filenamePrefix",
							respeaking.getFilenamePrefix().toString());
					startActivity(intent);
					ListenActivity.this.finish();
				}
			});
			try {
				if (respeaking.getSpeakersIds().size() > 0) {
					respeakingImage.setImageBitmap(
							Speaker.getSmallImage(
							respeaking.getSpeakersIds().get(0)));
				} else {
					continue;
				}
			} catch (IOException e) {
				// Not much can be done if the image can't be loaded.
			}
			respeakingImageContainer.addView(respeakingImage);
			TextView respeakingLang = new TextView(this);
			respeakingLang.setText(respeaking.getFirstLangCode());
			respeakingLang.setGravity(Gravity.CENTER_HORIZONTAL);
			/*
			List<Language> langs = respeaking.getLanguages();
			if (langs.size() > 0) {
				respeakingLang.setText(respeaking.getLanguages().get(0).getCode());
				respeakingLang.setGravity(Gravity.CENTER_HORIZONTAL);
			}
			*/
			respeakingImageContainer.addView(respeakingLang);
			respeakingImages.addView(respeakingImageContainer);
		}
	}

	private void setPlayer(SimplePlayer player) {
		this.player = player;
		fragment.setPlayer(player);
	}

	private void setPlayer(InterleavedPlayer player) {
		this.player = player;
		fragment.setPlayer(player);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return menuBehaviour.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return menuBehaviour.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.proximityDetector = new ProximityDetector(this) {
			public void near(float distance) {
				WindowManager.LayoutParams params = getWindow().getAttributes();
				params.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON;
				params.screenBrightness = 0;
				getWindow().setAttributes(params);
				//record();
			}
			public void far(float distance) {
				WindowManager.LayoutParams params = getWindow().getAttributes();
				params.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON;
				params.screenBrightness = 1;
				getWindow().setAttributes(params);
				//pause();
			}
		};
		this.proximityDetector.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		this.proximityDetector.stop();
	}

	/**
	 * Change to the thumb respeaking activity
	 *
	 * @param	view	The thumb respeaking button
	 */
	public void onThumbRespeakingButton(View view) {
		Intent intent = new Intent(this, ThumbRespeakActivity.class);
		intent.putExtra("filenamePrefix", recording.getFilenamePrefix());
		intent.putExtra("sampleRate", recording.getSampleRate());
		startActivity(intent);
	}

	/**
	 * Change to the phone respeaking activity
	 *
	 * @param	view	The phone respeaking button
	 */
	public void onPhoneRespeakingButton(View view) {
		Intent intent = new Intent(this, PhoneRespeakActivity.class);
		intent.putExtra("filenamePrefix", recording.getFilenamePrefix());
		intent.putExtra("sampleRate", recording.getSampleRate());
		startActivity(intent);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (proximityDetector.isNear()) {
			return false;
		} else {
			return super.dispatchTouchEvent(event);
		}
	}

	private boolean phoneRespeaking = false;
	private Player player;
	private ListenFragment fragment;
	private Recording recording;
	private MenuBehaviour menuBehaviour;
	private SimpleDateFormat simpleDateFormat;
	private ProximityDetector proximityDetector;
}
