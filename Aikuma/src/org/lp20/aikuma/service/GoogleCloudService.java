package org.lp20.aikuma.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.storage.Data;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.util.AikumaSettings;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class GoogleCloudService extends IntentService{
	
	private final static String TAG = "GoogleCloudService";
	
	private final String key = "approvedRecordings";
	
	private SharedPreferences preferences;
	private Editor prefsEditor;
	private HashSet<String> recordingList;
	
	private String googleAuthToken;
	
	public GoogleCloudService() {
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		preferences = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		recordingList = (HashSet<String>)
				preferences.getStringSet(key, new HashSet<String>());
		
		prefsEditor = preferences.edit();
		
		googleAuthToken = AikumaSettings.googleAuthToken;
		
		Log.i(TAG, recordingList.toString());
		Log.i(TAG, "GoogleCloudService created");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		String id = (String)
				intent.getExtras().get("id");
		if(id.equals("backup")) {
			
		} else if(id.equals("retry")) {
			for(String recordingId : recordingList) {

				try {
					Recording recording = Recording.read(recordingId);
					String requestDate = preferences.getString(recordingId, "");
					Log.i(TAG, recordingId + ": " + requestDate);
					
					Date uploadDate = upload(recording, requestDate);
					if(uploadDate != null) {
						recordingList.remove(id);
						prefsEditor.putStringSet(key, recordingList);
						prefsEditor.remove(id);
						prefsEditor.commit();

						recording.archive(uploadDate.toString());
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			
			try {
				Recording recording = Recording.read(id);
				
				String requestDate = new Date().toString();
				recordingList.add(id);
				prefsEditor.putStringSet(key, recordingList);
				prefsEditor.putString(id, requestDate);
				prefsEditor.commit();
				
				Date uploadDate = upload(recording, requestDate);
				if(uploadDate != null) {
					recordingList.remove(id);
					prefsEditor.putStringSet(key, recordingList);
					prefsEditor.remove(id);
					prefsEditor.commit();
					
					recording.archive(uploadDate.toString());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	private Date upload(Recording recording, String requestDate) throws IOException {
		File file = recording.getFile();
		File metadataFile = recording.getMetadataFile();
		Data data = Data.fromFile(file);
		
		if (data == null) {
			Log.e(TAG, "Source file doesn't exist");
			return null;		
		}
		
		GoogleDriveStorage gd = new GoogleDriveStorage(googleAuthToken);
		FusionIndex fi = new FusionIndex(googleAuthToken);
		JSONObject jsonfile;
		try {
			jsonfile = (JSONObject) JSONValue.parse(new FileReader(metadataFile));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Metadata file doesn't exist");
			return null;	
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
		metadata.put("language", languages);
		metadata.put("date_approved", requestDate);
		Date date = new Date();
		metadata.put("date_backedup", date.toString());
		
		Log.i(TAG, "meta: " + metadata.toString());
		
		if (gd.store(file.getName(), data) &&
				fi.index(file.getName(), metadata) 
				) {
			Log.i(TAG, "success");
			return date;
		}
		else {
			Log.i(TAG, "Upload failed");
			return null;
		}
	}

}
