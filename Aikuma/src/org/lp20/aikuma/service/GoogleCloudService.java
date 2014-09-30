/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.storage.Data;
import org.lp20.aikuma.storage.DataStore;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.Utils;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The service that deals with Google-Cloud API 
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class GoogleCloudService extends IntentService{
	
	private final static String TAG = "GoogleCloudService";
	
	private final String key = AikumaSettings.ARCHIVE_RECORDING_KEY;
	
	private SharedPreferences preferences;
	private Editor prefsEditor;
	private Set<String> recordingSet;
	
	private String googleAuthToken;

	/**
	 * Constructor for IntentService subclasses
	 */
	public GoogleCloudService() {
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		preferences = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		recordingSet = (HashSet<String>)
				preferences.getStringSet(key, new HashSet<String>());
		
		prefsEditor = preferences.edit();
		
		googleAuthToken = AikumaSettings.googleAuthToken;
		
		Log.i(TAG, "Cloud-service created:" + recordingSet.toString());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		String id = (String)
				intent.getExtras().get("id");
		Log.i(TAG, "Receive intent: " + id);
		
		if(id.equals("backup")) {		// Called when backup-setting is enabled
			backUp();
		} else if(id.equals("retry")) {	// Called with the start of application
			retry();
		} else if(id.equals("autoDownload")) {
			autoDownloadFiles();
		} else {						// Called when archive button is pressed
			archive(id);
		}
	}
	
	private void autoDownloadFiles() {
		if(googleAuthToken == null)
			return;
		
		FusionIndex fi = new FusionIndex(googleAuthToken);
		GoogleDriveStorage gd = new GoogleDriveStorage(googleAuthToken);
		
		String emailAddr = preferences.getString("defaultGoogleAccount", null);
		Map<String,String> criteria = new HashMap<String, String>();
		
		if(emailAddr != null)
			criteria.put("user_id", emailAddr);
        
		// Search the items not existing in this device
        List<String> archivedRecordingIds = fi.search(criteria);
        List<Recording> recordings = Recording.readAll();
        List<String> recordingIds = new ArrayList<String>();
        for(Recording item : recordings) {
        	String extension = (item.isMovie())? ".mp4" : ".wav";
        	String identifier = item.getId() + extension;
        	recordingIds.add(identifier);
        }
        
        archivedRecordingIds.removeAll(recordingIds);
        
        // Download the filtered items
        for(String itemId: archivedRecordingIds) {
        	Map<String, String> meta = fi.getItemMetadata(itemId);
        	String id = itemId.substring(0, itemId.length()-4);
        	String groupId = meta.get("item_id");
        	String metadataJSONStr = meta.get("metadata");
        	File dir = new File(Recording.getRecordingsPath(), groupId);
        	dir.mkdir();
        	
        	try{
        		// Write the recording file
            	InputStream is = gd.load(itemId);
            	FileOutputStream fos = new FileOutputStream(new File(dir, itemId));
            	Utils.copyStream(is, fos, true);
            	
            	// Write the recording metadata
            	FileIO.write(new File(dir, id + "-metadata.json"), metadataJSONStr);
        	} catch (FileNotFoundException e) {
        		Log.e(TAG, e.getMessage());
        	} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
        	
        }   
	}
	
	
	
	/**
	 *  Back-up function: add all recording-items to the Set 
	 *  and call retry() to upload the items
	 */
	private void backUp() {
		List<Recording> recordings = Recording.readAll();
		String requestDate = new SimpleDateFormat().
				format(new Date()).toString();
		String recordingArchiveState = (requestDate + "|" + "0");
		
		for(Recording recording : recordings) {
			if(!recording.isArchived()) {
				recordingSet.add(recording.getId());
				prefsEditor.putString(recording.getId(), 
						recordingArchiveState);
			}
		}
		prefsEditor.putStringSet(key, recordingSet);
		prefsEditor.commit();
		
		retry();
	}
	
	/**
	 *  Retry function: Upload all recording-items in the Set
	 */
	private void retry() {
		Log.i(TAG, "retry start");
		if(googleAuthToken == null)
			return;
		
		Set<String> recordings = new HashSet<String>(recordingSet);
		for(String recordingId : recordings) {
			Recording recording;
			try {
				recording = Recording.read(recordingId);
				if(!recording.isArchived()) {
					// Get the current state of archiving
					String[] requestArchiveState = 
							preferences.getString(recordingId, "").split("\\|");
					String requestDate = requestArchiveState[0];
					int archiveProgress = 
							Integer.parseInt(requestArchiveState[1]);
					
					Log.i(TAG, recordingId + "-state: " + requestArchiveState[0]+"|"+requestArchiveState[1]);
					
					String uri = null;
					if (requestArchiveState.length >= 3 && requestArchiveState[2].length() > 0)
						uri = requestArchiveState[2];
						
					startArchiving(recording, requestDate, uri, archiveProgress);
				} else {
					removeRecordingFromSet(recordingId);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "archive failed: " + recordingId);
			}
		}
	}
	
	/**
	 * Upload the recording-item having id
	 * 
	 * @param id	ID of the recording item
	 */
	private void archive(String id) {
		Recording recording;
		try {
			recording = Recording.read(id);
			if(!recording.isArchived()) {
				// Record archive-approved date
				String requestDate = new SimpleDateFormat().
						format(new Date()).toString();
				recordingSet.add(id);
				prefsEditor.putStringSet(key, recordingSet);
				
				String recordingArchiveState = (requestDate + "|" + "0");
				prefsEditor.putString(id, recordingArchiveState);
				prefsEditor.commit();
				
				
				startArchiving(recording, requestDate, null, 0);
			} else {
				removeRecordingFromSet(id);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "archive failed: " + id);
		}
		
	}
	
	// If the recording-item archiving is finished, remove the item from the Set
	private void removeRecordingFromSet(String recordingId) {
		recordingSet.remove(recordingId);
		prefsEditor.putStringSet(key, recordingSet);
		prefsEditor.remove(recordingId);
		prefsEditor.commit();
	}
	
	/**
	 * Start uploading the recording-item
	 * Archiving-state(0:approved, 1:File-uploaded, 
	 * 2:FusionTabe-index-finished and [recording]-archive.json file is created)
	 * 
	 * @param recording		The recording-item to be archived
	 * @param requestDate	Archive-approval date
	 * @param state			Archiving-state
	 * @throws IOException	Exception during file-processing
	 */
	private void startArchiving(Recording recording, String requestDate, String uri, int state) throws IOException  {
		File file = recording.getFile();
		String id = recording.getId();
		String identifier = file.getName();
		
		switch(state) {
		case 0:
			uri = uploadFile(file);
			if(uri == null) return;
			String recordingArchiveState = (requestDate + "|1|" + uri);
			prefsEditor.putString(id, recordingArchiveState);
			prefsEditor.commit();
		case 1:
			Date uploadDate = 
				uploadMetadata(recording, identifier, requestDate, uri);
			if(uploadDate != null) {
				recording.archive(uploadDate.toString(), uri);
				
				removeRecordingFromSet(id);
			}
		}
	}
	
	//upload the file to Google Drive
	private String uploadFile(File recordingFile) throws IOException {
		Data data = Data.fromFile(recordingFile);
		if (data == null) {
			Log.e(TAG, "Source file doesn't exist");
			return null;		
		}
		
		DataStore gd = new GoogleDriveStorage(googleAuthToken);
		String uri = gd.store(recordingFile.getName(), data);
		if(uri != null)
			Log.i(TAG, "File-upload success");
		else
			Log.i(TAG, "File-Upload failed");
		return uri;
	}
	
	// Upload the metadata of the recording to FusionTable
	private Date uploadMetadata(Recording recording, String identifier, String requestDate, String uri) throws IOException {
		File metadataFile = recording.getMetadataFile();
		String jsonstr;
		try {
			jsonstr = FileIO.read(metadataFile);
		} catch (IOException e) {
			Log.e(TAG, "Failed to read metadata file: " + metadataFile.getPath());
			return null;
		}
		
		FusionIndex fi = new FusionIndex(googleAuthToken);
		JSONObject jsonfile = (JSONObject) JSONValue.parse(jsonstr);

		Map<String, String> metadata = new HashMap<String,String>();
		JSONArray speakers_arr = (JSONArray) jsonfile.get("people");
		String speakers = "";
		String joiner = "";
		for (Object obj: speakers_arr) {
			speakers += joiner + (String) obj;
			joiner = ",";
		}
		String languages = "";
		joiner = "";
		for (Object obj: (JSONArray) jsonfile.get("languages")) {
			String lang = (String) ((JSONObject) obj).get("code");
			languages += joiner + lang;
			joiner = ",";
		}
		//SharedPreferences preferences = 
		//		PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String emailAddr = preferences.getString("defaultGoogleAccount", null);
		if (emailAddr == null) {
			Log.i(TAG, "defaultGoogleAccount is null");
			return null;
		}
		metadata.put("user_id", emailAddr);
		metadata.put("metadata", jsonstr);
		metadata.put("data_store_uri", uri);
		metadata.put("item_id", (String) jsonfile.get("recording"));
		metadata.put("file_type", (String) jsonfile.get("type"));
		metadata.put("speakers", speakers);
		metadata.put("languages", languages);
		metadata.put("date_approved", requestDate);
		Date uploadDate = new Date();
		metadata.put("date_backedup", 
				new SimpleDateFormat().format(uploadDate).toString());
		
		DataStore ds = new GoogleDriveStorage(googleAuthToken);
		if (ds.share(identifier) && fi.index(identifier, metadata)) {
			Log.i(TAG, "Metadata-upload success");
			return uploadDate;
		}
		else {
			Log.i(TAG, "Metadata-upload failed");
			return null;
		}
	}

}
