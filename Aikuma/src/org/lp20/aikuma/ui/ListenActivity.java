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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.ImageUtils;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ListenActivity extends AikumaActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);
		menuBehaviour = new MenuBehaviour(this);
		//UUID respeakingUUID = UUID.randomUUID();
		//phoneRespeaker = new PhoneRespeaker(recording,
		//		new File(Recording.getRecordingsPath(),
		//				respeakingUUID.toString() + ".wav"),
		//		sampleRate);
		fragment =
				(ListenFragment)
				getFragmentManager().findFragmentById(R.id.ListenFragment);
		setUpRecording();
		setUpPlayer();
		setUpRespeakingImages();
		setUpRecordingInfo();
	}

	private void setUpRecording() {
		Intent intent = getIntent();
		UUID recordingUUID = UUID.fromString(
				(String) intent.getExtras().get("uuidString"));
		try {
			recording = Recording.read(recordingUUID);
			setUpRecordingName();
		} catch (IOException e) {
			//The recording metadata cannot be read, so let's wrap up this
			//activity.
			Toast.makeText(this, "Failed to read recording metadata.",
					Toast.LENGTH_LONG).show();
			ListenActivity.this.finish();
		}
	}

	private void setUpRecordingInfo() {
		setUpRecordingName();
		LinearLayout recordingInfoView = (LinearLayout)
				findViewById(R.id.recordingInfo);
		for (UUID uuid : recording.getSpeakersUUIDs()) {
			recordingInfoView.addView(makeSpeakerImageView(uuid));
		}
	}

	private void setUpRecordingName() {
		TextView nameView = (TextView) findViewById(R.id.recordingName);
		nameView.setText(recording.getName());
	}

	private ImageView makeSpeakerImageView(UUID speakerUUID) {
		ImageView speakerImage = new ImageView(this);
		speakerImage.setAdjustViewBounds(true);
		speakerImage.setMaxHeight(40);
		speakerImage.setMaxWidth(40);
		speakerImage.setPaddingRelative(5,5,5,5);
		try {
			speakerImage.setImageBitmap(ImageUtils.getSmallImage(speakerUUID));
		} catch (IOException e) {
			// Not much can be done if the image can't be loaded.
		}
		return speakerImage;
	}

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

	private void setUpRespeakingImages() {
		List<Recording> respeakings;
		if (recording.isOriginal()) {
			respeakings = recording.getRespeakings();
		} else {
			try {
				respeakings =
						Recording.read(recording.getOriginalUUID()).getRespeakings();
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
					intent.putExtra("uuidString",
							respeaking.getUUID().toString());
					startActivity(intent);
					ListenActivity.this.finish();
				}
			});
			try {
				if (respeaking.getSpeakersUUIDs().size() > 0) {
					respeakingImage.setImageBitmap(
							ImageUtils.getSmallImage(
							respeaking.getSpeakersUUIDs().get(0)));
				} else {
					continue;
				}
			} catch (IOException e) {
				// Not much can be done if the image can't be loaded.
			}
			respeakingImageContainer.addView(respeakingImage);
			TextView respeakingLang = new TextView(this);
			respeakingLang.setText(respeaking.getLanguages().get(0).getCode());
			respeakingLang.setGravity(Gravity.CENTER_HORIZONTAL);
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
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onStop();
	}

	@Override
	public void onRestart() {
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onStop();
	}

	public void onPhoneRespeakingToggle(View view) {
		phoneRespeaking = ((ToggleButton) view).isChecked();
	}

	public void onThumbRespeakingButton(View view) {
		Intent intent = new Intent(this, ThumbRespeakActivity.class);
		intent.putExtra("uuidString", recording.getUUID().toString());
		intent.putExtra("sampleRate", recording.getSampleRate());
		startActivity(intent);
	}

	public void onPhoneRespeakingButton(View view) {
		Intent intent = new Intent(this, PhoneRespeakActivity.class);
		intent.putExtra("uuidString", recording.getUUID().toString());
		intent.putExtra("sampleRate", recording.getSampleRate());
		startActivity(intent);
	}

	private boolean phoneRespeaking = false;
	private Player player;
	private ListenFragment fragment;
	private Recording recording;
	private MenuBehaviour menuBehaviour;
}
