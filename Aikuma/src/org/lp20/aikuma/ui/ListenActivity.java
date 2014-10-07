/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.util.Log;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.InterleavedPlayer;
import org.lp20.aikuma.R;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.storage.Data;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.InvalidAccessTokenException;
import org.lp20.aikuma.ui.sensors.ProximityDetector;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.ImageUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ListenActivity extends AikumaListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);
		menuBehaviour = new MenuBehaviour(this);
		fragment =
				(ListenFragment)
				getFragmentManager().findFragmentById(R.id.ListenFragment);
		videoView = (VideoView) findViewById(R.id.videoView);
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		googleAuthToken = AikumaSettings.googleAuthToken;
		// respeakings load
//		ExpandableListView respeakingsList = (ExpandableListView)
//				findViewById(R.id.respeakingsList);
//		List<Recording> respeakings = recording.getRespeakings();
//		ExpandableListAdapter adapter = new RespeakingsArrayAdapter(this, respeakings);
//		respeakingsList.setAdapter(adapter);
		
		setUpRecording();
		if(recording.isOriginal()) {
			List<Recording> respeakings = recording.getRespeakings();
			ArrayAdapter adapter = new RecordingArrayAdapter(this, respeakings);
			setListAdapter(adapter);
		}
	}

	// Prepares the recording
	private void setUpRecording() {
		Intent intent = getIntent();
		String id = (String)
				intent.getExtras().get("id");
		try {
			recording = Recording.read(id);
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
//		LinearLayout recordingInfoView = (LinearLayout)
//				findViewById(R.id.recordingInfo);
		LinearLayout originalImages = (LinearLayout)
				findViewById(R.id.speakerImages);
		for (String id : recording.getSpeakersIds()) {
			originalImages.addView(makeSpeakerImageView(id));
		}
		
		// Add the comment or movie icon
		LinearLayout icons = (LinearLayout)
				findViewById(R.id.recordingIcons);
		
		List<Recording> respeakings = recording.getRespeakings();
		int numComments = respeakings.size();
		if(numComments > 0) {
			icons.addView(makeRecordingInfoIcon(R.drawable.commentary_32));
		}
		if(recording.isMovie()) {
			icons.addView(makeRecordingInfoIcon(R.drawable.movie_32));
		}
	}

	// Prepares the displayed name for the recording (including other things
	// such as duration and date.
	private void setUpRecordingName() {
		LinearLayout recordingInfoView = (LinearLayout) 
				findViewById(R.id.selectedOriginal);
		
		TextView nameView = (TextView) findViewById(R.id.recordingName);
		TextView dateDurationView = 
				(TextView) findViewById(R.id.recordingDateDuration);
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
		TextView viewCountsView = (TextView) findViewById(R.id.viewCounts);
		viewCountsView.setText(String.valueOf(recording.numViews()));

		// Add the number of stars information
		TextView numStarsView = (TextView)
				findViewById(R.id.numStars);
		numStarsView.setText(String.valueOf(recording.numStars()));

		// Add the number of flags information
		TextView numFlagsView = (TextView)
				findViewById(R.id.numFlags);
		numFlagsView.setText(String.valueOf(recording.numFlags()));
		
		List<String> speakers = recording.getSpeakersIds();
		StringBuilder sb = new StringBuilder();
		for(String speakerId : speakers) {
			try {
				sb.append(Speaker.read(speakerId).getName()+", ");
				Log.i("hi", sb.toString());
			} catch (IOException e) {
				// If the reader can't be read for whatever reason 
				// (perhaps JSON file wasn't formatted correctly),
				// Empty the speakersName
				e.printStackTrace();
			}
		}

//		LinearLayout lbuf = (LinearLayout)
//				recordingInfoView.findViewById(R.id.recordingMetaInformation);
		TextView speakerNameView = (TextView)
				recordingInfoView.findViewById(R.id.speakerNames);
		speakerNameView.setText(sb.substring(0, sb.length()-2));
		Log.i("hi", speakerNameView+" " + sb.substring(0, sb.length()-2));
		
		
		//
		QuickActionItem starAct = new QuickActionItem("star", R.drawable.star);
		QuickActionItem flagAct = new QuickActionItem("flag", R.drawable.flag);
		QuickActionItem shareAct = 
				new QuickActionItem("share", R.drawable.share);
		
		quickMenu = new QuickActionMenu(this);
		
		quickMenu.addActionItem(starAct);
		quickMenu.addActionItem(flagAct);
		quickMenu.addActionItem(shareAct);
		
		if(googleAuthToken != null) {
			QuickActionItem archiveAct = 
					new QuickActionItem("archive", R.drawable.archive_32);
			quickMenu.addActionItem(archiveAct);
		}
		
		
		//setup the action item click listener
		quickMenu.setOnActionItemClickListener(new QuickActionMenu.OnActionItemClickListener() {			
			@Override
			public void onItemClick(int pos) {
				
				if (pos == 0) { //Add item selected
					onStarButtonPressed(null);
				} else if (pos == 1) { //Accept item selected
					onFlagButtonPressed(null);
				} else if (pos == 2) { //Upload item selected
					onShareButtonPressed(null);
				} else if (pos == 3) {
					onArchiveButtonPressed(null);
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
		ImageButton starButton = (ImageButton) findViewById(R.id.starButton);
		starButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ListenActivity.this.onStarButtonPressed(v);
			}	
		});
		
				
		// set up the flagButton
		ImageButton flagButton = (ImageButton) findViewById(R.id.flagButton);
		flagButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ListenActivity.this.onFlagButtonPressed(v);
			}	
		});
				
		// set up the shareButton
		ImageButton shareButton = (ImageButton)findViewById(R.id.shareButton);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ListenActivity.this.onShareButtonPressed(v);
			}	
		});
		*/
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

	// Set up the video-player
	private void setUpVideoView() {
		videoView.setVideoPath(recording.getFile().getAbsolutePath());
		videoView.setMediaController(new MediaController(this));
	}
	
	// Set up the player
	private void setUpPlayer() {
		try {
			if (recording.isOriginal()) {
				setPlayer(new SimplePlayer(recording, true));
			} else {
				setPlayer(new InterleavedPlayer(recording));
				ImageButton respeakingButton =
						(ImageButton) findViewById(R.id.respeaking);
				respeakingButton.setVisibility(View.GONE);
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
//		LinearLayout respeakingImages = (LinearLayout)
//				findViewById(R.id.RespeakingImages);
		for (final Recording respeaking : respeakings) {
			LinearLayout respeakingImageContainer = new LinearLayout(this);
			respeakingImageContainer.setOrientation(LinearLayout.VERTICAL);
			ImageView respeakingImage = new ImageView(this);
			respeakingImage.setAdjustViewBounds(true);
			respeakingImage.setMaxHeight(60);
			respeakingImage.setMaxWidth(60);
			respeakingImage.setPadding(5,5,5,5);
			if (respeaking.equals(recording)) {
				respeakingImage.setBackgroundColor(0xFFCC0000);
			}
			respeakingImage.setOnClickListener(new View.OnClickListener() {
				public void onClick(View _) {
					Intent intent = new Intent(ListenActivity.this,
							ListenActivity.class);
					intent.putExtra("id",
							respeaking.getId().toString());
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
//			respeakingImages.addView(respeakingImageContainer);
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
	public void onStart() {
		super.onStart();

		if(recording.isMovie()) {
			setUpVideoView();
		} else {
			videoView.setVisibility(View.GONE);
			
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			fragment = new ListenFragment();
			ft.add(R.id.listenFragment, fragment);
			ft.commit();
			
			setUpPlayer();
		}
		
//		setUpRespeakingImages();
		setUpRecordingInfo();
//		updateViewCount();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateStarButton();
		updateFlagButton();
		if(googleAuthToken != null) {
			updateArchiveButton();
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
		
		// respeakings load
//		ExpandableListView respeakingsList = (ExpandableListView)
//				findViewById(R.id.respeakingsList);
//		List<Recording> respeakings = recording.getRespeakings();
//		ExpandableListAdapter adapter = new RespeakingsArrayAdapter(this, respeakings);
//		respeakingsList.setAdapter(adapter);
				
		if(recording.isOriginal()) {
			List<Recording> respeakings = recording.getRespeakings();
			ArrayAdapter adapter = new RecordingArrayAdapter(this, respeakings);
			setListAdapter(adapter);
		}
		
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		Recording respeaking = (Recording) getListAdapter().getItem(position);
		Intent intent = new Intent(this, ListenRespeakingActivity.class);
		intent.putExtra("originalId", recording.getId());
		intent.putExtra("respeakingId", respeaking.getId());
		startActivity(intent);
		
//		Intent intent = new Intent(ListenActivity.this,
//				ListenActivity.class);
//		intent.putExtra("id",
//				respeaking.getId().toString());
//		startActivity(intent);
//		ListenActivity.this.finish();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		this.proximityDetector.stop();
	}

	/**
	 * Change to the thumb respeaking activity
	 *
	 * @param	respeakingButton	The thumb respeaking button
	 */
	public void onRespeakingButton(View respeakingButton) {
		SharedPreferences preferences =
				PreferenceManager.getDefaultSharedPreferences(this);
		String respeakingMode = preferences.getString(
				AikumaSettings.RESPEAKING_MODE_KEY, "nothing");
		Log.i("ListenActivity", "respeakingMode: " + respeakingMode);
		Intent intent;
		if (respeakingMode.equals("phone")) {
			intent = new Intent(this, PhoneRespeakActivity.class);
		} else {
			// Lets just default to thumb respeaking
			intent = new Intent(this, ThumbRespeakActivity.class);
		}
		intent.putExtra("sourceId", recording.getId());
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
		intent.putExtra("sourceId", recording.getId());
		intent.putExtra("sampleRate", recording.getSampleRate());
		startActivity(intent);
	}

	/**
	 * When the star button is pressed
	 *
	 * @param	view	The star button
	 */
	public void onStarButtonPressed(View view) {
		try {
			recording.star();
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
			recording.flag();
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
	 * @param	view	The share button
	 */
	public void onArchiveButtonPressed(View view) {
		Intent intent = new Intent(this, GoogleCloudService.class);
		intent.putExtra("id", recording.getId());
		intent.putExtra("type", "recording");
		startService(intent);
	}
	
	private void updateStarButton() {
		if(recording.isStarredByThisPhone()) {
			quickMenu.setItemEnabledAt(0, false);
			quickMenu.setItemImageResourceAt(0, R.drawable.star_grey);
		} else {
			quickMenu.setItemEnabledAt(0, true);
			quickMenu.setItemImageResourceAt(0, R.drawable.star);
		}
	}
	
	private void updateFlagButton() {
		if(recording.isFlaggedByThisPhone()) {
			quickMenu.setItemEnabledAt(1, false);
			quickMenu.setItemImageResourceAt(1, R.drawable.flag_grey);
		} else {
			quickMenu.setItemEnabledAt(1, true);
			quickMenu.setItemImageResourceAt(1, R.drawable.flag);
		}
	}
	
	private void updateArchiveButton() {
		if(recording.isArchived()) {
			quickMenu.setItemEnabledAt(3, false);
			quickMenu.setItemImageResourceAt(3, R.drawable.archive_grey);
		} else {
			quickMenu.setItemEnabledAt(3, true);
			quickMenu.setItemImageResourceAt(3, R.drawable.archive_32);
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
	private MenuBehaviour menuBehaviour;
	private SimpleDateFormat simpleDateFormat;
	private ProximityDetector proximityDetector;
	
	private QuickActionMenu quickMenu;
	
	private String googleAuthToken;
}
