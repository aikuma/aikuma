/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma2.R;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.Index;
import org.lp20.aikuma.util.AikumaSettings;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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
	
	private EditText searchQueryView;
	
	private List<Recording> recordings;
	private RecordingArrayAdapter adapter;
	private Parcelable listViewState;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_search);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		recordings = new ArrayList<Recording>();
		adapter = new RecordingArrayAdapter(this, recordings);
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
		listViewState = getListView().onSaveInstanceState();
		MainActivity.locationDetector.stop();
	}
	
	/**
	 * Search the recordings using the query
	 * 
	 * @param searchQueryView	View having the query
	 */
	public void onSearchButton(View view) {
		if(!Aikuma.isDeviceOnline())
			Aikuma.showAlertDialog(this, "Network is disconnected");
		
		recordings.clear();
		String langQuery = searchQueryView.getText().toString();
		String emailAccount = AikumaSettings.getCurrentUserId();
		String accessToken = AikumaSettings.getCurrentUserToken();
		
		new GetSearchResultsTask(langQuery, emailAccount, accessToken).execute();
	}

	private void showRecordingsOnCloud() {
		if(recordings.size() == 0)
			return;
			
		adapter.notifyDataSetChanged();
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
        	constraints.put("file_type", "source");
        	//constraints.put("user_id", mEmailAccount);
        	
        	//recordingsMetadata = 
        	fi.search(constraints, new Index.SearchResultProcessor() {
				@Override
				public boolean process(Map<String, String> result) {
					String metadataJSONStr = result.get("metadata");
					Log.i(TAG, metadataJSONStr);
					JSONParser parser = new JSONParser();
					try {
						JSONObject jsonObj = (JSONObject) parser.parse(metadataJSONStr);
						recordings.add(Recording.read(jsonObj));
					} catch (ParseException e) {
						Log.e(TAG, e.getMessage());
					} catch (IOException e) {
						Log.e(TAG, e.getMessage());
					}
					
					return true;
				}
			});
        	
        	if(recordings.size() > 0)
        		return true;
        	else
        		return false;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
        	if(result)
        		showRecordingsOnCloud();
        }
    }
}
