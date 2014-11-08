/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.ui;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.util.AikumaSettings;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;

/**
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 * 
 * Activity class dealing with recording item search interface
 */
public class CloudSearchActivity extends AikumaListActivity {
	
	private static final String TAG = "CloudSearchActivity";
	
	private List<String> recordingsMetadata;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final EditText searchQuery = (EditText) findViewById(R.id.searchQuery);
		
		searchQuery.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if((event.getAction() == KeyEvent.ACTION_DOWN && 
						(event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
					onSearchButton(searchQuery);
					return true;
				}
				return false;
			}
			
		});		
			
	}
	
	/**
	 * Search the recordings using the query
	 * 
	 * @param searchQueryView	View having the query
	 */
	public void onSearchButton(EditText searchQueryView) {
		String langQuery = searchQueryView.getText().toString();
		String emailAccount = AikumaSettings.getCurrentUserId();
		String accessToken = AikumaSettings.getCurrentUserToken();
		
		new GetSearchResultsTask(langQuery, emailAccount, accessToken).execute();
	}

	private boolean showRecordingsOnCloud(List<String> recordingsMetadata) {
		if(recordingsMetadata == null)
			return false;
			
		//TODO: fill the list view
		
		return true;
	}
	
	/**
     * Inner class to get an access token from google server
     * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
     *
     */
    private class GetSearchResultsTask extends AsyncTask<Void, Void, Boolean>{
    	
    	private static final String TAG = "GetSearchResultsTask";

    	private String mEmailAccount;
    	private String mAccessToken;
    	private String mQuery;

        GetSearchResultsTask(String query, String emailAccount, String accessToken) {
        	this.mEmailAccount = emailAccount;
        	this.mAccessToken = accessToken;
        	this.mQuery = query;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
        	FusionIndex fi = new FusionIndex(mAccessToken);
        	Map<String, String> constraints = new TreeMap<String, String>();
        	constraints.put("languages", mQuery);
        	//constraints.put("user_id", mEmailAccount);
        	
        	recordingsMetadata = fi.search(constraints);
        	
        	if(recordingsMetadata != null)
        		return true;
        	else
        		return false;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
        	if(result)
        		showRecordingsOnCloud(recordingsMetadata);
        }
    }
}
