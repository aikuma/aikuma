package org.lp20.aikuma.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lp20.aikuma2.R;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.model.Recording;

import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.ui.sensors.ProximityDetector;
import org.lp20.aikuma.util.AikumaSettings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**	An activiy executed after ListenActivity
 * 	Show two recordings(original and respeaking)
 * 
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class ListenRespeakingActivity extends AikumaActivity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen_respeaking);
		menuBehaviour = new MenuBehaviour(this);

		originalListenFragment = new ListenFragment();
		respeakingListenFragment = new ListenFragment();
		originalListenFragment.setOtherPlayer(respeakingListenFragment);
		respeakingListenFragment.setOtherPlayer(originalListenFragment);
		
		FragmentManager fm = getFragmentManager();
		addNewFragment(fm, R.id.recordingPlayerInterface, 
				originalListenFragment, "original");
		addNewFragment(fm, R.id.respeakingPlayerInterface, 
				respeakingListenFragment, "respeak");
		fm.executePendingTransactions();
		
		googleAuthToken = AikumaSettings.getCurrentUserToken();
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		setUp();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateStarButtons();
		updateFlagButtons();
		updatePublicShareButtons();
		updateArchiveButtons();
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
	
	private void addNewFragment(FragmentManager fm, 
			int containerId, Fragment fragment, String tag) {
		
	    FragmentTransaction ft = fm.beginTransaction();
	    ft.replace(containerId, fragment, tag);
	    
	    //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    //ft.addToBackStack(null);
	    ft.commit();
	}
	
	private void setUp() {
		setUpRecordings();
		setUpPlayers();
	}
	
	
	// Prepares the recording
	private void setUpRecordings() {
		Intent intent = getIntent();
		String originalId = (String)
				intent.getExtras().get("originalId");
		String respeakingId = (String)
				intent.getExtras().get("respeakingId");

		String originalVerName = (String) intent.getExtras().get("originalVerName");
		String originalOwnerId = (String) intent.getExtras().get("originalOwnerId");
		
		setUpOriginal(originalVerName, originalOwnerId, originalId);
		setUpRespeaking(respeakingId);
	}
	
	// Prepares the selected original
	private void setUpOriginal(String versionName, 
			String ownerId, String originalId) {
		try {
			original = Recording.read(versionName, ownerId, originalId);
			originalQuickMenu = new QuickActionMenu<Recording>(this);
			setUpQuickMenu(originalQuickMenu, original);
			List<Recording> originalBox = new ArrayList<Recording>();
			originalBox.add(original);
			ArrayAdapter<Recording> adapter = 
					new RecordingArrayAdapter(this, originalBox, originalQuickMenu);
			
			ListView originalView = 
					(ListView) findViewById(R.id.selectedOriginal);
			originalView.setAdapter(adapter);
		} catch (IOException e) {
			//The recording metadata cannot be read, so let's wrap up this
			//activity.
			Toast.makeText(this, "Failed to read recording metadata.",
					Toast.LENGTH_LONG).show();
			ListenRespeakingActivity.this.finish();
		}
	}
	
	// Prepares the selected respeaking
	private void setUpRespeaking(String respeakingId) {
		List<Recording> respeakings = original.getRespeakings();

		for(Recording buf : respeakings) {
			if(buf.getId().equals(respeakingId)) {
				respeaking = buf;
				break;
			}
		}
		respeakingQuickMenu = new QuickActionMenu<Recording>(this);
		setUpQuickMenu(respeakingQuickMenu, respeaking);
		List<Recording> respeakingBox = new ArrayList<Recording>();
		respeakingBox.add(respeaking);
		ArrayAdapter<Recording> adapter =
				new RecordingArrayAdapter(this, respeakingBox, respeakingQuickMenu);
		
		ListView respeakingView =
				(ListView) findViewById(R.id.selectedRespeaking);
		respeakingView.setAdapter(adapter);
	}
	
	// Prepares the quickMenu(star/flag/share/archive)
	private void setUpQuickMenu(QuickActionMenu<Recording> quickMenu, 
			final Recording recording){	
		QuickActionItem starAct = new QuickActionItem("star", R.drawable.star);
		QuickActionItem flagAct = new QuickActionItem("flag", R.drawable.flag);
		QuickActionItem shareAct = new QuickActionItem("share", R.drawable.share);
		QuickActionItem respeakAct = new QuickActionItem("respeak", R.drawable.respeak_32);
		QuickActionItem translateAct = new QuickActionItem("translate", R.drawable.translate_32);
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
		/*
		quickMenu.addActionItem(respeakAct);
		quickMenu.addActionItem(translateAct);
		quickMenu.addActionItem(refresthAct);
		*/
		// Tagging buttons
		QuickActionItem tagAct =
				new QuickActionItem("tag", R.drawable.tag_32);
		quickMenu.addActionItem(tagAct);		
		
		//setup the action item click listener
		quickMenu.setOnActionItemClickListener(new QuickActionMenu.OnActionItemClickListener<Recording>() {			
			@Override
			public void onItemClick(int pos, Recording recording) {
				
				if (pos == 0) { //Add item selected
					onStarButtonPressed(recording);
				} else if (pos == 1) { //Accept item selected
					onFlagButtonPressed(recording);
				} else if (pos == 2) { //Upload item selected
					onShareButtonPressed(recording);
				} else if (pos == 3) {
					onArchiveButtonPressed(recording);
				} else if (pos == 4) {
					onPrivateShareButtonPressed(recording);
				} else if (pos == 5) {
					//respeak
					//onRespeakButton(recording);
					onTagButtonPressed(recording);
				} else if (pos == 6) {
					//translate
					//onInterpretButtonPressed(recording);
				} else if (pos == 7) {
					//refresh
				}
			}
		});
	}

	private void setUpPlayers() {
		setUpPlayer(originalListenFragment, original);
		setUpPlayer(respeakingListenFragment, respeaking);
	}

	// Set up the player
	private void setUpPlayer(ListenFragment playerInterface, 
			Recording recording) {

		try {
			if (recording.isOriginal()) {
				setPlayer(playerInterface, new SimplePlayer(recording, true));
			} else {
				setPlayer(playerInterface, new InterleavedPlayer(recording));
			}
		} catch (IOException e) {
			//The player couldn't be created from the recoridng, so lets wrap
			//this activity up.
			Toast.makeText(this, "Failed to create player from recording.",
					Toast.LENGTH_LONG).show();
			ListenRespeakingActivity.this.finish();
		}
	}


	private void setPlayer(ListenFragment playerInterface, 
			SimplePlayer player) {
		playerInterface.setPlayer(player);
	}

	private void setPlayer(ListenFragment playerInterface, 
			InterleavedPlayer player) {
		ListenFragment lf = (ListenFragment)
				getFragmentManager().findFragmentByTag("respeak");
		playerInterface.setPlayer(player);
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
	 * When the star button is pressed
	 * 
	 * @param recording		Recording object where star is recorded
	 */
	public void onStarButtonPressed(Recording recording) {
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
		updateStarButtons();
		updateFlagButtons();
	}

	/**
	 * When the flag button is pressed
	 *
	 *  @param recording	Recording object where flag is recorded
	 */
	public void onFlagButtonPressed(Recording recording) {
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
		updateFlagButtons();
	}
	
	/**
	 * When the share button is pressed
	 *
	 * @param recording	Recording object which will be shared
	 */
	public void onShareButtonPressed(Recording recording) {
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
	 * @param recording	Recording object which will be shared
	 */
	public void onArchiveButtonPressed(Recording recording) {
		if(!AikumaSettings.isPublicShareEnabled) {
			Toast.makeText(this, 
					"Before sharing, please see the sharing agreement in the settings menu.", 
					Toast.LENGTH_LONG).show();
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
		if(recording.isOriginal()) {
			originalQuickMenu.setItemEnabledAt(3, false);
			originalQuickMenu.setItemImageResourceAt(3, R.drawable.aikuma_grey);
		} else {
			respeakingQuickMenu.setItemEnabledAt(3, false);
			respeakingQuickMenu.setItemImageResourceAt(3, R.drawable.aikuma_grey);
		}
	}
	
	/**
	 * Callback for the private-share button
	 *
	 * @param recording	Recording object which will be shared
	 */
	public void onPrivateShareButtonPressed(Recording recording) {
		final EditText emailInput = new EditText(this);
		emailInput.setInputType(InputType.TYPE_CLASS_TEXT | 
				InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

		final String recId = recording.getId();
		final AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Private Share")
				.setMessage("Share the recording with ")
				.setView(emailInput)
				.setPositiveButton("Share", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO: Share the recording
						String msg = String.format("Share %s with %s", 
								recId, emailInput.getText());
						Toast.makeText(ListenRespeakingActivity.this, 
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
	 * Callback for the tagging button
	 *
	 * @param recording	Recording object which will be tagged
	 */
	public void onTagButtonPressed(Recording recording) {
		Intent intent = new Intent(this, RecordingTagActivity.class);
		intent.putExtra("id", recording.getId());
		intent.putExtra("start", 1);
		startActivity(intent);
	}

	private void updateStarButtons() {
		updateStarButton(originalQuickMenu, original);
		updateStarButton(respeakingQuickMenu, respeaking);
	}
	
	private void updateFlagButtons() {
		updateFlagButton(originalQuickMenu, original);
		updateFlagButton(respeakingQuickMenu, respeaking);
	}
	
	private void updatePublicShareButtons() {
		updatePublicShareButton(originalQuickMenu, original);
		updatePublicShareButton(respeakingQuickMenu, respeaking);
	}
	
	private void updateArchiveButtons() {
		updateArchiveButton(originalQuickMenu, original);
		updateArchiveButton(respeakingQuickMenu, respeaking);
	}
	
	private void updateStarButton(QuickActionMenu<Recording> quickMenu, 
			Recording recording) {
		if(recording.isStarredByThisPhone(AikumaSettings.getLatestVersion(), 
				AikumaSettings.getCurrentUserId())) {
			quickMenu.setItemEnabledAt(0, false);
			quickMenu.setItemImageResourceAt(0, R.drawable.star_grey);
		} else {
			quickMenu.setItemEnabledAt(0, true);
			quickMenu.setItemImageResourceAt(0, R.drawable.star);
		}
	}
	
	private void updateFlagButton(QuickActionMenu<Recording> quickMenu, 
			Recording recording) {
		if(recording.isFlaggedByThisPhone(AikumaSettings.getLatestVersion(), 
				AikumaSettings.getCurrentUserId())) {
			quickMenu.setItemEnabledAt(1, false);
			quickMenu.setItemImageResourceAt(1, R.drawable.flag_grey);
		} else {
			quickMenu.setItemEnabledAt(1, true);
			quickMenu.setItemImageResourceAt(1, R.drawable.flag);
		}
	}
	
	private void updatePublicShareButton(QuickActionMenu<Recording> quickMenu, 
			Recording recording) {
		if(Aikuma.isArchived(AikumaSettings.getCurrentUserId(), recording) &&
				Aikuma.isDeviceOnline()) {
			quickMenu.setItemEnabledAt(2, true);
			quickMenu.setItemImageResourceAt(2, R.drawable.share);
		} else {
			quickMenu.setItemEnabledAt(2, false);
			quickMenu.setItemImageResourceAt(2, R.drawable.share_grey);
		}
	}

	private void updateArchiveButton(QuickActionMenu<Recording> quickMenu, 
			Recording recording) {
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
	private Recording original;
	private Recording respeaking;
	private ListenFragment originalListenFragment;
	private ListenFragment respeakingListenFragment;
	private MenuBehaviour menuBehaviour;
	private ProximityDetector proximityDetector;
	
	
	private QuickActionMenu<Recording> originalQuickMenu;
	private QuickActionMenu<Recording> respeakingQuickMenu;
	
	private String googleAuthToken;
}
