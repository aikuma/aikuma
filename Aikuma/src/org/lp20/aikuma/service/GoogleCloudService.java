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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.MainActivity;
import org.lp20.aikuma.model.FileModel;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.storage.Data;
import org.lp20.aikuma.storage.DataStore;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.GoogleAuth;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.Utils;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.StandardDateFormat;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
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
	public final static String SYNC_RESULT = "org.lp20.aikuma.sync.result";
	/**
	 * Key of data in the broadcast message
	 */
	public final static String SYNC_STATUS = "sync_status";
	
	/**
	 * A list of keys used to start GoogleCloudService
	 */
	public final static String ACTION_KEY = "id";
	/* */
	public final static String ACCOUNT_KEY = "emailAccount";
	/* */
	public final static String TOKEN_KEY = "authToken";
	/* */
	public final static String ARCHIVE_FILE_TYPE_KEY = "type";
	
	private final static String TAG = "GoogleCloudService";
	
	private final String approvalKey = AikumaSettings.APPROVED_RECORDING_KEY;
	private final String archiveKey = AikumaSettings.ARCHIVED_RECORDING_KEY;
	private final String approvalSpKey = AikumaSettings.APPROVED_SPEAKERS_KEY;
	private final String archiveSpKey = AikumaSettings.ARCHIVED_SPEAKERS_KEY;
	private final String approvalOtherKey = AikumaSettings.APPROVED_OTHERS_KEY;
	private final String archiveOtherKey = AikumaSettings.ARCHIVED_OTHERS_KEY;
	
	private final String cloudIdFormat = "^v\\d{2}\\/\\S\\/\\S{2}\\/.+\\/.+\\/.+\\/.+$";
	
	private SharedPreferences preferences;
	private Editor prefsEditor;
	private Set<String> approvedRecordingSet;
	private Set<String> archivedRecordingSet;
	private Set<String> approvedSpeakerSet;
	private Set<String> archivedSpeakerSet;
	private Set<String> approvedOtherSet;	// Other items to be uploaded(transcript, mapping)
	private Set<String> archivedOtherSet;
	
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
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String id = intent.getStringExtra(ACTION_KEY);
		Log.i(TAG, "Receive intent: " + id);
		googleEmailAccount = intent.getStringExtra(ACCOUNT_KEY);
		googleAuthToken = intent.getStringExtra(TOKEN_KEY);
		prepareSettings(googleEmailAccount);
		
		if(id.equals("sync")) {
			validateToken();
			backUp();
			retry();
			autoDownloadFiles();
		} else if(id.equals("backup")) {		// Called when backup-setting is enabled
			validateToken();
			backUp();
			retry();
		} else if(id.equals("retry")) {	// Called with the start of application
			validateToken();
			retry();
		} else if(id.equals("autoDownload")) {
			validateToken();
			autoDownloadFiles();
		} else {						// Called when archive button is pressed (and token was already validated)
			String itemType = (String)
					intent.getExtras().get("type");
			// id : (version)-(file's ID)
			if(itemType.equals("recording"))
				archive(id, 0);
			else
				archive(id, 1);
			
			retry();
		}
		broadcastStatus("end");
	}
	
	private void prepareSettings(String emailAccount) {

		preferences = this.getSharedPreferences(emailAccount, MODE_PRIVATE);

		approvedRecordingSet = (HashSet<String>)
				preferences.getStringSet(approvalKey, new HashSet<String>());
		archivedRecordingSet = (HashSet<String>)
				preferences.getStringSet(archiveKey, new HashSet<String>());
		approvedSpeakerSet = (HashSet<String>)
				preferences.getStringSet(approvalSpKey, new HashSet<String>());
		archivedSpeakerSet = (HashSet<String>)
				preferences.getStringSet(archiveSpKey, new HashSet<String>());
		approvedOtherSet = (HashSet<String>)
				preferences.getStringSet(approvalOtherKey, new HashSet<String>());
		archivedOtherSet = (HashSet<String>)
				preferences.getStringSet(archiveOtherKey, new HashSet<String>());
		
		
		prefsEditor = preferences.edit();
		
		Log.i(TAG, "Cloud created(recording-approve):" + approvedRecordingSet.toString());
		Log.i(TAG, "Cloud created(recording-archive):" + archivedRecordingSet.toString());
		Log.i(TAG, "Cloud created(speaker-approve):" + approvedSpeakerSet.toString());
		Log.i(TAG, "Cloud created(speaker-archive):" + archivedSpeakerSet.toString());
		Log.i(TAG, "Cloud created(other-approve):" + approvedOtherSet.toString());
		Log.i(TAG, "Cloud created(otherr-archive):" + archivedOtherSet.toString());
	}
	
	private void autoDownloadFiles() {
		if(googleAuthToken == null || !Aikuma.isDeviceOnline())
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
	 *  Back-up function: add all recording/speaker-items to the Set (Log all items)
	 *  (and retry() will upload the items)
	 */
	private void backUp() {
		List<Recording> recordings = Recording.readAll();
		List<Speaker> speakers = Speaker.readAll();
		List<FileModel> others = new ArrayList<FileModel>();
		
		for(Recording recording : recordings) {
			if(recording.isOriginal() && recording.getTranscriptFile() != null) {
				others.add(new FileModel(recording.getVersionName(), recording.getOwnerId(), 
						recording.getTranscriptId(), "other", "txt"));
			} else {
				others.add(new FileModel(recording.getVersionName(), recording.getOwnerId(), 
						recording.getMapId(), "other", "txt"));
			}
		}

		logFileInApprovalSet(recordings, false, approvalKey, approvedRecordingSet, archivedRecordingSet);
		logFileInApprovalSet(speakers, false, approvalSpKey, approvedSpeakerSet, archivedSpeakerSet);
		logFileInApprovalSet(others, true, approvalOtherKey, approvedOtherSet, archivedOtherSet);
		
		prefsEditor.commit();
	}

	/**
	 *  Retry function: Upload all recording-items in the Set
	 */
	private void retry() {
		Log.i(TAG, "retry start");
		if(googleAuthToken == null || !Aikuma.isDeviceOnline())
			return;
		
		// Recordings
		retry("recording", approvalKey, approvedRecordingSet, 
				archiveKey, archivedRecordingSet);
		
		// Speakers
		retry("speaker", approvalSpKey, approvedSpeakerSet,
				archiveSpKey, archivedSpeakerSet);
		
		// Others
		retry("other", approvalOtherKey, approvedOtherSet, 
				archiveOtherKey, archivedOtherSet);
	}
	
	private void retry(String type,
			String apKey, Set<String> approvedSet, String arKey, Set<String> archivedSet) {
		
		Set<String> itemVerIdExts = new HashSet<String>(approvedSet);
		
		for(String itemVerIdExt : itemVerIdExts) {
			String[] splitItemNameExt = itemVerIdExt.split("\\.");
			String itemVerId = splitItemNameExt[0];
			String format = splitItemNameExt[1];
			
			String[] splitItemName = itemVerId.split("-");
			String versionName = splitItemName[0];
			String ownerId = splitItemName[2];
			String itemId;
			if(type.equals("speaker")) {
				itemId = splitItemName[1];
			} else {
				itemId = itemVerId.substring(4);
			}
			
			FileModel item = new FileModel(versionName, ownerId, itemId, type, format);
			if(!archivedSet.contains(itemId)) {
				broadcastStatus("start");
				
				// Get the current state of archiving
				String[] requestArchiveState = 
						preferences.getString(itemId, "").split("\\|");
				String requestDate = requestArchiveState[0];
				int archiveProgress = 
						Integer.parseInt(requestArchiveState[1]);
				
				Log.i(TAG, itemId + "-state: " + requestArchiveState[0]+"|"+requestArchiveState[1]);
				
				String uri = null;
				if (requestArchiveState.length >= 3 && requestArchiveState[2].length() > 0)
					uri = requestArchiveState[2];
					
				try {
					startArchiving(item, requestDate, uri, archiveProgress,
							apKey, approvedSet, arKey, archivedSet);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			} else {
				updateApprovalArchiveSet(itemVerIdExt, type, 
						apKey, approvedSet, arKey, archivedSet);
			}	
		}
		
	}
	
	/**
	 * Log the recording/speaker-item of id to be uploaded
	 * 
	 * @param verId	Version and ID of the recording item
	 * @param type	Type of the item(0: recording, 1: speaker)
	 */
	private void archive(String verId, int type) {
		
		String[] splitName = verId.split("-");
		
		String versionName = splitName[0];
		String ownerId = splitName[2];
		
		try {
			if(type == 0) {	// Recording
				String id = verId.substring(4);
				
				Recording recording = Recording.read(versionName, ownerId, id);
				List<Recording> oneRecording = new ArrayList<Recording>();
				oneRecording.add(recording);
				List<FileModel> speakers = recording.getSpeakers();
				List<FileModel> other = new ArrayList<FileModel>();
				if(recording.isOriginal() && recording.getTranscriptFile() != null) {
					other.add(new FileModel(recording.getVersionName(), recording.getOwnerId(), 
							recording.getTranscriptId(), "other", "txt"));
				} else {
					other.add(new FileModel(recording.getVersionName(), recording.getOwnerId(), 
							recording.getMapId(), "other", "txt"));
				}
				
				
				logFileInApprovalSet(oneRecording, false, approvalKey, approvedRecordingSet, archivedRecordingSet);
				logFileInApprovalSet(speakers, false, approvalSpKey, approvedSpeakerSet, archivedSpeakerSet);
				logFileInApprovalSet(other, true, approvalOtherKey, approvedOtherSet, archivedOtherSet);
			
			} else {	// Speaker
				String id = splitName[1];
				
				Speaker speaker = Speaker.read(versionName, ownerId, id);
				List<Speaker> speakers = new ArrayList<Speaker>();
				speakers.add(speaker);
				
				logFileInApprovalSet(speakers, false, approvalSpKey, approvedSpeakerSet, archivedSpeakerSet);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "archive failed: " + verId);
		}
		
		prefsEditor.commit();
	}
	
	/**
	 *  Log the file approved to be uploaded
	 *  MyFile				(Recording, Speaker) : 0(file-upload) -> 1(metadata upload) -> 2(indexing in FusionTable)
	 *  MyFile		(Others:mapping, transcript) : 3(file-upload)                       -> 2(indexing in FusionTable)
	 *  Other-owner's file	(Recording, Speaker) : 4(file-upload) -> 5(metadata upload)
	 *  Other-owner's file (mapping, transcript) : 6(file-upload)
	 */
	
	private void logFileInApprovalSet(List<? extends FileModel> items, boolean isOtherType,
			String apKey, Set<String> approvedSet, Set<String> archivedSet) {
		String requestDate = new StandardDateFormat().
				format(new Date()).toString();
		String archiveState;
		
		for(FileModel item : items) {
			if(!archivedSet.contains(item.getId())) {
				String itemVerIdExt = item.getVerIdFormat();
				approvedSet.add(itemVerIdExt);
				
				String ownerId = item.getOwnerId();
				if(ownerId.equals(googleEmailAccount)) {
					if(isOtherType) {
						archiveState = (requestDate + "|3");
					} else {
						archiveState = (requestDate + "|0");
					}
					
				} else {
					if(isOtherType) {
						archiveState = (requestDate + "|6");
					} else {
						archiveState = (requestDate + "|4");
					}
				}
				
				prefsEditor.putString(item.getId(), archiveState);
			}
		}
		
		prefsEditor.putStringSet(apKey, approvedSet);
	}
	
	
	// If the recording/speaker-item archiving is finished, update the Approval/Archive Set
	private void updateApprovalArchiveSet(String verIdExt, String type, 
			String apKey, Set<String> approvedSet, String arKey, Set<String> archivedSet) {
		String verId = verIdExt.split("\\.")[0];
		String id;
		if(type.equals("speaker")) {
			id = verId.split("-")[1];
		} else {
			id = verId.substring(4);
		}
		
		prefsEditor.remove(id);
		
		approvedSet.remove(verIdExt);
		prefsEditor.putStringSet(apKey, approvedSet);
		
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
	
	private void startArchiving(FileModel item, String requestDate, String uri, int state,
			String apKey, Set<String> approvedSet, String arKey, Set<String> archivedSet) throws IOException {
		String id = item.getId();
		String identifier = item.getCloudIdentifier(0);	// path + id + [.wav|.mp4|-small-image.jpg|.txt]
		String itemArchiveState;
		File itemFile;
		Log.i(TAG, "cloud-Id: " + identifier);
		
		switch(state) {
		case 0:
		case 3:
		case 4:
		case 6:	
			itemFile = item.getFile(0);
			uri = uploadFile(itemFile, identifier);

			if(uri == null) return;

			if(state == 0) {
				itemArchiveState = (requestDate + "|1|" + uri);
			}
			else if(state == 3) {
				itemArchiveState = (requestDate + "|2|" + uri);
			}
			else if(state == 4) {
				itemArchiveState = (requestDate + "|5|" + uri);
			} else {
				updateApprovalArchiveSet(item.getVerIdFormat(), item.getFileType(), 
						apKey, approvedSet, arKey, archivedSet);
				return;
			}
			
			prefsEditor.putString(id, itemArchiveState);
			prefsEditor.commit();
		case 1:
		case 5:
			String metaIdentifier = item.getCloudIdentifier(1);		// path + id(recording/speaker) + [-metadata.json]
			Log.i(TAG, "metafile-cloud-Id: " + metaIdentifier);
			itemFile = item.getFile(1);
			uri = uploadFile(itemFile, metaIdentifier);

			if(uri == null) return;
			
			if(state == 1) {
				itemArchiveState = (requestDate + "|2|" + uri);
				prefsEditor.putString(id, itemArchiveState);
				prefsEditor.commit();
			} else {
				updateApprovalArchiveSet(item.getVerIdFormat(), item.getFileType(), 
						apKey, approvedSet, arKey, archivedSet);
				return;
			}	
		case 2:
			Date uploadDate = 
				uploadMetadata(item, identifier, requestDate, uri);
			if(uploadDate == null) return;
			
			//recording.archive(uploadDate.toString(), uri);
			updateApprovalArchiveSet(item.getVerIdFormat(), item.getFileType(), 
					apKey, approvedSet, arKey, archivedSet);
			return;
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
	private Date uploadMetadata(
			FileModel item, String identifier, String requestDate, String uri) throws IOException {
		
		Date uploadDate = new Date();
		FusionIndex fi = new FusionIndex(googleAuthToken);
		Map<String, String> metadata = new HashMap<String,String>();
		
		String[] splitName = item.getId().split("-");
		String emailAddr = item.getOwnerId();
		if (emailAddr == null) {
			Log.e(TAG, "itemOwnerAccount is null");
			return null;
		}
		
		File metadataFile = item.getFile(1);
		if(metadataFile != null) {	// Recording or Speaker
			String jsonstr;
			try {
				jsonstr = FileIO.read(metadataFile);
			} catch (IOException e) {
				Log.e(TAG, "Failed to read metadata file: " + metadataFile.getPath());
				return null;
			}
			JSONObject jsonfile = (JSONObject) JSONValue.parse(jsonstr);
			
			String groupId;
			String speakers = "";
			String joiner = "";
			if(item.getFileType().equals("speaker")) {
				groupId = (String) jsonfile.get("id");
			} else {
				groupId = (String) jsonfile.get("item_id");
				
				JSONArray speakers_arr = (JSONArray) jsonfile.get("speakers");
				for (Object obj: speakers_arr) {
					speakers += joiner + (String) obj;
					joiner = ",";
				}
			}
			
			String languages = "";
			joiner = "";
			for (Object obj: (JSONArray) jsonfile.get("languages")) {
				String lang = (String) ((JSONObject) obj).get("code");
				languages += joiner + lang;
				joiner = ",";
			}
			
			metadata.put("user_id", emailAddr);
			metadata.put("data_store_uri", uri);
			metadata.put("item_id", groupId);
			metadata.put("file_type", item.getFileType());
			metadata.put("speakers", speakers);
			metadata.put("languages", languages);
			metadata.put("metadata", jsonstr);
			
		} else {	// Other-type(transcript, mapping)
			metadata.put("user_id", emailAddr);
			metadata.put("data_store_uri", uri);
			
			String groupId = splitName[0];
			metadata.put("item_id", groupId);
			metadata.put("file_type", item.getFileType());
			metadata.put("speakers", "");
			metadata.put("languages", "");
		}
		
		metadata.put("date_approved", requestDate);
		metadata.put("date_backedup", 
				new StandardDateFormat().format(uploadDate).toString());
		
		// tags are used to group derivative-wave-file and mapping-file
		String suffix = splitName[splitName.length-1];
		if(suffix.length() == 3 && NumberUtils.isNumber(suffix)) {
			metadata.put("tags", suffix);
		}	
		
		
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
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void validateToken() {
		try {
        	// If the token is invalid, refresh token
        	if(googleAuthToken != null && Aikuma.isDeviceOnline() &&
    				!GoogleAuth.validateAccessToken(googleAuthToken)) {
    			GoogleAuthUtil.clearToken(getApplicationContext(), googleAuthToken);
    			googleAuthToken = GoogleAuthUtil.getToken(getApplicationContext(), 
    					googleEmailAccount, AikumaSettings.getScope());
    		}
        } catch (Exception e) {
            Log.e(TAG, "Unrecoverable error " + e.getMessage());
        }
	}
	
}
