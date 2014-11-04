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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.storage.Data;
import org.lp20.aikuma.storage.DataStore;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.Utils;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.StandardDateFormat;
import org.lp20.aikuma.util.SyncUtil;

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
	
	/**
	 * Broadcast message signature
	 */
	public final static String SYNC_RESULT = "org.lp20.aikuma";
	/**
	 * Key of data in the broadcast message
	 */
	public final static String SYNC_STATUS = "sync_status";
	
	private final static String TAG = "GoogleCloudService";
	
	private final String approvalKey = AikumaSettings.APPROVED_RECORDING_KEY;
	private final String archiveKey = AikumaSettings.ARCHIVED_RECORDING_KEY;
	private final String approvalSpKey = AikumaSettings.APPROVED_SPEAKERS_KEY;
	private final String archiveSpKey = AikumaSettings.ARCHIVED_SPEAKERS_KEY;
	private final String cloudIdFormat = "^v\\d{2}\\/\\S\\/\\S{2}\\/.+\\/.+\\/.+\\/.+$";
	
	private Timer timer;
	private SharedPreferences preferences;
	private Editor prefsEditor;
	private Set<String> approvedRecordingSet;
	private Set<String> archivedRecordingSet;
	private Set<String> approvedSpeakerSet;
	private Set<String> archivedSpeakerSet;
	
	private String googleEmailAccount;
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
		approvedRecordingSet = (HashSet<String>)
				preferences.getStringSet(approvalKey, new HashSet<String>());
		archivedRecordingSet = (HashSet<String>)
				preferences.getStringSet(archiveKey, new HashSet<String>());
		approvedSpeakerSet = (HashSet<String>)
				preferences.getStringSet(approvalSpKey, new HashSet<String>());
		archivedSpeakerSet = (HashSet<String>)
				preferences.getStringSet(archiveSpKey, new HashSet<String>());
		
		prefsEditor = preferences.edit();
		
		googleEmailAccount = AikumaSettings.getCurrentUserId();
		googleAuthToken = AikumaSettings.getCurrentUserToken();
		
		Log.i(TAG, "Cloud created(recording-approve):" + approvedRecordingSet.toString());
		Log.i(TAG, "Cloud created(recording-archive):" + archivedRecordingSet.toString());
		Log.i(TAG, "Cloud created(speaker-approve):" + approvedSpeakerSet.toString());
		Log.i(TAG, "Cloud created(speaker-archive):" + archivedSpeakerSet.toString());
		
		// schedule task
		timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	@Override
        	public void run() {
        		Log.i(TAG, "run by timer");
        		
        		backUp();
        		autoDownloadFiles();
        		broadcastStatus("end");
        	}
        }, AikumaSettings.SYNC_INTERVAL, AikumaSettings.SYNC_INTERVAL);  
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
		} else if(id.equals("cancel")) {
			timer.cancel();
		} else {						// Called when archive button is pressed
			String itemType = (String)
					intent.getExtras().get("type");
			// id : (version)-(file's ID)
			if(itemType.equals("recording"))
				archive(id, 0);
			else
				archive(id, 1);
		}
		broadcastStatus("end");
	}
	
	private void autoDownloadFiles() {
		if(googleAuthToken == null)
			return;
		
		FusionIndex fi = new FusionIndex(googleAuthToken);
		GoogleDriveStorage gd = new GoogleDriveStorage(googleAuthToken);
		
		String emailAddr = preferences.getString(AikumaSettings.SETTING_OWNER_ID_KEY, null);
		Map<String,String> criteria = new HashMap<String, String>();
		
		if(emailAddr != null)
			criteria.put("user_id", emailAddr);
        
		// Search the items not existing in this device 
		// 1. Collect file-IDs in Cloud
        final List<String> archivedRecordingIds = new ArrayList<String>();// = fi.search(criteria);
        final List<String> archivedSpeakerIds = new ArrayList<String>();
        
        gd.list(new GoogleDriveStorage.ListItemHandler() {
			@Override
			public boolean processItem(String identifier, Date date) {
				// Classify identifiers and store them in different lists 
				if(identifier.matches(cloudIdFormat)) {
					String relPath = identifier.substring(0, identifier.lastIndexOf('/')-12);
					if(relPath.endsWith(Recording.PATH) && 
							!identifier.endsWith("-mapping.txt")) {
						archivedRecordingIds.add(identifier);
					} else if(relPath.endsWith(Speaker.PATH)){
						archivedSpeakerIds.add(identifier);
					}
				}
				
				return true;
			}
		});
        
        // 2. Collect file-IDs in device
        List<Recording> recordings = Recording.readAll();
        List<Speaker> speakers = Speaker.readAll();
        List<String> recordingIds = new ArrayList<String>();
        List<String> speakerIds = new ArrayList<String>();
        for(Recording item : recordings) {
        	String identifier = item.getCloudIdentifier();
        	recordingIds.add(identifier);
        }
        for(Speaker item : speakers) {
        	speakerIds.add(item.getCloudIdentifier(1));
        	speakerIds.add(item.getCloudIdentifier(2));
        }
        
        // 3. Filtering
        Log.i(TAG, "Recordings in cloud: " + archivedRecordingIds.toString());
        Log.i(TAG, "Recordings in device: " + recordingIds.toString());
        Log.i(TAG, "Speakers in cloud: " + archivedSpeakerIds.toString());
        Log.i(TAG, "Speakers in device: " + speakerIds.toString());
        
        archivedRecordingIds.removeAll(recordingIds);
        archivedSpeakerIds.removeAll(speakerIds);
        
        // Download the filtered speaker items
        for(String itemId : archivedSpeakerIds) {
    		broadcastStatus("start");
        	Log.i(TAG, "id: " + itemId);
        	String[] buf = itemId.split("\\/");
        	
        	String relPath = itemId.substring(0, itemId.lastIndexOf('/'));
        	String id = itemId.substring(itemId.lastIndexOf('/')+1);
        	
        	//String groupId = buf[];
        	//File dir = new File(Speaker.getSpeakersPath(), groupId);
        	File dir = new File(FileIO.getAppRootPath(), relPath);
        	dir.mkdirs();
        	
			try {
				// Write the speaker file
				InputStream is = gd.load(itemId);
	        	FileOutputStream fos = new FileOutputStream(new File(dir, id));
	        	Utils.copyStream(is, fos, true);
	        	
	        	// Log that files are archived
	        	if(buf[buf.length-1].endsWith("-metadata.json")) {
	        		String name = buf[buf.length-1].split("\\-")[0];
	            	archivedSpeakerSet.add(name);
	        		prefsEditor.putStringSet(archiveSpKey, archivedSpeakerSet);
	        		prefsEditor.commit();
	        	}
			} catch (FileNotFoundException e) {
        		Log.e(TAG, e.getMessage());
        	} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
        	
        }
        
        
        // Download the filtered recording items
        // itemId : path + recording-name + extension / id : recording-name + extension / name : recording-name
        for(String itemId : archivedRecordingIds) {
        	Log.i(TAG, "id: " + itemId);
        	Map<String, String> meta = fi.getItemMetadata(itemId);	
        	String metadataJSONStr = meta.get("metadata");
        	
        	String relPath = itemId.substring(0, itemId.lastIndexOf('/'));
        	String id = itemId.substring(itemId.lastIndexOf('/')+1);
        	String name = id.substring(0, id.length()-4);
        	
        	//String groupId = meta.get("item_id");
        	//File dir = new File(Recording.getRecordingsPath(), groupId);
        	File dir = new File(FileIO.getAppRootPath(), relPath);
        	dir.mkdirs();
        	
        	try{
        		// Write the recording file
            	InputStream is = gd.load(itemId);
            	FileOutputStream fos = new FileOutputStream(new File(dir, id));
            	Utils.copyStream(is, fos, true);
            	
            	// Write the recording metadata
            	FileIO.write(new File(dir, name + "-metadata.json"), metadataJSONStr);
            	
            	// Write other files(map/transcript)
            	if (name.split("-")[2].equals("source")) {
            		//TODO: upload transcript file	
    			} else {
    				// download *-mapping.txt file
    				String mapFileId = 
    						itemId.substring(0, itemId.length()-4) + "-mapping.txt";
    				InputStream is2 = gd.load(mapFileId);
                	FileOutputStream fos2 = 
                			new FileOutputStream(new File(dir, name + "-mapping.txt"));
                	Utils.copyStream(is2, fos2, true);
    			}
            	
            	// Log that files are already archived
            	archivedRecordingSet.add(name);
        		prefsEditor.putStringSet(archiveKey, archivedRecordingSet);
        		prefsEditor.commit();
        		
        		broadcastStatus(itemId.substring(0, 3) + "-" + name);
        	} catch (FileNotFoundException e) {
        		Log.e(TAG, "no-found:" + e.getMessage());
        	} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
        	
        }   
        
	}
	
	
	
	/**
	 *  Back-up function: add all recording/speaker-items to the Set 
	 *  and call retry() to upload the items
	 */
	private void backUp() {
		//List<Recording> recordings = Recording.readAll();
		List<Recording> recordings = Recording.readAll(googleEmailAccount);
		
		List<Speaker> speakers = Speaker.readAll();
		
		String requestDate = new StandardDateFormat().
				format(new Date()).toString();
		String archiveState = (requestDate + "|" + "0");
		
		for(Recording recording : recordings) {
			if(!archivedRecordingSet.contains(recording.getId())) {
				String recordingVerId = recording.getVersionName() + "-" + recording.getId();
				approvedRecordingSet.add(recordingVerId);
				prefsEditor.putString(recording.getId(), 
						archiveState);
			}
		}
		
		for(Speaker speaker : speakers) {
			if(!archivedSpeakerSet.contains(speaker.getId())) {
				String speakerVerId = 
						speaker.getVersionName() + "-" + speaker.getOwnerId() + "-" + speaker.getId();
				approvedSpeakerSet.add(speakerVerId);
				prefsEditor.putString(speaker.getId(),
						archiveState);
			}
		}
		
		prefsEditor.putStringSet(approvalKey, approvedRecordingSet);
		prefsEditor.putStringSet(approvalSpKey, approvedSpeakerSet);
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
		// Recordings
		Set<String> recordings = new HashSet<String>(approvedRecordingSet);
		for(String recordingVerId : recordings) {
			Recording recording;
			String[] splitRecordingName = recordingVerId.split("-");
			
			String versionName = splitRecordingName[0];
			String ownerId = splitRecordingName[2];
			String recordingId = recordingVerId.substring(4);
			try {
				recording = Recording.read(versionName, ownerId, recordingId);
				if(!archivedRecordingSet.contains(recordingId)) {
					broadcastStatus("start");
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
					updateApprovalArchiveSet(recordingVerId, approvalKey, approvedRecordingSet,
							archiveKey, archivedRecordingSet);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "archive failed: " + recordingId);
			}
		}
		
		// Speakers
		Set<String> speakers = new HashSet<String>(approvedSpeakerSet);
		for(String speakerVerId : speakers) {
			String[] splitSpeakerName = speakerVerId.split("-");
			
			String versionName = splitSpeakerName[0];
			String ownerId = splitSpeakerName[1];
			String speakerId = splitSpeakerName[2];
			try {
				Speaker speaker = Speaker.read(versionName, ownerId, speakerId);
				if(!archivedSpeakerSet.contains(speakerId)) {
					broadcastStatus("start");
					String[] requestArchiveState = 
							preferences.getString(speakerId, "").split("\\|");
					String requestDate = requestArchiveState[0];
					int archiveProgress = 
							Integer.parseInt(requestArchiveState[1]);
					
					startArchiving(speaker, requestDate, archiveProgress);
				} else {
					updateApprovalArchiveSet(speakerVerId, approvalSpKey, approvedSpeakerSet,
							archiveSpKey, archivedSpeakerSet);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "archive failed: " + speakerId);
			}
			
		}
	}
	
	/**
	 * Upload the recording/speaker-item having id
	 * 
	 * @param verId	Version and ID of the recording item
	 * @param type	Type of the item(0: recording, 1: speaker)
	 */
	private void archive(String verId, int type) {
		String requestDate = new StandardDateFormat().
				format(new Date()).toString();
		String archiveState = (requestDate + "|" + "0");
		
		try {
			if(type == 0) {
				String[] splitName = verId.split("-");
				
				String versionName = splitName[0];
				String ownerId = splitName[2];
				String id = verId.substring(4);
				
				Recording recording = Recording.read(versionName, ownerId, id);
				if(!archivedRecordingSet.contains(id)) {
					// Record archive-approved date
					approvedRecordingSet.add(verId);
					prefsEditor.putStringSet(approvalKey, approvedRecordingSet);
					prefsEditor.putString(id, archiveState);
					
					prefsEditor.commit();
					startArchiving(recording, requestDate, null, 0);
				} else {
					updateApprovalArchiveSet(verId, approvalKey, approvedRecordingSet,
							archiveKey, archivedRecordingSet);
				}
				// Archive speakers together
				List<String> speakerIds = recording.getSpeakersIds();
				for(String speakerId : speakerIds) {
					//Assume that recording and speaker's versions/owner are the same
					archive(versionName + "-" + ownerId + "-" + speakerId, 1);
				}
			} else {
				String[] splitName = verId.split("-");
				
				String versionName = splitName[0];
				String ownerId = splitName[1];
				String id = splitName[2];
				
				Speaker speaker = Speaker.read(versionName, ownerId, id);
				if(!archivedSpeakerSet.contains(id)) {
					approvedSpeakerSet.add(verId);
					prefsEditor.putStringSet(approvalSpKey, approvedSpeakerSet);
					prefsEditor.putString(id, archiveState);
					
					prefsEditor.commit();
					startArchiving(speaker, requestDate, 0);
				} else {
					updateApprovalArchiveSet(verId, approvalSpKey, approvedSpeakerSet, 
							archiveSpKey, archivedSpeakerSet);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "archive failed: " + verId);
		}
	}

	// If the recording/speaker-item archiving is finished, update the Approval/Archive Set
	private void updateApprovalArchiveSet(String verId, String apKey, Set<String> approvedSet, 
			String arKey, Set<String> archivedSet) {
		String id = verId.substring(4);
		approvedSet.remove(verId);
		prefsEditor.putStringSet(apKey, approvedSet);
		prefsEditor.remove(id);
		
		archivedSet.add(id);
		prefsEditor.putStringSet(arKey, archivedSet);
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
		String id = recording.getId();
		String verId = recording.getVersionName() + "-" + recording.getId();
		String identifier = recording.getCloudIdentifier();		// path + id + [.wav|.mp4]
		Log.i(TAG, "cloud-Id: " + identifier);
		switch(state) {
		case 0:
			File recordingFile = recording.getFile();
			uri = uploadFile(recordingFile, identifier);
			if(uri == null) return;
			if (recording.isOriginal()) {
				// TODO: upload transcript file
			} else {
				// upload *.map file
				File mapFile = recording.getMapFile();
				String mapIdentifier = recording.getMapFileCloudId();
				uri = uploadFile(mapFile, mapIdentifier);
				if(uri == null) return;
			}
			String recordingArchiveState = (requestDate + "|1|" + uri);
			prefsEditor.putString(id, recordingArchiveState);
			prefsEditor.commit();
		case 1:
			Date uploadDate = 
				uploadMetadata(recording, identifier, requestDate, uri);
			if(uploadDate != null) {
				recording.archive(uploadDate.toString(), uri);
				
				updateApprovalArchiveSet(verId, approvalKey, approvedRecordingSet, 
						archiveKey, archivedRecordingSet);
			}
		}
	}
	
	/**
	 * Start uploading the speaker-item
	 * Archiving-state(0:approved, 1:small-image-uploaded, 2:metadata-uploaded)
	 * 
	 * @param speaker		The speaker-item to be archived
	 * @param requestDate	Archive-approval date
	 * @param state			Archiving-state
	 * @throws IOException	Exception during file-processing
	 */
	private void startArchiving(Speaker speaker, String requestDate, int state) throws IOException {
		String id = speaker.getId();
		String verId = speaker.getVersionName() + "-" + speaker.getOwnerId() + "-" + speaker.getId();
		String identifier, uri;
		
		switch(state) {
		case 0:
			File spSmallImgFile = speaker.getSmallImageFile();
			identifier = speaker.getCloudIdentifier(1);
			uri = uploadFile(spSmallImgFile, identifier);
			if(uri == null) return;
			Log.i(TAG, "cloud-Id: " + identifier);
			String speakerArchiveState = (requestDate + "|1");
			prefsEditor.putString(id, speakerArchiveState);
			prefsEditor.commit();
		case 1:
			File spMetadataFile = speaker.getMetadataFile();
			identifier = speaker.getCloudIdentifier(2);
			uri = uploadFile(spMetadataFile, identifier);
			if(uri == null) return;
			Log.i(TAG, "cloud-Id: " + identifier);
			updateApprovalArchiveSet(verId, approvalSpKey, approvedSpeakerSet,
					archiveSpKey, archivedSpeakerSet);
		}
	}
	
	//upload the file to Google Drive
	private String uploadFile(File file, String identifier) throws IOException {
		Data data = Data.fromFile(file);
		if (data == null) {
			Log.e(TAG, "Source file doesn't exist(file:" + file.getName() + ", ID:" + identifier +")");
			return null;		
		}
		
		DataStore gd = new GoogleDriveStorage(googleAuthToken);
		String uri = gd.store(identifier, data);
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
		JSONArray speakers_arr = (JSONArray) jsonfile.get("speakers");
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
		String emailAddr = recording.getOwnerId();
		if (emailAddr == null) {
			Log.i(TAG, "defaultGoogleAccount is null");
			return null;
		}
		metadata.put("user_id", emailAddr);
		metadata.put("metadata", jsonstr);
		metadata.put("data_store_uri", uri);
		metadata.put("item_id", (String) jsonfile.get("item_id"));
		metadata.put("file_type", (String) jsonfile.get("file_type"));
		metadata.put("speakers", speakers);
		metadata.put("languages", languages);
		metadata.put("date_approved", requestDate);
		Date uploadDate = new Date();
		metadata.put("date_backedup", 
				new StandardDateFormat().format(uploadDate).toString());
		
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
	
	// status(0: start, 1:new file download, 2:stop)
	private void broadcastStatus(String status) {
		Intent intent = new Intent(SYNC_RESULT);
		intent.putExtra(SYNC_STATUS, status);    
	    sendBroadcast(intent);
	}

}
