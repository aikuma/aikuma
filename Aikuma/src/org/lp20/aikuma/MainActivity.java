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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.musicg.wave.Wave;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.GoogleAuth;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.ui.ListenActivity;
import org.lp20.aikuma.ui.MenuBehaviour;
import org.lp20.aikuma.ui.RecordingArrayAdapter;
import org.lp20.aikuma.ui.RecordingMetadataActivity;
import org.lp20.aikuma.ui.sensors.LocationDetector;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.SyncUtil;

// For audio imports
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.lp20.aikuma.R;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.WaveFile;

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
		menuBehaviour = new MenuBehaviour(this);
		SyncUtil.startSyncLoop();
		List<Recording> recordings = Recording.readAll();
		ArrayAdapter adapter = new RecordingArrayAdapter(this, recordings);
		setListAdapter(adapter);
		Aikuma.loadLanguages();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		
		// Load setting values
		SharedPreferences preferences = 
				PreferenceManager.getDefaultSharedPreferences(this);
		
		recordingSet = (HashSet<String>)
				preferences.getStringSet(AikumaSettings.APPROVED_RECORDING_KEY, 
						new HashSet<String>());
		speakerSet = (HashSet<String>)
				preferences.getStringSet(AikumaSettings.APPROVED_SPEAKERS_KEY,
						new HashSet<String>());
		
		emailAccount = preferences.getString("defaultGoogleAccount", null);
		googleAuthToken = preferences.getString("googleAuthToken", null);
		googleAPIScope = getScope();
    	Log.i(TAG, "Account: " + emailAccount + ", scope: " + googleAPIScope);
    	
    	AikumaSettings.googleAuthToken = googleAuthToken;
		AikumaSettings.isBackupEnabled = 
				preferences.getBoolean(AikumaSettings.BACKUP_MODE_KEY, false);
		AikumaSettings.isAutoDownloadEnabled =
				preferences.getBoolean(AikumaSettings.AUTO_DOWNLOAD_MODE_KEY, false);
		
		Log.i(TAG, recordingSet.toString());
	
		if(emailAccount != null) {
			// Validate access token
			// (And if there are items to be archived, upload them)
			new GetTokenTask(emailAccount, googleAPIScope, 
	         		preferences).execute();
		} else if(AikumaSettings.isBackupEnabled 
				|| AikumaSettings.isAutoDownloadEnabled) {
			// When backup was enabled but the user hasn't ever signed-in google account
			getAccountToken();
		}
			
		// Start gathering location data
		MainActivity.locationDetector = new LocationDetector(this);
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
	public void onResume() {
		super.onResume();
		List<Recording> recordings = Recording.readAll();

		// Filter the recordings for originals
		List<Recording> originals = new ArrayList<Recording>();
		for (Recording recording : recordings) {
			if (recording.isOriginal()) {
				originals.add(recording);
			}
		}

		ArrayAdapter adapter = new RecordingArrayAdapter(this, originals);
		setListAdapter(adapter);
		if (listViewState != null) {
			getListView().onRestoreInstanceState(listViewState);
		}

		MainActivity.locationDetector.start();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		MainActivity.locationDetector.stop();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		Recording recording = (Recording) getListAdapter().getItem(position);
		Intent intent = new Intent(this, ListenActivity.class);
		intent.putExtra("id", recording.getId());
		startActivity(intent);
	}

	MenuBehaviour menuBehaviour;
	
	private Set<String> recordingSet;
	private Set<String> speakerSet;

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
        	if(emailAccount == null) {
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
     * Start an activity which allows a user to pick up an account
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
            	SharedPreferences preferences = 
        				PreferenceManager.getDefaultSharedPreferences(this);
            	emailAccount = 
            			data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (isDeviceOnline()) {	
                    new GetTokenTask(emailAccount, googleAPIScope, 
                    		preferences).execute();
                } else {
                    Toast.makeText(this, "Network is disconnected", 
                    		Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You must pick up an account", 
                		Toast.LENGTH_SHORT).show();
            }
            
        } else if ((requestCode == RECOVER_FROM_AUTH_ERROR_REQUEST_CODE ||
                requestCode == RECOVER_FROM_GOOGLEPLAY_ERROR_REQUEST_CODE)
                && resultCode == RESULT_OK) {
            handleReRequestResult(resultCode, data);
            return;
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
        	SharedPreferences preferences = 
    				PreferenceManager.getDefaultSharedPreferences(this);
            new GetTokenTask(emailAccount, googleAPIScope, 
            		preferences).execute();
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
     * Checks whether the device is currently connected to a network
     * @return	boolean for status
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
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
                    		RECOVER_FROM_GOOGLEPLAY_ERROR_REQUEST_CODE);
                }
            }
        });
    }
    
    /**
     * Return an scope for google-API scope
     * @return
     */
    private String getScope() {
    	String joiner = "";
		String scope = "oauth2:";
		for (String s: GoogleDriveStorage.getScopes()) {
			scope += joiner + s;
			joiner = " ";
		}

		for (String s: FusionIndex.getScopes()) {
			scope += joiner + s;
			joiner = " ";
		}
		return scope;
    }
    
    /**
     * Inner class to get an access token from google server
     * 
     * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
     *
     */
    private class GetTokenTask extends AsyncTask<Void, Void, Void>{
    	
    	private static final String TAG = "GetTokenTask";

        private String mScope;
        private String mEmailAccount;
        private SharedPreferences preferences;
        
        GetTokenTask(String email, String scope, 
        		SharedPreferences preferences) {
            this.mEmailAccount = email;
            this.mScope = scope;
            this.preferences = preferences;
        }

        @Override
        protected Void doInBackground(Void... params) {
        	try {
        		googleAuthToken = getToken();
				
				// Store the default account, access-token for next use
				SharedPreferences.Editor prefsEditor = preferences.edit();
				prefsEditor.putString("googleAuthToken", googleAuthToken);
				prefsEditor.putString("defaultGoogleAccount", mEmailAccount);
				prefsEditor.commit();
				AikumaSettings.googleAuthToken = googleAuthToken;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getMessage());
			}
    		Log.i(TAG, "token: " + googleAuthToken);
        	return null;
        }

        @Override
        protected void onPostExecute(Void param) {
        	if(recordingSet.size() > 0 || speakerSet.size() > 0) {
        		// If there are items to be uploaded,
    			// start the GoogleCloud upload service 
        		Intent intent = new Intent(MainActivity.this, 
        				GoogleCloudService.class);
        		intent.putExtra("id", "retry");
				startService(intent);
    		}
        	
        	if(AikumaSettings.isAutoDownloadEnabled) {
        		Intent intent = new Intent(MainActivity.this, 
        				GoogleCloudService.class);
        		intent.putExtra("id", "autoDownload");
        		startService(intent);
        	}
    
        }
        
        /**
         * Request to get an authentication token for google-api services
         * Error(recoverable) -> UI-Thread / (fata) -> Log error
         * 
         * @return	authToken	for google-api services
         * @throws IOException
         */
        private String getToken() throws IOException {
            try {
            	// If the previous token is invalid, refresh token
            	// Otherwise, get new token.
            	if(googleAuthToken != null && 
        				!GoogleAuth.validateAccessToken(googleAuthToken)) {
        			GoogleAuthUtil.clearToken(MainActivity.this, googleAuthToken);
        		}
                return GoogleAuthUtil
                		.getToken(MainActivity.this, mEmailAccount, mScope);
            } catch (UserRecoverableAuthException userRecoverableException) {
                // Error which can be recovered by a user occurs
                // Show the user some UI through the activity.
                handleException(userRecoverableException);
            } catch (GoogleAuthException fatalException) {
                Log.e(TAG, "Unrecoverable error " + fatalException.getMessage());
            } 
            return null;
        }
    }
    
    
    private static final int PICK_ACCOUNT_REQUEST_CODE = 1000;
    private static final int RECOVER_FROM_AUTH_ERROR_REQUEST_CODE = 1001;
    private static final int RECOVER_FROM_GOOGLEPLAY_ERROR_REQUEST_CODE = 1002;
    
    private String emailAccount;
    private String googleAuthToken;
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
	public void audioImport(View _view) {
		mPath = Environment.getExternalStorageDirectory();
		loadFileList(mPath, FILE_TYPE);
		showAudioFilebrowserDialog();
	}

	/**
	 * Loads the list of files in the specified directory into mFileList
	 *
	 * @param	dir	The directory to scan.
	 * @param	fileType	The type of file (other than directories) to look
	 * for.
	 */
	private void loadFileList(File dir, final String fileType) {
		if(dir.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					return filename.contains(fileType) || sel.isDirectory();
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

			builder.setTitle("Import audio file");
			if(mFileList == null) {
				Log.e("importfile", "Showing file picker before loading the file list");
				dialog = builder.create();
				return dialog;
			}
			builder.setItems(mFileList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mChosenFile = mFileList[which];
					Log.i("importfile", "mChosenFile: " + mChosenFile);
					mPath = new File(mPath, mChosenFile);
					if (mPath.isDirectory()) {
						loadFileList(mPath, ".wav");
						showAudioFilebrowserDialog();
					} else {
						//Then it must be a .wav file.

						UUID uuid = UUID.randomUUID();

						// Use musicg WaveHeader to extract information.
						try {
							Wave wave = new Wave(
									new FileInputStream(mPath));
							String format = wave.getWaveHeader().getFormat();
							int sampleRate = wave.getWaveHeader().
									getSampleRate();
							int durationMsec = (int) wave.length() * 1000;
							int bitsPerSample = wave.getWaveHeader().
									getBitsPerSample();
							int numChannels = wave.getWaveHeader().
									getChannels();

							//Copy the file to the no-sync directory.
							try {
								FileUtils.copyFile(mPath,
										new File(Recording.getNoSyncRecordingsPath(),
										uuid.toString() + ".wav"));
							} catch (IOException e) {
								Toast.makeText(getActivity(),
										"Failed to import the recording.",
										Toast.LENGTH_LONG).show();
							}

							// Pass the info along to RecordingMetadataActivity.
							Intent intent = new Intent(getActivity(),
									RecordingMetadataActivity.class);
							intent.putExtra("uuidString", uuid.toString());
							intent.putExtra("sampleRate", (long) sampleRate);
							intent.putExtra("durationMsec", durationMsec);
							intent.putExtra("numChannels", numChannels);
							intent.putExtra("format", format);
							intent.putExtra("bitsPerSample", bitsPerSample);
							startActivity(intent);

						} catch (FileNotFoundException e) {
							// This shouldn't be happening.
							throw new RuntimeException(e);
						}

					}
				}
			});
			dialog = builder.show();
			return dialog;
		}
	}

	private String[] mFileList;
	private File mPath;
	private String mChosenFile;
	private static final String FILE_TYPE = ".wav";
	
	private static final String TAG = "MainActivity";
	/**
	 *  Location-service variable which can be accessed  by all other activities
	 */
	public static LocationDetector locationDetector;

}
