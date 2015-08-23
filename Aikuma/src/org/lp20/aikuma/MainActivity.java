/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveFileManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Calendar;
import java.util.List;

import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.model.FileModel;
import org.lp20.aikuma.model.Language;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.RecordingCSVFile;
import org.lp20.aikuma.model.Recording.TagType;
import org.lp20.aikuma.model.RecordingCSVFile.MetadataChunk;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.model.SpeakerCSVFile;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.storage.google.GoogleAuth;
import org.lp20.aikuma.ui.ListenActivity;
import org.lp20.aikuma.ui.MenuBehaviour;
import org.lp20.aikuma.ui.RecordingArrayAdapter;
import org.lp20.aikuma.ui.RecordingMetadataActivity1;
import org.lp20.aikuma.ui.sensors.LocationDetector;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.SyncUtil;
import org.lp20.aikuma.util.UpdateUtils;

// For audio imports
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;
import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.lp20.aikuma2.R;


/**
 * The primary activity that lists existing recordings and allows you to select
 * them for listening and subsequent respeaking.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class MainActivity extends ListActivity {

	// Helps us store how far down the list we are when MainActivity gets
	// stopped.
	private Parcelable listViewState;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		menuBehaviour = new MenuBehaviour(this);
		originals = new ArrayList<Recording>();
		adapter = new RecordingArrayAdapter(this, originals);
		setListAdapter(adapter);
		
		//SyncUtil.startSyncLoop(this);
		
		Aikuma.loadLanguages();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		
		// Load setting values
		SharedPreferences settings = 
				PreferenceManager.getDefaultSharedPreferences(this);
//		settings.edit().clear().commit();
		
		emailAccount = settings.getString(AikumaSettings.SETTING_OWNER_ID_KEY, null);
		googleAuthToken = settings.getString(AikumaSettings.SETTING_AUTH_TOKEN_KEY, null);
                googleIdToken = settings.getString(AikumaSettings.SETTING_ID_TOKEN_KEY, null);
		googleAPIScope = AikumaSettings.getScope();
		AikumaSettings.isPublicShareEnabled = 
    			settings.getBoolean(AikumaSettings.PUBLIC_SHARE_CONSENT_KEY, false);
		Log.i(TAG, "Account: " + emailAccount + ", scope: " + googleAPIScope);
    	
    	AikumaSettings.setUserId(emailAccount);
    	showUserAccount(emailAccount, null);
    	/*
		AikumaSettings.isBackupEnabled = 
				settings.getBoolean(AikumaSettings.BACKUP_MODE_KEY, true);
		AikumaSettings.isAutoDownloadEnabled =
				settings.getBoolean(AikumaSettings.AUTO_DOWNLOAD_MODE_KEY, true);
		*/
		
		// Automatic validation
		if(emailAccount != null) {
			// Validate access token
			// (And if there are items to be archived, upload them)
			if (Aikuma.isDeviceOnline()) {	
                new GetTokenTask(emailAccount, googleAPIScope, 
                		settings, false).execute();
            } else {
                Toast.makeText(this, "Network is disconnected", 
                		Toast.LENGTH_SHORT).show();
            }

		} else if(AikumaSettings.isBackupEnabled 
				|| AikumaSettings.isAutoDownloadEnabled) {
			// When backup was enabled but the user hasn't ever signed-in google account
			getAccountToken();
		}
			
		// Start gathering location data
		MainActivity.locationDetector = new LocationDetector(this);
		
		// Create a broadcastReceiver to receive sync-data
		syncReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String status = intent.getStringExtra(GoogleCloudService.SYNC_STATUS);
				
				if(status.equals("start")) {
					showProgressStatus(View.VISIBLE);
				} else if(status.equals("end")) {
					Toast.makeText(MainActivity.this, "sync finished", Toast.LENGTH_LONG).show();
					showProgressStatus(View.GONE);
				} else if(status.endsWith("source")) {	//source-download
					String[] splitName = status.split("-");
					String verName = splitName[0];
					String ownerId = splitName[2];
					String recordingId = status.substring(4);
					
					try {
						updateRecordingView(Recording.read(verName, ownerId, recordingId));
					} catch (IOException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			}
		};
		
		checkDate();
		// Create an index file when app starts
		try {
			Recording.indexAll();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}		
		
		//TODO: Update existing files
		/*
		String appVersionName = 
				settings.getString(AikumaSettings.SETTING_VERSION_KEY, "");
		if(!appVersionName.equals(AikumaSettings.getLatestVersion())) {
			// Update the file structure and metadata
			new UpdateUtils(this).update();
		} */
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
	public void onPause() {
		super.onPause();
		listViewState = getListView().onSaveInstanceState();
		MainActivity.locationDetector.stop();
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		speakerId = intent.getStringExtra("speakerId");
	}
	
	@Override
	public void onBackPressed() {
		Log.i(TAG, "backbutton");
		if(speakerId != null) {
			adapter.getFilter().filter(null);
			speakerId = null;
		} else {
			this.finish();
		}
	}

	@Override
	public void onResume() {
		super.onResume();	

		updateRecordingList();
		
		if (listViewState != null) {
			getListView().onRestoreInstanceState(listViewState);
		}

		MainActivity.locationDetector.start();
	}
	
	@Override
	protected void onStart() {
	    super.onStart();
	    LocalBroadcastManager.getInstance(this).registerReceiver(
	    		syncReceiver, new IntentFilter(GoogleCloudService.SYNC_RESULT));
	    showProgressStatus(View.GONE);  
	}

	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(syncReceiver);
	    super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		MainActivity.locationDetector.stop();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		Recording recording = (Recording) getListAdapter().getItem(position);
		if(emailAccount == null) {
			Aikuma.showAlertDialog(this, 
					"Please sign in to your Google account using the settings menu");
			
			return;
		}
		
		Intent intent = new Intent(this, ListenActivity.class);
		intent.putExtra("id", recording.getId());
		intent.putExtra("ownerId", recording.getOwnerId());
		intent.putExtra("versionName", recording.getVersionName());

		startActivity(intent);
	}
	
	private void updateRecordingList() {
		List<Recording> recordings = Recording.readAll();
		Log.i(TAG, "num: " +recordings.size());
		
		// Filter the recordings for originals
		originals = new ArrayList<Recording>();
		for (Recording recording : recordings) {
			if (recording.isOriginal()) {
				originals.add(recording);
			}
		}
		
		Log.i(TAG, "original num: " + originals.size() + ", " + adapter.getCount());
		//adapter.clear();
		//adapter.addAll(originals);
		//adapter.notifyDataSetChanged();
		Log.i(TAG, "original num: " + originals.size() + ", " + adapter.getCount());
		
		if(speakerId != null) {
			adapter.getFilter().filter(speakerId);
		} else {
			adapter.setRecordings(originals);
			//adapter.notifyDataSetChanged();
		}
		adapter.notifyDataSetChanged();
	}
	
	// If current year < 2000, make the user type in the correct date continuously
	private void checkDate() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		Log.i(TAG, "year: " + year);
		if(year < 2000) {
			new AlertDialog.Builder(this)
			.setTitle("Set the current date correctly")
			.setPositiveButton(android.R.string.yes, 
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(
							android.provider.Settings.ACTION_DATE_SETTINGS);
					startActivityForResult(intent, PICK_DATE_REQUEST_CODE);
				}
			}).show();
		}
		
	}
	
	/**
     * Display the progress dialog to the user
     * 
     * @param message	String to display
     */
    public void showProgressDialog(String message) {
        progressDialog =
            ProgressDialog.show(this, "Update", message);
    }
	
    /**
     * Dismiss the progress dialog
     */
    public void dismissProgressDialog() {
    	progressDialog.dismiss();
    }
    
    /**
     * Show the status of cloud-background thread
     * @param visibility	Visibility of the progress bar View
     */
    public void showProgressStatus(int visibility) {
    	ProgressBar pStatus = (ProgressBar) findViewById(R.id.cloudProgress);
    	pStatus.setVisibility(visibility);
    }
    
    /**
     * Show the current user's ID
     * @param userId	The user's ID
     * @param token		The user-account's auth_token
     */
    public void showUserAccount(String userId, String token) {
    	TextView userIdView = (TextView) findViewById(R.id.userIdView);
    	if(userId != null) {
    		if(token != null)
    			userIdView.setTextColor(Color.BLACK);
    		else
    			userIdView.setTextColor(Color.GRAY);
        	userIdView.setText(userId);
    	} else {
    		userIdView.setText("");
    	}
    		
    }

    /**
     * Update the list of recordings view
     * @param recording		New recording to be updated
     */
    public void updateRecordingView(Recording recording) {
    	if(!originals.contains(recording)) {
    		originals.add(recording);
    		adapter.notifyDataSetChanged();
    	}
    }
    
	/**
	 * Setup the search-menu-item interface (called by MenuBehavior)
	 * @param menu	menu object
	 */
	public void setUpSearchInterface(Menu menu) {
		
		final MenuItem searchMenuItem = menu.findItem(R.id.search);
		searchView = (SearchView) searchMenuItem.getActionView();
		// Touch event outside the searchview closes the searchview
		searchView.setOnQueryTextFocusChangeListener(
				new View.OnFocusChangeListener() {	
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!hasFocus) {
	                searchMenuItem.collapseActionView();
	                //searchView.setQuery("", false);
	            }
			}
		});
		
		// Execute search
		searchView.setOnQueryTextListener(
				new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				// TODO Auto-generated method stub
				adapter.getFilter().filter(query);
				searchView.clearFocus();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				adapter.getFilter().filter(newText);
				return true;
			}
		});	
	}

	BroadcastReceiver syncReceiver;
	private List<Recording> originals;
	
	SearchView searchView;
	private String speakerId;
	
	MenuBehaviour menuBehaviour;

	private RecordingArrayAdapter adapter;
	private ProgressDialog progressDialog;
	
	private static final int PICK_DATE_REQUEST_CODE = 0;
	
	/////////////////////////////////////////////////////
	////                                   			/////
	//// Things pertaining to getting Google token. /////
	////                                   			/////
	/////////////////////////////////////////////////////
		
	/**
	 * Get the google-service API token and set it to googleAuthToken variable
	 */
    public void getAccountToken() {
        int statusCode = GooglePlayServicesUtil
        		.isGooglePlayServicesAvailable(this);
        if (statusCode == ConnectionResult.SUCCESS) {
        	Log.i(TAG, "getAccountToken");

        	//TODO: Sign-out, Sign-in with other accounts
        	if(AikumaSettings.getCurrentUserId() == null) {
        		pickUserAccount();
        	}
        } else if (GooglePlayServicesUtil.isUserRecoverableError(statusCode)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                    statusCode, this, 0 /* request code not used */);
            dialog.show();
        } else {
            Toast.makeText(this, "Unrecoverable Google-Play Services error", 
            		Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Clear the account and token
     */
    public void clearAccountToken() {
        emailAccount = null;
        googleAuthToken = null;
        googleIdToken = null;
        AikumaSettings.setUserId(null);
        AikumaSettings.setUserToken(null);
        AikumaSettings.setUserIdToken(null);
        showUserAccount(emailAccount, null);
        
        menuBehaviour.setSignInState(false);
    }
	
    /**
     * Start an activity which allows a user to pick an account
     */
    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, " (Default Google-account)", 
                null, null, null);
        startActivityForResult(intent, PICK_ACCOUNT_REQUEST_CODE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, 
    		int resultCode, Intent data) {
        if (requestCode == PICK_ACCOUNT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
            	SharedPreferences settings = 
        				PreferenceManager.getDefaultSharedPreferences(this);
            	emailAccount = 
            			data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            	// Stores the account for next-use
            	AikumaSettings.setUserId(emailAccount);
            	showUserAccount(emailAccount, null);
            	menuBehaviour.setSignInState(true);
                settings.edit().putString(
                		AikumaSettings.SETTING_OWNER_ID_KEY, emailAccount).commit();
            	
            	if (Aikuma.isDeviceOnline()) {	
                    new GetTokenTask(emailAccount, googleAPIScope, 
                    		settings, false).execute();
                } else {
                    Toast.makeText(this, "Network is disconnected", 
                    		Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You must pick an account", 
                		Toast.LENGTH_SHORT).show();
            }
            
        } else if ((requestCode == RECOVER_FROM_AUTH_ERROR_REQUEST_CODE ||
                requestCode == RECOVER_FROM_GOOGLEPLAY_ERROR_REQUEST_CODE)
                && resultCode == RESULT_OK) {
            handleReRequestResult(resultCode, data);
            return;
        } else if(requestCode == PICK_DATE_REQUEST_CODE) {
        	checkDate();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void handleReRequestResult(int resultCode, Intent data) {
        if (data == null) {
        	Toast.makeText(this, "Unknown error, Retry it", 
            		Toast.LENGTH_SHORT).show();
            return;
        }
        if (resultCode == RESULT_OK) {
            // User recovered error, retry to get access_token
        	if (Aikuma.isDeviceOnline()) {	
        		SharedPreferences settings = 
        				PreferenceManager.getDefaultSharedPreferences(this);
                new GetTokenTask(emailAccount, googleAPIScope, 
                		settings, false).execute();
            } else {
                Toast.makeText(this, "Network is disconnected", 
                		Toast.LENGTH_SHORT).show();
            }

            return;
        }
//        if (resultCode == RESULT_CANCELED) {
//            show("User rejected authorization.");
//            return;
//        }
        Toast.makeText(this, "Unknown error, Retry it", 
        		Toast.LENGTH_SHORT).show();
    }
	
    /**
     * A hook for background UI thread which allows the user to fix errors
     * @param e	Error-kind
     */
    private void handleException(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is not available.
                    // Show a dialog that allows the user to update the APK
                    int statusCode = 
                    		((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    GooglePlayServicesUtil.getErrorDialog(statusCode, 
                    		MainActivity.this, 
                    		RECOVER_FROM_GOOGLEPLAY_ERROR_REQUEST_CODE).show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // When authenticate failed
                	// (User has not yet accessed to the account)
                    // Show an activity for user's re-login Google Play services.
                    Intent intent = 
                    		((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                    		RECOVER_FROM_AUTH_ERROR_REQUEST_CODE);
                }
            }
        });
    }
    
    /**
     * Inner class to get/validate an access token from google server
     * 
     * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
     *
     */
    private class GetTokenTask extends AsyncTask<Void, Void, Boolean>{
    	
    	private static final String TAG = "GetTokenTask";

        private String mScope;
        private String mEmailAccount;
        private SharedPreferences preferences;
        private boolean forceSync;
        
        GetTokenTask(String email, String scope, 
        		SharedPreferences preferences, boolean forceSync) {
            this.mEmailAccount = email;
            this.mScope = scope;
            this.preferences = preferences;
            this.forceSync = forceSync;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
		try {
			getToken();
			
			// Store the access-token for next use
			preferences.edit()
				.putString(AikumaSettings.SETTING_AUTH_TOKEN_KEY, googleAuthToken)
				.putString(AikumaSettings.SETTING_ID_TOKEN_KEY, googleIdToken)
				.commit();
			AikumaSettings.setUserToken(googleAuthToken);
			AikumaSettings.setUserIdToken(googleIdToken);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
			return false;
		}
		Log.i(TAG, "access token: " + googleAuthToken);
		Log.i(TAG, "identity token: " + googleIdToken);
		return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
        	showUserAccount(emailAccount, AikumaSettings.getCurrentUserToken());
        	
        	if(!result)
        		return;

        	if(AikumaSettings.isBackupEnabled 
					|| AikumaSettings.isAutoDownloadEnabled) {
				Aikuma.syncRefresh(MainActivity.this, true);
			}
        }
        
        /**
         * Request to get an authentication token for google-api services
         * Error(recoverable) -> UI-Thread / (fata) -> Log error
         * 
         * @return	authToken	for google-api services
         * @throws IOException
         */
        private void getToken() throws IOException {
            try {
                googleAuthToken = GoogleAuthUtil.getToken(MainActivity.this, mEmailAccount, mScope);
                googleIdToken = GoogleAuthUtil.getToken(MainActivity.this, mEmailAccount, AikumaSettings.getIdTokenScope());
            } catch (UserRecoverableAuthException userRecoverableException) {
                // Error which can be recovered by a user occurs
                // Show the user some UI through the activity.
                handleException(userRecoverableException);
            } catch (GoogleAuthException fatalException) {
                Log.e(TAG, "Unrecoverable error " + fatalException.getMessage());
            } 
        }
    }
    
    
    private static final int PICK_ACCOUNT_REQUEST_CODE = 1000;
    private static final int RECOVER_FROM_AUTH_ERROR_REQUEST_CODE = 1001;
    private static final int RECOVER_FROM_GOOGLEPLAY_ERROR_REQUEST_CODE = 1002;
    
    private String emailAccount;
    private String googleAuthToken;
    private String googleIdToken;
    private String googleAPIScope;
    
    
	////////////////////////////////////////////
	////                                   /////
	//// Things pertaining to AudioImport. /////
	////                                   /////
	////////////////////////////////////////////

	/**
	 * Called when the import button is pressed; starts the import process.
	 *
	 * @param	_view	the audio import button.
	 */
    public void fileImport(View _view) {
    	mPath = Environment.getExternalStorageDirectory();
		
    	final CharSequence[] fileTypes = {"Speaker", "Recording"};
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Select the filetype to import");
    	builder.setSingleChoiceItems(fileTypes, -1, new DialogInterface.OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
				case 0:
					mFileType = FileModel.FileType.SPEAKER;
					listFiles();
					break;
				case 1:
					mFileType = FileModel.FileType.SOURCE;
					listFiles();
					break;
				}
				mDialog.dismiss();
			}
		});
    	mDialog = builder.show();
    }
    
    /**
     * Called when the import filetype is selected
     */
	private void listFiles() {
		switch(mFileType) {
		case SPEAKER:
			loadFileList(mPath, FILE_EXT2);
			break;
		case SOURCE:
			loadFileList(mPath, FILE_EXT, FILE_EXT2);
			break;
		}
		showAudioFilebrowserDialog();
	}

	/**
	 * Loads the list of files in the specified directory into mFileList
	 *
	 * @param	dir	The directory to scan.
	 * @param	fileTypes	The types of file (other than directories) to look
	 * for.
	 */
	private void loadFileList(File dir, final String... fileTypes) {
		if(dir.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					boolean isType = sel.isDirectory();
					for(String fileType : fileTypes)
						isType  |= filename.contains(fileType);
					return isType;
				}
			};
			mFileList = mPath.list(filter);
		}
		else {
			mFileList= new String[0];
		}
	}

	/**
	 * Presents the dialog for choosing audio files to the user.
	 */
	private void showAudioFilebrowserDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		FilebrowserDialogFragment fbdf = new FilebrowserDialogFragment();
		fbdf.show(ft, "dialog");
	}

	/**
	 * Used to display audio files that the user can choose to load from.
	 */
	public class FilebrowserDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog dialog = null;
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			if(mFileList == null) {
				Log.e("importfile", "Showing file picker before loading the file list");
				dialog = builder.create();
				return dialog;
			}
			if (mFileType == FileModel.FileType.SPEAKER)
				builder.setTitle("Import speaker file");
			else if (mFileType == FileModel.FileType.SOURCE)
				builder.setTitle("Import source file");
			builder.setItems(mFileList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mChosenFile = mFileList[which];
					Log.i("importfile", "mChosenFile: " + mChosenFile);
					File chosenFile = new File(mPath, mChosenFile);
					if (chosenFile.isDirectory()) {
						mPath = chosenFile;
						MainActivity.this.listFiles();
					} else {
						MainActivity.this.startImporting(chosenFile);
					}
					
				}
			});
			dialog = builder.show();
			return dialog;
		}
	}
	
	private void startImporting(File chosenFile) {
		if (mFileType == FileModel.FileType.SOURCE) {
			if (chosenFile.getName().endsWith(FILE_EXT2)) { //csv
				try {
					RecordingCSVFile csvReader = new RecordingCSVFile(mPath, mChosenFile);
					for(RecordingCSVFile.MetadataChunk metadata : csvReader.getMetadataChunks()) {
						//Then it must be a .wav file.
						UUID uuid = UUID.randomUUID();
						UUID imageUUID = null;
						
						// Use WaveHeader to extract info
						File recordingFile = new File(mPath, metadata.getFileName());
						Wave wave = new Wave(new FileInputStream(recordingFile));
						String format = "vnd.wave";//wave.getWaveHeader().getFormat();
						int sampleRate = wave.getWaveHeader().
								getSampleRate();
						int durationMsec = (int) wave.length() * 1000;
						int bitsPerSample = wave.getWaveHeader().
								getBitsPerSample();
						int numChannels = wave.getWaveHeader().
								getChannels();
						Log.i(TAG, metadata.getFileName() + ": " + format + ", " + sampleRate + ", " + durationMsec + ", " + bitsPerSample + ", " + numChannels);
						
						//Copy the file to the no-sync directory.
						try {
							FileUtils.copyFile(recordingFile,
									new File(Recording.getNoSyncRecordingsPath(),
									uuid.toString() + "." + FileModel.AUDIO_EXT));
						} catch (IOException e) {
							Toast.makeText(this,
									"Failed to import the recording.",
									Toast.LENGTH_LONG).show();
						}
						
						// Create preview file
						wave.getWaveHeader().setChunkSize(recordingFile.length() - 8);
						double trimmedDurationSec = durationMsec/1000 < Recording.SAMPLE_SEC? 
								0 : (durationMsec/1000 - Recording.SAMPLE_SEC);
						Log.i(TAG, trimmedDurationSec * sampleRate * (bitsPerSample / 8) * numChannels + " / " + wave.getWaveHeader().getSubChunk2Size());
						wave.rightTrim(trimmedDurationSec);
						WaveFileManager waveFileManager = new WaveFileManager(wave);
						waveFileManager.saveWaveAsFile(Recording.getNoSyncRecordingsPath() + 
								"/" + uuid.toString() + FileModel.SAMPLE_SUFFIX + "." + FileModel.AUDIO_EXT);
						
						
						//Optional image
						if(metadata.getImageName() != null) {
							File imageFile = new File(mPath, metadata.getImageName());
							imageUUID = UUID.randomUUID();
							try {
								FileUtils.copyFile(imageFile,
										new File(Recording.getNoSyncRecordingsPath(),
										imageUUID.toString() + "." + FileModel.IMAGE_EXT));
							} catch (IOException e) {
								Toast.makeText(this,
										"Failed to import the image.",
										Toast.LENGTH_LONG).show();
							}
						}
						
						//Write the recording
						Recording recording = new Recording(
								uuid, imageUUID, metadata.getTitle(), metadata.getComments(), metadata.getDate(), 
								AikumaSettings.getLatestVersion(), AikumaSettings.getCurrentUserId(),
								metadata.getLanguages(),
								Aikuma.getDeviceName(), Aikuma.getAndroidID(), null, null,
								sampleRate, durationMsec, format, numChannels, 
								bitsPerSample, null, null);
						
						//Write the tags(speakers, languages)
						recording.write();
						
						for(Language lang : metadata.getLanguages()) {
							recording.tag(TagType.LANGUAGE, lang.toTagString(), 
									AikumaSettings.getCurrentUserId());
						}
						
						List<Speaker> deviceSpeakers = Speaker.readAll();
						for(Speaker speaker : metadata.getSpeakers()) {
							if(deviceSpeakers.contains(speaker)) {
								recording.tag(TagType.SPEAKER, 
										speaker.getId(), AikumaSettings.getCurrentUserId());
							}	
						}
						
						//Backup? or put it to MetadataActivity?
						if(AikumaSettings.isBackupEnabled) {
							Aikuma.syncRefresh(MainActivity.this, true);
						}
						
					}
					
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
				updateRecordingList();		
						
			} else {
				//Then it must be a .wav file.
				UUID uuid = UUID.randomUUID();
				UUID imageUUID = null;

				// Use musicg WaveHeader to extract information.
				try {
					Wave wave = new Wave(
							new FileInputStream(chosenFile));
	
					String format = "vnd.wave";//wave.getWaveHeader().getFormat();
					int sampleRate = wave.getWaveHeader().
							getSampleRate();
					int durationMsec = (int) wave.length() * 1000;
					int bitsPerSample = wave.getWaveHeader().
							getBitsPerSample();
					int numChannels = wave.getWaveHeader().
							getChannels();
					
					//Copy the file to the no-sync directory.
					try {
						FileUtils.copyFile(chosenFile,
								new File(Recording.getNoSyncRecordingsPath(),
								uuid.toString() + "." + FileModel.AUDIO_EXT));
					} catch (IOException e) {
						Toast.makeText(this,
								"Failed to import the recording.",
								Toast.LENGTH_LONG).show();
					}

					// Pass the info along to RecordingMetadataActivity.
					Intent intent = new Intent(this,
							RecordingMetadataActivity1.class);
					intent.putExtra("uuidString", uuid.toString());
					intent.putExtra("sampleRate", (long) sampleRate);
					intent.putExtra("durationMsec", durationMsec);
					intent.putExtra("numChannels", numChannels);
					intent.putExtra("format", format);
					intent.putExtra("bitsPerSample", bitsPerSample);
					intent.putParcelableArrayListExtra("languages", new ArrayList<Language>());
					startActivity(intent);

				} catch (FileNotFoundException e) {
					// This shouldn't be happening.
					throw new RuntimeException(e);
				}

			}
		} else if (mFileType == FileModel.FileType.SPEAKER) {
			try {
				SpeakerCSVFile csvReader = new SpeakerCSVFile(mPath, mChosenFile);
				List<Speaker> deviceSpeakers = Speaker.readAll();
				for(SpeakerCSVFile.MetadataChunk metadata : csvReader.getMetadataChunks()) {
					Speaker speaker = metadata.getSpeaker();
					if(!deviceSpeakers.contains(speaker)) {
						speaker.write();
					}	
				}
				
				//Backup? or put it to MetadataActivity?
				if(AikumaSettings.isBackupEnabled) {
					Aikuma.syncRefresh(MainActivity.this, true);
				}
				
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		
	}
	

	private AlertDialog mDialog;
	private String[] mFileList;
	private File mPath;
	private FileModel.FileType mFileType;
	private String mChosenFile;
	private static final String FILE_EXT = ".wav";
	private static final String FILE_EXT2 = ".csv";
	
	private static final String TAG = "MainActivity";
	/**
	 *  Location-service variable which can be accessed  by all other activities
	 */
	public static LocationDetector locationDetector;

}
