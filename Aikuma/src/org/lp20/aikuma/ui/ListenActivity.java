/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.audio.MarkedPlayer;
import org.lp20.aikuma.audio.TranscriptPlayer;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.ui.sensors.ProximityDetector;
import org.lp20.aikuma.util.AikumaSettings;

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
		videoView = (VideoView) findViewById(R.id.videoView);
		
		googleAuthToken = AikumaSettings.getCurrentUserToken();

		setUpRecording();
		setUpRespeakings();

	}
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if(recording.isMovie()) {
			setUpVideoView();
		} else {
			videoView.setVisibility(View.GONE);
			
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			fragment = new ListenFragment();
			ft.replace(R.id.listenFragment, fragment);
			ft.commit();
			
			setUpPlayer();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateStarButton();
		updateFlagButton();
		updatePublicShareButton();
		updateArchiveButton();
		//TODO: updatePrivateShareButton(), updateRefreshButton()
		
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
	
	

	// Prepares the original recording 
	private void setUpRecording() {
		Intent intent = getIntent();
		String id = (String) intent.getExtras().get("id");
		versionName = (String) intent.getExtras().get("versionName");
		ownerId = (String) intent.getExtras().get("ownerId");
		try {
			recording = Recording.read(versionName, ownerId, id);
			
			setUpQuickMenu();
			List<Recording> original = new ArrayList<Recording>();
			original.add(recording);
			ArrayAdapter<Recording> adapter = 
					new RecordingArrayAdapter(this, original, quickMenu);
			ListView originalView = 
					(ListView) findViewById(R.id.selectedOriginal);
			originalView.setAdapter(adapter);
		} catch (IOException e) {
			//The recording metadata cannot be read, so let's wrap up this
			//activity.
			Toast.makeText(this, "Failed to read recording metadata.",
					Toast.LENGTH_LONG).show();
			ListenActivity.this.finish();
		}
	}
	
	// Prepares the respeakings related to the original recording
	private void setUpRespeakings() {
		if(recording.isOriginal()) {
			ListView respeakingsListView = 
					(ListView) findViewById(R.id.relatedCommentaries);
			List<Recording> respeakings = recording.getRespeakings();
			ArrayAdapter<Recording> adapter = 
					new RecordingArrayAdapter(this, respeakings);
			respeakingsListView.setAdapter(adapter);
			
			respeakingsListView.setOnItemClickListener(
					new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					Recording respeaking = 
							(Recording) parent.getItemAtPosition(position);
					Intent intent = new Intent(ListenActivity.this, 
							ListenRespeakingActivity.class);
					intent.putExtra("originalId", recording.getId());
					intent.putExtra("originalOwnerId", ownerId);
					intent.putExtra("originalVerName", versionName);
					
					intent.putExtra("respeakingId", respeaking.getId());

					startActivity(intent);
				}
			});
		}
	}


	// Creates the quickMenu for the original recording 
	//(quickMenu: star/flag/share/archive)
	private void setUpQuickMenu() {
		quickMenu = new QuickActionMenu<Recording>(this);
		
		QuickActionItem starAct = new QuickActionItem("star", R.drawable.star);
		QuickActionItem flagAct = new QuickActionItem("flag", R.drawable.flag);
		QuickActionItem shareAct = 
				new QuickActionItem("share", R.drawable.share);
		QuickActionItem respeakAct = new QuickActionItem("respeak", R.drawable.respeak_32);
		QuickActionItem translateAct = new QuickActionItem("interpret", R.drawable.translate_32);
		QuickActionItem refresthAct = new QuickActionItem("refresh", R.drawable.refresh_32);
		
		quickMenu.addActionItem(starAct);
		quickMenu.addActionItem(flagAct);
		quickMenu.addActionItem(shareAct);
		
		/*
		if(googleAuthToken != null) {
			QuickActionItem archiveAct = 
					new QuickActionItem("share", R.drawable.aikuma_32);
			QuickActionItem privateShareAct =
					new QuickActionItem("share", R.drawable.speakers_32g);
			quickMenu.addActionItem(archiveAct);
			quickMenu.addActionItem(privateShareAct);
		}*/
		QuickActionItem archiveAct = 
				new QuickActionItem("share", R.drawable.aikuma_32);
		QuickActionItem privateShareAct =
				new QuickActionItem("share", R.drawable.speakers_32g);
		quickMenu.addActionItem(archiveAct);
		quickMenu.addActionItem(privateShareAct);
		if(googleAuthToken == null) {
			quickMenu.setItemEnabledAt(3, false);
			quickMenu.setItemEnabledAt(4, false);
		}
		
		quickMenu.addActionItem(respeakAct);
		quickMenu.addActionItem(translateAct);
		quickMenu.addActionItem(refresthAct);
		
		// Tagging buttons
		QuickActionItem tagAct =
				new QuickActionItem("tag", R.drawable.tag_32);
		quickMenu.addActionItem(tagAct);
		
		//setup the action item click listener
		quickMenu.setOnActionItemClickListener(new QuickActionMenu.OnActionItemClickListener<Recording>() {			
			@Override
			public void onItemClick(int pos, Recording recording) {
				
				if (pos == 0) { //Add item selected
					onStarButtonPressed(null);
				} else if (pos == 1) { //Accept item selected
					onFlagButtonPressed(null);
				} else if (pos == 2) { //Upload item selected
					onShareButtonPressed(null);
				} else if (pos == 3) {
					onArchiveButtonPressed(null);
				} else if (pos == 4) {
					onPrivateShareButtonPressed(null);
				} else if (pos == 5) {
					//respeak
					onRespeakButton(null, "respeak");
				} else if (pos == 6) {
					//translate
					onInterpretButtonPressed(null);
				} else if (pos == 7) {
					//refresh
				} else if (pos == 8) {
					// tag
					onTagButtonPressed(null);
				}
			}
		});
	}

	// Set up the video-player
	private void setUpVideoView() {
		videoView.setVideoPath(recording.getFile().getAbsolutePath());
		videoView.setMediaController(new MediaController(this));
	}
	
	// Set up the player
	private void setUpPlayer() {
		try {
			if (recording.isOriginal()) {
				// If there is a transcript for the recording
				// TranscriptPlayer is loaded
				if(recording.getTranscript() != null) {
					TranscriptPlayer player =
							new TranscriptPlayer(recording, this);
					
					// TODO: How to deal with transcripts needs to be decided
					findViewById(R.id.topLine).setVisibility(View.VISIBLE);
					findViewById(R.id.transcriptView).setVisibility(View.VISIBLE);
					findViewById(R.id.middleLine).setVisibility(View.VISIBLE);
					findViewById(R.id.transcriptView2).setVisibility(View.VISIBLE);
					findViewById(R.id.bottomLine).setVisibility(View.VISIBLE);

					setPlayer(player);
				} else {
					setPlayer(new SimplePlayer(recording, true));
				}
				
			} else {
				setPlayer(new InterleavedPlayer(recording));
				/*
				ImageButton respeakingButton =
						(ImageButton) findViewById(R.id.respeaking);
				respeakingButton.setVisibility(View.GONE);
				*/
			}
		} catch (IOException e) {
			//The player couldn't be created from the recoridng, so lets wrap
			//this activity up.
			Toast.makeText(this, "Failed to create player from recording.",
					Toast.LENGTH_LONG).show();
			ListenActivity.this.finish();
		}
	}

	private void setPlayer(SimplePlayer player) {
		this.player = player;
		fragment.setPlayer(player);
	}

	private void setPlayer(MarkedPlayer player) {
		this.player = player;
		fragment.setPlayer(player);
	}

	private void setPlayer(TranscriptPlayer player) {
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

	/**
	 * Change to the thumb respeak/interpret activity
	 *
	 * @param	respeakingButton	The thumb respeaking button
	 * @param	respeakingType		Derivative recording type(respeak/interpret)
	 */
	public void onRespeakButton(View respeakingButton, String respeakingType) {
		SharedPreferences preferences =
				PreferenceManager.getDefaultSharedPreferences(this);
		String respeakingMode = preferences.getString(
				AikumaSettings.RESPEAKING_MODE_KEY, "nothing");
		int rewindAmount = preferences.getInt("respeaking_rewind", 500);
		Log.i(TAG, 
				"respeakingMode: " + respeakingMode +", rewindAmount: " + rewindAmount);

		Intent intent;
		if(respeakingType.equals("respeak")) {
			intent = new Intent(this, RecordingLanguageActivity.class);
			
			intent.putParcelableArrayListExtra("languages", (ArrayList<Language>) recording.getLanguages());
			intent.putExtra("mode", respeakingMode);
		} else {
			intent = new Intent(this, RecordingLanguageActivity.class);

			intent.putExtra("mode", respeakingMode);
		}
		intent.putExtra("respeakingType", respeakingType);

		intent.putExtra("sourceId", recording.getId());
		intent.putExtra("ownerId", ownerId);
		intent.putExtra("versionName", versionName);
		intent.putExtra("sampleRate", recording.getSampleRate());
		intent.putExtra("rewindAmount", rewindAmount);
		
		startActivity(intent);
	}

	/**
	 * When the star button is pressed
	 *
	 * @param	view	The star button
	 */
	public void onStarButtonPressed(View view) {
		try {
			recording.star(AikumaSettings.getLatestVersion(), 
					AikumaSettings.getCurrentUserId());
		} catch (IOException e) {
			// This isn't thrown if the file already exists (rather, if the
			// file cannot be made for other reasons, so it's probably a
			// programmer bug or some sort of permissions error. To throw or
			// not to throw...
			throw new RuntimeException(e);
		}
		updateStarButton();
		updateFlagButton();
	}

	/**
	 * When the flag button is pressed
	 *
	 * @param	view	The flag button
	 */
	public void onFlagButtonPressed(View view) {
		try {
			recording.flag(AikumaSettings.getLatestVersion(), 
					AikumaSettings.getCurrentUserId());
		} catch (IOException e) {
			// This isn't thrown if the file already exists (rather, if the
			// file cannot be made for other reasons, so it's probably a
			// programmer bug or some sort of permissions error. To throw or
			// not to throw...
			throw new RuntimeException(e);
		}
		updateFlagButton();
	}
	
	/**
	 * When the share button is pressed
	 *
	 * @param	view	The share button
	 */
	public void onShareButtonPressed(View view) {
		String urlToShare = "http://repository.aikuma.org/item/" + 
				recording.getGroupId() + "#" + recording.getRespeakingId();
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, urlToShare);
		startActivity(Intent.createChooser(intent, "Share the link via"));
	}
	
	/**
	 * When the archive button is pressed
	 *
	 * @param	view	The share button
	 */
	public void onArchiveButtonPressed(View view) {
		if(!AikumaSettings.isPublicShareEnabled) {
			Intent intent = new Intent(this, ConsentActivity.class);
			startActivity(intent);
	
			return;
		}
		
		Intent intent = new Intent(this, GoogleCloudService.class);
		intent.putExtra(GoogleCloudService.ACTION_KEY, 
				recording.getVersionName() + "-" + recording.getId());
		intent.putExtra(GoogleCloudService.ARCHIVE_FILE_TYPE_KEY, "archive");
		intent.putExtra(GoogleCloudService.ACCOUNT_KEY, 
				AikumaSettings.getCurrentUserId());
		intent.putExtra(GoogleCloudService.TOKEN_KEY, 
				AikumaSettings.getCurrentUserToken());
		
		startService(intent);
		// Disable the button instantly because it can take a while until archive is finished
		quickMenu.setItemEnabledAt(3, false);
		quickMenu.setItemImageResourceAt(3, R.drawable.aikuma_grey);
		
		Toast.makeText(this, "Queued for public sharing", Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Callback for the private-share quickAction button
	 *
	 * @param	view	The share button
	 */
	public void onPrivateShareButtonPressed(View view) {
		final EditText emailInput = new EditText(this);
		emailInput.setInputType(InputType.TYPE_CLASS_TEXT | 
				InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

		final AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Private Share")
				.setMessage("Share the recording with ")
				.setView(emailInput)
				.setPositiveButton("Share", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO: Share the recording
						String msg = String.format("Share %s with %s", 
								recording.getId(), emailInput.getText());
						Toast.makeText(ListenActivity.this, 
								msg, Toast.LENGTH_LONG).show();
					}
				})
				.setNegativeButton("Cancel", null)
				.create();
		
		emailInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				if(!TextUtils.isEmpty(s) && 
						android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
					dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
				} else {
					dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
				}
			}
		});
		
		dialog.show();
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
	}
	
	/**
	 * Callback for an interpret quickaction button
	 * @param view	The interpret quickaction button
	 */
	public void onInterpretButtonPressed(View view) {
		onRespeakButton(view, "interpret");
	}
	
	/**
	 * Callback for an tagging quickaction button
	 * @param view	The tagging quickaction button
	 */
	public void onTagButtonPressed(View view) {
		Intent intent = new Intent(this, RecordingTagActivity.class);
		intent.putExtra("id", recording.getId());
		intent.putExtra("start", 0);
		startActivity(intent);
	}
	
	private void updateStarButton() {
		if(recording.isStarredByThisPhone(AikumaSettings.getLatestVersion(), 
				AikumaSettings.getCurrentUserId())) {
			quickMenu.setItemEnabledAt(0, false);
			quickMenu.setItemImageResourceAt(0, R.drawable.star_grey);
		} else {
			quickMenu.setItemEnabledAt(0, true);
			quickMenu.setItemImageResourceAt(0, R.drawable.star);
		}
	}
	
	private void updateFlagButton() {
		if(recording.isFlaggedByThisPhone(AikumaSettings.getLatestVersion(), 
				AikumaSettings.getCurrentUserId())) {
			quickMenu.setItemEnabledAt(1, false);
			quickMenu.setItemImageResourceAt(1, R.drawable.flag_grey);
		} else {
			quickMenu.setItemEnabledAt(1, true);
			quickMenu.setItemImageResourceAt(1, R.drawable.flag);
		}
	}
	
	private void updatePublicShareButton() {	// external share
		if(Aikuma.isArchived(AikumaSettings.getCurrentUserId(), recording) &&
				Aikuma.isDeviceOnline()) {
			quickMenu.setItemEnabledAt(2, true);
			quickMenu.setItemImageResourceAt(2, R.drawable.share);
		} else {
			quickMenu.setItemEnabledAt(2, false);
			quickMenu.setItemImageResourceAt(2, R.drawable.share_grey);
		}
	}
	
	private void updateArchiveButton() {
		if(Aikuma.isArchived(AikumaSettings.getCurrentUserId(), recording) ||
				!recording.getOwnerId().equals(AikumaSettings.getCurrentUserId())) {
			quickMenu.setItemEnabledAt(3, false);
			quickMenu.setItemImageResourceAt(3, R.drawable.aikuma_grey);
		} else {
			quickMenu.setItemEnabledAt(3, true);
			quickMenu.setItemImageResourceAt(3, R.drawable.aikuma_32);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (proximityDetector.isNear()) {
			return false;
		} else {
			return super.dispatchTouchEvent(event);
		}
	}

	/**
	 * Updates the view that tracks the number of times the recording has been
	 * listened to.
	 */
//	public void updateViewCount() {
//		TextView viewCount = (TextView) findViewById(R.id.viewCount);
//		int numViews = recording.numViews();
//		viewCount.setText("# views: " + numViews);
//	}

	private boolean phoneRespeaking = false;
	private Player player;
	private ListenFragment fragment;
	private VideoView videoView;
	private Recording recording;
	private String ownerId;
	private String versionName;
	private MenuBehaviour menuBehaviour;
	private ProximityDetector proximityDetector;
	
	private QuickActionMenu<Recording> quickMenu;
	
	private String googleAuthToken;
	
	private static final String TAG = "ListenActivity";
}
