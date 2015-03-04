package org.lp20.aikuma.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lp20.aikuma2.R;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.model.Recording;

import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.ui.sensors.ProximityDetector;
import org.lp20.aikuma.util.AikumaSettings;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
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
	public void onStart() {
		super.onStart();
		setUp();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateStarButtons();
		updateFlagButtons();
		if(googleAuthToken != null) {
			updateArchiveButtons();
		}
		
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
		
		quickMenu.addActionItem(starAct);
		quickMenu.addActionItem(flagAct);
		quickMenu.addActionItem(shareAct);
		
		if(googleAuthToken != null) {
			QuickActionItem archiveAct = 
					new QuickActionItem("share", R.drawable.aikuma_32);
			quickMenu.addActionItem(archiveAct);
		}
		
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
//				ImageButton respeakingButton =
//						(ImageButton) findViewById(R.id.respeaking);
//				respeakingButton.setVisibility(View.GONE);
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
		String urlToShare = "http://example.com/" + recording.getGroupId();
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
		Intent intent = new Intent(this, GoogleCloudService.class);
		intent.putExtra(GoogleCloudService.ACTION_KEY, 
				recording.getVersionName() + "-" + recording.getId());
		intent.putExtra(GoogleCloudService.ARCHIVE_FILE_TYPE_KEY, "recording");
		intent.putExtra(GoogleCloudService.ACCOUNT_KEY, 
				AikumaSettings.getCurrentUserId());
		intent.putExtra(GoogleCloudService.TOKEN_KEY, 
				AikumaSettings.getCurrentUserToken());
		
		startService(intent);
	}

	private void updateStarButtons() {
		updateStarButton(originalQuickMenu, original);
		updateStarButton(respeakingQuickMenu, respeaking);
	}
	
	private void updateFlagButtons() {
		updateFlagButton(originalQuickMenu, original);
		updateFlagButton(respeakingQuickMenu, respeaking);
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

	private void updateArchiveButton(QuickActionMenu<Recording> quickMenu, 
			Recording recording) {
		if(recording.isArchived() || 
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
