/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.model.FileModel;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.storage.DataStore;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.Index;
import org.lp20.aikuma.storage.Utils;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 * 
 * Activity class dealing with recording item search interface
 */
public class CloudSearchActivity extends AikumaListActivity {
	
	private static final String TAG = "CloudSearchActivity";
	
	private String googleEmailAccount;
	private String googleAuthToken;
	
	private MediaPlayer mediaPlayer;
	private boolean isMediaPlayerPrepared;
	private boolean isMediaPlayerReleased;
	
	private EditText searchQueryView;
	
	private QuickActionMenu<Recording> quickMenu;
	
	// Search results to be shown
	private RecordingArrayAdapter adapter;
	private Parcelable listViewState;
	private List<Recording> recordings;
	
	// List of item-ids to be downloaded
	private List<String> itemIdsToDownload;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_search);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		googleEmailAccount = AikumaSettings.getCurrentUserId();
		googleAuthToken = AikumaSettings.getCurrentUserToken();
		
		mediaPlayer = new MediaPlayer();
		isMediaPlayerPrepared = false;
		isMediaPlayerReleased = false;
		
		setUpQuickMenu();
		recordings = new ArrayList<Recording>();
		itemIdsToDownload = new ArrayList<String>();
		
		adapter = new RecordingArrayAdapter(this, recordings, quickMenu);
		setListAdapter(adapter);
		
		searchQueryView = (EditText) findViewById(R.id.searchQuery);
		
		searchQueryView.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if((event.getAction() == KeyEvent.ACTION_DOWN && 
						(event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
					onSearchButton(null);
					return true;
				}
				return false;
			}
			
		});		
			
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(listViewState != null) {
			getListView().onRestoreInstanceState(listViewState);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(isMediaPlayerPrepared)
			mediaPlayer.pause();
		listViewState = getListView().onSaveInstanceState();
		MainActivity.locationDetector.stop();
		
		new GetSearchResultsTask(1, new ArrayList<String>(itemIdsToDownload), 
				googleEmailAccount, googleAuthToken).execute();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		isMediaPlayerReleased = true;
		mediaPlayer.release();
	}
	
	@Override
	public void onBackPressed() {
		Toast.makeText(CloudSearchActivity.this, 
				itemIdsToDownload.size() + " items are queued to be downloaded", 
				Toast.LENGTH_LONG).show();
		this.finish();
	}
	
	
	/**
	 * Search the recordings using the query
	 * 
	 * @param view	View having the query
	 */
	public void onSearchButton(View view) {
		if(!Aikuma.isDeviceOnline())
			Aikuma.showAlertDialog(this, "Network is disconnected");
		
		recordings.clear();
		String langQuery = searchQueryView.getText().toString().toLowerCase();
		List<String> query = new ArrayList<String>();
		query.add(langQuery);
		
		new GetSearchResultsTask(0, query, googleEmailAccount, googleAuthToken).execute();
	}

	private void showRecordingsOnCloud() {
		// If search result is zero, clear the view
		//if(recordings.size() == 0)
		//	return;
			
		adapter.notifyDataSetChanged();
	}
	
	// Creates the quickMenu for the original recording 
	//(quickMenu: download)
	private void setUpQuickMenu() {
		quickMenu = new QuickActionMenu<Recording>(this);
		
		if(AikumaSettings.getCurrentUserToken() != null) {
			QuickActionItem sampleDownloadAct = 
					new QuickActionItem("Sample", R.drawable.download_32);
			QuickActionItem samplePlayAct =
					new QuickActionItem("Sample", R.drawable.play_32);
			QuickActionItem itemDownloadAct = 
					new QuickActionItem("Item", R.drawable.download_32);
			
			quickMenu.addActionItem(sampleDownloadAct);
			quickMenu.addActionItem(samplePlayAct);
			quickMenu.addActionItem(itemDownloadAct);
			
			//setup the popup event listener
			quickMenu.setOnQuickMenuPopupListener(
					new QuickActionMenu.OnQuickMenuPopupListener<Recording>() {
						@Override
						public void onPopup(Recording item) {
							// Initialize the action buttons
							if(item.getPreviewFile() != null) {
								quickMenu.setItemEnabledAt(0, false);
								quickMenu.setItemImageResourceAt(0, R.drawable.download_32_grey);
								quickMenu.setItemEnabledAt(1, true);
								quickMenu.setItemImageResourceAt(1, R.drawable.play_32);
							} else {
								quickMenu.setItemEnabledAt(0, true);
								quickMenu.setItemImageResourceAt(0, R.drawable.download_32);
								quickMenu.setItemEnabledAt(1, false);
								quickMenu.setItemImageResourceAt(1, R.drawable.play_32_grey);
							}	
						}
					});
			
			//setup the action item click listener
			quickMenu.setOnActionItemClickListener(
					new QuickActionMenu.OnActionItemClickListener<Recording>() {			
				@Override
				public void onItemClick(int pos, Recording recording) {
					if(!Aikuma.isDeviceOnline()) {
		        		Aikuma.showAlertDialog(getApplicationContext(), "Network is disconnected");
		        		return;
		        	}
					
					if (pos == 0) {			//Download Sample
						Log.i(TAG, recording.getCloudIdentifier());
						
						//If preview file doesn't exist, Download the sample(preview)
						String sampleCloudId = new FileModel(recording.getVersionName(), 
								recording.getOwnerId(), recording.getPreviewId(), "preview", "wav").
								getCloudIdentifier(0);
						List<String> cloudId = new ArrayList<String>();
						cloudId.add(sampleCloudId);
						new RequestShareFileTask(cloudId, googleEmailAccount, googleAuthToken).execute();
						
					} else if (pos == 1) {	// Play Sample
						File sampleFile = recording.getPreviewFile();
						if(sampleFile != null) {
							// Play sample
							setUpPlayer(sampleFile);
							mediaPlayer.start();
						}
		
					} else if (pos == 1) { //Add the item to the download list (which will be downloaded onPause)
						Log.i(TAG, recording.getGroupId());
						String itemId = recording.getGroupId();
						itemIdsToDownload.add(itemId);
					}
				}
			});
			
		}

	}
	
	private void setUpPlayer(File recordingFile) {
		mediaPlayer.reset();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(recordingFile.getCanonicalPath());
			mediaPlayer.prepare();
			isMediaPlayerPrepared = true;
		} catch (IOException e) {
			Log.e(TAG, "Failed to prepare MediaPlayer: " + e.getMessage());
		}
	}
	
	/**
     * Inner class to get search results from FusionIndex
     * (queryType: 0: user-search using language-code, 1: item-search when user wants to download)
     * 
     * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
     *
     */
    private class GetSearchResultsTask extends AsyncTask<Void, Void, Boolean>{
    	
    	private static final String TAG = "GetSearchResultsTask";

    	private int queryType;
    	private String mEmailAccount;
    	private String mAccessToken;
    	private List<String> mQuery;
    	
    	Map<String, String> constraints;
    	
    	List<String> cloudIdsToDownload;
		Map<String, String> metadataToWrite;
		List<String> speakerIdsToDownload;

        GetSearchResultsTask(int queryType, List<String> query, String emailAccount, String accessToken) {
        	this.queryType = queryType;
        	this.mEmailAccount = emailAccount;
        	this.mAccessToken = accessToken;
        	this.mQuery = query;
        	
        	constraints = new TreeMap<String, String>();
        	cloudIdsToDownload = new ArrayList<String>();
        	metadataToWrite = new HashMap<String, String>();
    		speakerIdsToDownload = new ArrayList<String>();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
        	if(!Aikuma.isDeviceOnline()) {
        		return false;
        	}
        	
        	Index fi = new FusionIndex(mAccessToken);

        	if(queryType == 0) {
        		constraints.put("languages", mQuery.get(0));
            	constraints.put("file_type", "source");
            	//constraints.put("user_id", mEmailAccount);
            	
            	fi.search(constraints, new Index.SearchResultProcessor() {
    				@Override
    				public boolean process(Map<String, String> result) {
    					String metadataJSONStr = result.get("metadata");
    					Log.i(TAG, metadataJSONStr);
    					JSONParser parser = new JSONParser();
    					try {
    						JSONObject jsonObj = (JSONObject) parser.parse(metadataJSONStr);
    						Recording recording = Recording.read(jsonObj);
    						recordings.add(recording);
    					} catch (ParseException e) {
    						Log.e(TAG, e.getMessage());
    					} catch (IOException e) {
    						Log.e(TAG, e.getMessage());
    					}
    					
    					return true;
    				}
    			});
            	
            	return true;
            	
        	} else if (queryType == 1) {
        		// Search the files belonging to the item_id except for a sample file
        		// The file IDs are stored in 'cloudIdsToDownload','speakerIdsToDownload' respectively
				for(String itemId : mQuery) {
					collectRelatedData(fi, itemId);
				}
				for(String speakerId : speakerIdsToDownload) {
					collectRelatedData(fi, speakerId);
				}
				
				Log.i(TAG, itemIdsToDownload.toString());
				Log.i(TAG, cloudIdsToDownload.toString());
				Log.i(TAG, speakerIdsToDownload.toString());

				mQuery.clear();
				Log.i(TAG, itemIdsToDownload.toString());
				// Write metadata of recordings/speakers to JSON files
				// TODO: This needs to be done by downloading shared METADATA from CloudService
//    			for(String identifier : metadataToWrite.keySet()) {
//    				String metadataJSONStr = metadataToWrite.get(identifier);
//    				writeJSONFile(identifier, metadataJSONStr);
//    			}
    			if(cloudIdsToDownload.size() > 0)
    				return true;
    			else
    				return false;
        	}
        	return false;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
        	if(result) {
        		switch(queryType) {
        		case 0:
        			showRecordingsOnCloud();
        			break;
        		case 1:
        			// Download Files
        			new RequestShareFileTask(cloudIdsToDownload, mEmailAccount, mAccessToken).execute();
        			
        			break;
        		}
        	}
        		
        }
        
        private void collectRelatedData(Index fi, String identifier) {
        	constraints.clear();
			constraints.put("item_id", identifier);
			
			fi.search(constraints, new Index.SearchResultProcessor() {
				@Override
				public boolean process(Map<String, String> result) {
					// Collect identifiers for all files(recording, mapping, ...) / exclude preview
					String fileType = result.get("file_type");
					if(fileType.equals("preview"))
						return true;
		
					String identifier = result.get("identifier");
					cloudIdsToDownload.add(identifier);
					
					// Collect metadata
					if(fileType.equals("speaker") || fileType.equals("source") || 
							fileType.equals("respeaking")) {
//						String metadataJSONStr = result.get("metadata");
//						metadataToWrite.put(identifier, metadataJSONStr);
						FileModel fm = FileModel.fromCloudId(identifier);
						String metadataCloudId = fm.getCloudIdentifier(1);
						cloudIdsToDownload.add(metadataCloudId);
					}
					
					// Collect speaker IDs to find speaker-related files
					if(fileType.equals("source") || fileType.equals("respeaking")) {
						String speakerIdsStr = result.get("speakers");
						String[] speakerIds = speakerIdsStr.split("\\|");
						for(String speakerId : speakerIds) {
							if(speakerId.length() == 0)
								continue;
							speakerIdsToDownload.add(speakerId);
						}
					}
					
					return true;
				}
    		});
        }
        
        private void writeJSONFile(String identifier, String metadataJSONStr) {
			FileModel fileModel = FileModel.fromCloudId(identifier);
			JSONParser parser = new JSONParser();
			try {
				JSONObject jsonObj = (JSONObject) parser.parse(metadataJSONStr);
				
				String relPath = identifier.substring(0, identifier.lastIndexOf('/'));
				File dir = new File(FileIO.getAppRootPath(), relPath);
				dir.mkdirs();
				
				Log.i(TAG, dir.getAbsolutePath() + ", " + fileModel.getMetadataIdExt());
				FileIO.writeJSONObject(new File(dir, fileModel.getMetadataIdExt()), jsonObj);
			} catch (ParseException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
    }
    
    /**
     * Inner class to request the share of a file to central Index-server
     * (fileCloudIds.size == 1: to play a sample, >1: to download item-related files)
     * 
     * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
     *
     */
    private class RequestShareFileTask extends AsyncTask<Void, Void, Boolean> {
    	private static final String TAG = "RequestShareFileTask";

    	private String mEmailAccount;
    	private String mAccessToken;
    	//private String mIdToken;
    	private ArrayList<String> mFileCloudIds;
    	private ArrayList<String> mSharedFileCloudIds;
        
        RequestShareFileTask(List<String> fileCloudIds, String emailAccount, 
        		String accessToken) {
        	this.mEmailAccount = emailAccount;
        	this.mAccessToken = accessToken;
        	//this.mIdToken = idToken;
        	this.mFileCloudIds = new ArrayList<String>(fileCloudIds);
        	this.mSharedFileCloudIds = new ArrayList<String>();
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
        	if(!Aikuma.isDeviceOnline()) {
        		return false;
        	}
        	
            try {
            	URL base = new URL(AikumaSettings.getIndexServerUrl());
            	for(String identifier : mFileCloudIds) {
            		String path = String.format("/file/%s/share/%s", identifier, mEmailAccount);
            		URL url = new URL(base, path);
                    Log.i(TAG, "share url: " + url.toString());
                    
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("PUT");
                    con.setDoOutput(false);
                    con.addRequestProperty("X-Aikuma-Auth-Token", AikumaSettings.getCurrentUserIdToken());
                    switch (con.getResponseCode()) {
                        case 200:
                        	mSharedFileCloudIds.add(identifier);
                            break;
                        case 404:
                        	Log.e(TAG, "404");
                        	break;
                        default:
                        	Log.e(TAG, "" + con.getResponseCode());
                        	break;
                    }
            	}
                
            } catch (MalformedURLException e) {
                Log.e(TAG, "malformed URL: " + e.getMessage());
            } catch (ProtocolException e) {
                Log.e(TAG, "protocol error: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "io exception: " + e.getMessage());
            }
            
            Log.i(TAG, "cnt: " + mSharedFileCloudIds.size());
            if(mSharedFileCloudIds.size() > 0)
            	return true;
            else
            	return false;
        }
        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
            	if(mSharedFileCloudIds.size() == 1) {
            		String identifier = mSharedFileCloudIds.get(0);
            		new DownloadFileTask (identifier, mEmailAccount, mAccessToken).execute();
            	} else {
            		// AutoDonwload all item-related files
            		// This part will be called when user leaves this activity (onPause)
            		// When the device pauses, downloading all shared files
            		// Re-indexing will be done by cloud-service
            		// TODO: make cloud-service share metadata as well so that it can be downloaded
            		Log.i(TAG, "autodownload start");
            		Intent syncIntent = new Intent(CloudSearchActivity.this, GoogleCloudService.class);
            		syncIntent.putExtra(GoogleCloudService.ACTION_KEY, "autoDownload");
            		syncIntent.putExtra(GoogleCloudService.ACCOUNT_KEY, 
            				AikumaSettings.getCurrentUserId());
            		syncIntent.putExtra(GoogleCloudService.TOKEN_KEY, 
            				AikumaSettings.getCurrentUserToken());
            		syncIntent.putStringArrayListExtra("downloadItems", mSharedFileCloudIds);
            		startService(syncIntent);

            	}
            } else {
            	if(mSharedFileCloudIds.size() == 0) {
            		Toast.makeText(CloudSearchActivity.this, 
    						"Download failed", 
    						Toast.LENGTH_LONG).show();
            	}
            	
            }
        }
    }
    
    /**
     * Inner class to download a sample file from google drive
     * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
     *
     */
    private class DownloadFileTask extends AsyncTask<Void, Void, Boolean> {
    	
    	private static final String TAG = "DownloadFileTask";

    	private String mEmailAccount;
    	private String mAccessToken;
    	private String mFileCloudId;
    	private File mFile;

    	DownloadFileTask(String fileCloudId, String emailAccount, String accessToken) {
    		this.mEmailAccount = emailAccount;
    		this.mAccessToken = accessToken;
    		this.mFileCloudId = fileCloudId;
    	}
    	
		@Override
		protected Boolean doInBackground(Void... params) {
			DataStore gd;
			try {
				gd = new GoogleDriveStorage(mAccessToken, 
						AikumaSettings.ROOT_FOLDER_ID, AikumaSettings.CENTRAL_USER_ID);
			} catch (DataStore.StorageException e) {
				Log.e(TAG, "Failed to initialize GoogleDriveStorage");
				return false;
			}
			
			FileModel item = FileModel.fromCloudId(mFileCloudId);
			String relPath = mFileCloudId.substring(0, mFileCloudId.lastIndexOf('/'));
			File dir = new File(FileIO.getAppRootPath(), relPath);
			dir.mkdirs();
			InputStream is = gd.load(mFileCloudId);
			if (is == null) {
				Log.e(TAG, "Failed to get a file from GoogleDriveStorage");
				return false;
			}
			
			try {
				mFile = new File(dir, item.getIdExt());
				FileOutputStream fos = 
						new FileOutputStream(mFile);
				Utils.copyStream(is, fos, true);
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
				return false;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				return false;
			}
			
			return true;
		}
    	
		@Override
		protected void onPostExecute(final Boolean result) {
			if (result) {
				Toast.makeText(CloudSearchActivity.this, 
						"Sample is downloaded", 
						Toast.LENGTH_LONG).show();
				/*
				if(!isMediaPlayerReleased) {
					setUpPlayer(mFile);
					mediaPlayer.start();
				}*/
			} else {
				//Aikuma.showAlertDialog(getApplicationContext(), "Error in downloading a file");
			}
				
		}

    }
}
