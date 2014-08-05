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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import java.util.ArrayList;
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
import org.lp20.aikuma.audio.MarkedPlayer;
import org.lp20.aikuma.audio.TranscriptPlayer;
import org.lp20.aikuma.R;
import org.lp20.aikuma.storage.Data;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.InvalidAccessTokenException;
import org.lp20.aikuma.ui.sensors.ProximityDetector;
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
public class ListenActivity extends AikumaActivity {

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
		
		googleAuthToken = getIntent().getExtras().getString("token");
	
		setUpRecording();
		setUpRespeakings();
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
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateStarButton();
		updateFlagButton();
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
	
	@Override
	public void onStop() {
		super.onStop();
		this.player.release();
	}

	// Prepares the original recording 
	private void setUpRecording() {
		Intent intent = getIntent();
		String id = (String)
				intent.getExtras().get("id");
		try {
			recording = Recording.read(id);
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
					intent.putExtra("respeakingId", respeaking.getId());
					intent.putExtra("token", googleAuthToken);
					startActivity(intent);
				}
			});
		}
	}


	// Creates the quickMenu for the original recording 
	//(quickMenu: star/flag/share/archive)
	private void setUpQuickMenu() {
		quickMenu = new QuickActionMenu(this);
		
		QuickActionItem starAct = new QuickActionItem("star", R.drawable.star);
		QuickActionItem flagAct = new QuickActionItem("flag", R.drawable.flag);
		QuickActionItem shareAct = 
				new QuickActionItem("share", R.drawable.share);
		
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
					setPlayer(player);
				} else {
					setPlayer(new SimplePlayer(recording, true));
				}
				
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
	 * Change to the thumb respeaking activity
	 *
	 * @param	respeakingButton	The thumb respeaking button
	 */
	public void onRespeakingButton(View respeakingButton) {
		SharedPreferences preferences =
				PreferenceManager.getDefaultSharedPreferences(this);
		String respeakingMode = preferences.getString(
				"respeaking_mode", "nothing");
		int rewindAmount = preferences.getInt("respeaking_rewind", 1000);
		Log.i(TAG, 
				"respeakingMode: " + respeakingMode +", rewindAmount: " + rewindAmount);
		Intent intent;
		if (respeakingMode.equals("phone")) {
			intent = new Intent(this, PhoneRespeakActivity.class);
		} else {
			// Lets just default to thumb respeaking
			intent = new Intent(this, ThumbRespeakActivity.class);
		}
		intent.putExtra("sourceId", recording.getId());
		intent.putExtra("sampleRate", recording.getSampleRate());
		intent.putExtra("rewindAmount", rewindAmount);
		
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
		new archiveTask().execute();
	}
	
	/**
	 * Asynchronous task to upload file and metadata to google-server
	 * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
	 *
	 */
	private class archiveTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			File file = recording.getFile();
			File metadataFile = recording.getMetadataFile();
			Data data = Data.fromFile(file);
			if (data == null) {	
				return 0;		
			}

			GoogleDriveStorage gd = new GoogleDriveStorage(googleAuthToken);
			FusionIndex fi = new FusionIndex(googleAuthToken);
			JSONObject jsonfile;
			try {
				jsonfile = (JSONObject) JSONValue.parse(new FileReader(metadataFile));
			} catch (FileNotFoundException e) {
				return 1;	
			}
			Map<String, String> metadata = new HashMap<String,String>();
			JSONArray speakers_arr = (JSONArray) jsonfile.get("people");
			String speakers = "";
			String joiner = "";
			for (Object obj: (JSONArray) jsonfile.get("people")) {
				speakers += joiner + (String) obj;
				joiner = "|";
			}
			String languages = "";
			joiner = "";
			for (Object obj: (JSONArray) jsonfile.get("languages")) {
				String lang = (String) ((JSONObject) obj).get("code");
				languages += joiner + lang;
				joiner = "|";
				break;  // TODO: use only the first language for now
			}
			metadata.put("data_store_uri", "NA");  // TODO: obtain real url
			metadata.put("item_id", (String) jsonfile.get("recording"));
			metadata.put("file_type", (String) jsonfile.get("type"));
			metadata.put("speakers", speakers);
			metadata.put("languages", languages);

			Log.i("hi", "meta: " + metadata.toString());

			if (gd.store(file.getName(), data) && 
					fi.index(file.getName(), metadata)) {
				Log.i("hi", "success");
				return 3;
			}
			else {
				Log.i("hi", "fail");
				return 2;
			}
		}
		
		protected void onPostExecute(Integer resultCode) {
			switch(resultCode) {
			case 0:
				Toast.makeText(ListenActivity.this, "Failed to open file", 
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				Toast.makeText(ListenActivity.this, "Failed to open metaFile",
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(ListenActivity.this, "Upload failed", 
						Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(ListenActivity.this, "Upload succeeded", 
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
		
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
	
	private static final String TAG = "ListenActivity";
}
