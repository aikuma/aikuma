package org.lp20.aikuma.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.lp20.aikuma.R;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.ui.sensors.ProximityDetector;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		originalListenFragment = new ListenFragment();
		respeakingListenFragment = new ListenFragment();
		
	
		//setUpRecording();
		//setUpPlayer();
//		setUpRespeakingImages();
		//setUpRecordingInfo();
//		updateViewCount();
		
		// respeakings load
//		ExpandableListView respeakingsList = (ExpandableListView)
//				findViewById(R.id.respeakingsList);
//		List<Recording> respeakings = recording.getRespeakings();
//		ExpandableListAdapter adapter = new RespeakingsArrayAdapter(this, respeakings);
//		respeakingsList.setAdapter(adapter);
		
	}
	
	private void addNewFragment(FragmentManager fm, 
			int containerId, Fragment fragment, String tag) {
		
	    FragmentTransaction ft = fm.beginTransaction();
	    ft.add(containerId, fragment, tag);
	    
	    //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    //ft.addToBackStack(null);
	    ft.commit();
	}
	
	private void setUp() {
		setUpRecordings();
		setUpPlayers();
		setUpRecordingsInfo();
	}
	
	
	// Prepares the recording
	private void setUpRecordings() {
		Intent intent = getIntent();
		String originalId = (String)
				intent.getExtras().get("originalId");
		String respeakingId = (String)
				intent.getExtras().get("respeakingId");

		try {
			original = Recording.read(originalId);
			List<Recording> respeakings = original.getRespeakings();
			for(Recording buf : respeakings) {
				
				if(buf.getId().equals(respeakingId)) {
					respeaking = buf;
					break;
				}
			}
			//setUpRecordingName();
		} catch (IOException e) {
			//The recording metadata cannot be read, so let's wrap up this
			//activity.
			Toast.makeText(this, "Failed to read recording metadata.",
					Toast.LENGTH_LONG).show();
			ListenRespeakingActivity.this.finish();
		}
	}

	// Prepares the information pertaining to the recording
	private void setUpRecordingsInfo() {
		
//		LinearLayout recordingInfoView = (LinearLayout)
//				findViewById(R.id.recordingInfo);
		LinearLayout originalInfoView = 
				(LinearLayout) findViewById(R.id.selectedOriginal);
		LinearLayout respeakingInfoView = 
				(LinearLayout) findViewById(R.id.selectedRespeaking);
		
		setUpRecordingInfo(originalInfoView, original);
		setUpRecordingInfo(respeakingInfoView, respeaking);
		
		originalQuickMenu = new QuickActionMenu(this);
		respeakingQuickMenu = new QuickActionMenu(this);
		setUpRecordingInterface(originalInfoView, 
				originalQuickMenu, original);
		setUpRecordingInterface(respeakingInfoView, 
				respeakingQuickMenu, respeaking);
	}

	// Prepares the displayed name for the recording (including other things
	// such as duration and date.
	private void setUpRecordingInfo(LinearLayout recordingInfoView, 
			Recording recording) {			
		TextView nameView = (TextView) recordingInfoView.
				findViewById(R.id.recordingName);
		TextView dateDurationView = (TextView) recordingInfoView.
				findViewById(R.id.recordingDateDuration);
//		TextView langView = (TextView) findViewById(R.id.recordingLangCode);
		

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

		// Add the number of views information
		TextView viewCountsView = (TextView) 
				recordingInfoView.findViewById(R.id.viewCounts);
		viewCountsView.setText(String.valueOf(recording.numViews()));
		
		// Add the number of stars information
		TextView numStarsView = (TextView)
				recordingInfoView.findViewById(R.id.numStars);
		numStarsView.setText(String.valueOf(recording.numStars()));

		// Add the number of flags information
		TextView numFlagsView = (TextView)
				recordingInfoView.findViewById(R.id.numFlags);
		numFlagsView.setText(String.valueOf(recording.numFlags()));
		
		// Add the speakers' images
		LinearLayout speakerImages = (LinearLayout)
				recordingInfoView.findViewById(R.id.speakerImages);
		for (String speakerId : recording.getSpeakersIds()) {
			speakerImages.addView(makeSpeakerImageView(speakerId));
		}
		
		// Add the speakers' names
		List<String> speakers = recording.getSpeakersIds();
		StringBuilder sb = new StringBuilder();
		for(String speakerId : speakers) {
			try {
				sb.append(Speaker.read(speakerId).getName()+", ");
			} catch (IOException e) {
				// If the reader can't be read for whatever reason 
				// (perhaps JSON file wasn't formatted correctly),
				// Empty the speakersName
				e.printStackTrace();
			}
		}
		TextView speakerNameView = (TextView)
				recordingInfoView.findViewById(R.id.speakerNames);
		speakerNameView.setText(sb.substring(0, sb.length()-2));
		
		// Add the comment or movie icon 
		LinearLayout icons = (LinearLayout)
				recordingInfoView.findViewById(R.id.recordingIcons);
		
		List<Recording> respeakings = recording.getRespeakings();
		int numComments = respeakings.size();
		if(numComments > 0) {
			icons.addView(makeRecordingInfoIcon(R.drawable.commentary_32));
		}
		if(recording.isMovie()) {
			icons.addView(makeRecordingInfoIcon(R.drawable.commentary_32));
		}
		
	}
	
	private void setUpRecordingInterface(LinearLayout recordingInfoView, 
			final QuickActionMenu quickMenu, final Recording recording){
		
		QuickActionItem starAct = new QuickActionItem("star", R.drawable.star);
		QuickActionItem flagAct = new QuickActionItem("flag", R.drawable.flag);
		QuickActionItem shareAct = new QuickActionItem("share", R.drawable.share);
		
		quickMenu.addActionItem(starAct);
		quickMenu.addActionItem(flagAct);
		quickMenu.addActionItem(shareAct);
		
		//setup the action item click listener
		quickMenu.setOnActionItemClickListener(new QuickActionMenu.OnActionItemClickListener() {			
			@Override
			public void onItemClick(int pos) {
				
				if (pos == 0) { //Add item selected
					onStarButtonPressed(recording);
				} else if (pos == 1) { //Accept item selected
					onFlagButtonPressed(recording);
				} else if (pos == 2) { //Upload item selected
					onShareButtonPressed(recording);
				}	
			}
		});
		
		recordingInfoView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				quickMenu.show(v);
				return false;
			}
			
		});
		
		
		/*
		// set up the starButton
		ImageButton starButton = (ImageButton)
				recordingInterface.findViewById(R.id.starButton);
		starButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ListenRespeakingActivity.this.onStarButtonPressed(recording);
			}	
		});
		
		
		// set up the flagButton
		ImageButton flagButton = (ImageButton)
				recordingInterface.findViewById(R.id.flagButton);
		flagButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ListenRespeakingActivity.this.onFlagButtonPressed(recording);
			}	
		});
		
		// set up the shareButton
		ImageButton shareButton = (ImageButton)
				recordingInterface.findViewById(R.id.shareButton);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ListenRespeakingActivity.this.onShareButtonPressed(recording);
			}	
		});*/
	}
	
	/**
	 * Create the view for a icon with resourceId
	 * 
	 * @param resourceId	The id of a drawalbe image
	 * @return
	 */
	private ImageView makeRecordingInfoIcon(int resourceId) {
		ImageView iconImage = new ImageView(this);
		iconImage.setImageResource(resourceId);
		iconImage.setAdjustViewBounds(true);
		iconImage.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		
		return iconImage;
	}

	// Makes the imageview for a given speaker
	private ImageView makeSpeakerImageView(String speakerId) {
		ImageView speakerImage = new ImageView(this);
		speakerImage.setAdjustViewBounds(true);
		speakerImage.setMaxHeight(60);
		speakerImage.setMaxWidth(60);
		try {
			speakerImage.setImageBitmap(Speaker.getSmallImage(speakerId));
		} catch (IOException e) {
			// Not much can be done if the image can't be loaded.
		}
		return speakerImage;
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

	@Override
	public void onStart() {
		super.onStart();
		
		FragmentManager fm = getFragmentManager();
		addNewFragment(fm, R.id.recordingPlayerInterface, 
				originalListenFragment, "original");
		addNewFragment(fm, R.id.respeakingPlayerInterface, 
				respeakingListenFragment, "respeak");
		fm.executePendingTransactions();
		
		setUp();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateStarButtons();
		updateFlagButtons();
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
		
		// respeakings load
//		ExpandableListView respeakingsList = (ExpandableListView)
//				findViewById(R.id.respeakingsList);
//		List<Recording> respeakings = recording.getRespeakings();
//		ExpandableListAdapter adapter = new RespeakingsArrayAdapter(this, respeakings);
//		respeakingsList.setAdapter(adapter);
	}

	
	@Override
	public void onPause() {
		super.onPause();
		this.proximityDetector.stop();
	}

	/**
	 * When the star button is pressed
	 * 
	 * @param recording		Recording object where star is recorded
	 */
	public void onStarButtonPressed(Recording recording) {
		try {
			recording.star();
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
			recording.flag();
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

	private void updateStarButtons() {
		updateStarButton(originalQuickMenu, original);
		updateStarButton(respeakingQuickMenu, respeaking);
	}
	
	private void updateFlagButtons() {
		updateFlagButton(originalQuickMenu, original);
		updateFlagButton(respeakingQuickMenu, respeaking);
	}
	
	private void updateStarButton(QuickActionMenu quickMenu, 
			Recording recording) {
		if(recording.isStarredByThisPhone()) {
			quickMenu.setItemEnabledAt(0, false);
			quickMenu.setItemImageResourceAt(0, R.drawable.star_grey);
		} else {
			quickMenu.setItemEnabledAt(0, true);
			quickMenu.setItemImageResourceAt(0, R.drawable.star);
		}
	}
	
	private void updateFlagButton(QuickActionMenu quickMenu, 
			Recording recording) {
		if(recording.isFlaggedByThisPhone()) {
			quickMenu.setItemEnabledAt(1, false);
			quickMenu.setItemImageResourceAt(1, R.drawable.flag_grey);
		} else {
			quickMenu.setItemEnabledAt(1, true);
			quickMenu.setItemImageResourceAt(1, R.drawable.flag);
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
	private SimpleDateFormat simpleDateFormat;
	private ProximityDetector proximityDetector;
	
	
	private QuickActionMenu originalQuickMenu;
	private QuickActionMenu respeakingQuickMenu;
}
