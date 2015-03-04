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

import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.model.FileModel;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.storage.Data;
import org.lp20.aikuma.storage.DataStore;
import org.lp20.aikuma.storage.FusionIndex;
import org.lp20.aikuma.storage.FusionIndex2;
import org.lp20.aikuma.storage.GoogleAuth;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.Index;
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
	/** */
	public final static String ACCOUNT_KEY = "emailAccount";
	/** */
	public final static String TOKEN_KEY = "authToken";
	/** */
	public final static String ARCHIVE_FILE_TYPE_KEY = "type";
	
	private final static String TAG = "GoogleCloudService";
	
	private final String approvalKey = AikumaSettings.APPROVED_RECORDING_KEY;
	private final String downloadKey = AikumaSettings.DOWNLOAD_RECORDING_KEY;
	private final String archiveKey = AikumaSettings.ARCHIVED_RECORDING_KEY;
	private final String approvalSpKey = AikumaSettings.APPROVED_SPEAKERS_KEY;
	private final String downloadSpKey = AikumaSettings.DOWNLOAD_SPEAKERS_KEY;
	private final String archiveSpKey = AikumaSettings.ARCHIVED_SPEAKERS_KEY;
	private final String approvalOtherKey = AikumaSettings.APPROVED_OTHERS_KEY;
	private final String downloadOtherKey = AikumaSettings.DOWNLOAD_OTHERS_KEY;
	private final String archiveOtherKey = AikumaSettings.ARCHIVED_OTHERS_KEY;
	
	private final String cloudIdFormat = "^v\\d{2}\\/\\S\\/\\S{2}\\/.+\\/.+\\/.+\\/.+$";
	
	private SharedPreferences preferences;
	private Editor prefsEditor;
	private Set<String> approvedRecordingSet;
	private Set<String> downloadRecordingSet;
	private Set<String> archivedRecordingSet;
	private Set<String> approvedSpeakerSet;
	private Set<String> downloadSpeakerSet;
	private Set<String> archivedSpeakerSet;
	private Set<String> approvedOtherSet;	// Other items to be uploaded(transcript, mapping)
	private Set<String> downloadOtherSet;	// Other items to be downloaded(transcript, mapping)
	private Set<String> archivedOtherSet;
	
	private boolean forceSync;
	private List<Recording> recordings;
    private List<Speaker> speakers;
    private List<FileModel> others;
	
	private String googleEmailAccount;
	private String googleAuthToken;
	private String googleIdToken;
	
	private int numOfItemsToDownload = 0;
	private boolean isNewRecording = false;
	
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
		
		googleAuthToken = intent.getStringExtra(TOKEN_KEY);
		
		List<String> googleAccountList;
		if(googleAuthToken == null) {	
			// when the automatic-sync is checked or 
			// when the connectivity changes after the automatic-sync is enabled
			googleAccountList = intent.getStringArrayListExtra(ACCOUNT_KEY);
		} else {
			googleAccountList = new ArrayList<String>();
			googleAccountList.add(intent.getStringExtra(ACCOUNT_KEY));
		}
		
		forceSync = intent.getBooleanExtra("forceSync", false);
		
		Log.i(TAG, "device-connectivit: " + Aikuma.isDeviceOnline());
		// If this is triggered by (CloudSettingsActivity or BootReceiver),
		// sync happens across all accounts.
		for(String googleAccount : googleAccountList) {
			Log.i(TAG, "intent-action: " + id + " using " + googleAccount);
			
			googleEmailAccount = googleAccount;
			
			prepareSettings(googleEmailAccount);
			
			if(id.equals("sync")) {
				backUp();
				autoDownloadFiles();
				validateToken();
				retryBackup();
				retryDownload();
			} else if(id.equals("retry")) {	// Called with the start of application
				validateToken();
				retryBackup();
				retryDownload();
			} else if(id.equals("backup")) {	// Called when backup-setting is enabled
				backUp();
				validateToken();
				retryBackup(); 
			} else if(id.equals("autoDownload")) {
				autoDownloadFiles();
				validateToken();
				// TODO: In the current state, metadata will not exist in GoogleDrive (needs to be changed later)
				List<String> itemCloudIdsToDownload = intent.getStringArrayListExtra("downloadItems");
				if(itemCloudIdsToDownload != null) {
					for(String itemId : itemCloudIdsToDownload) {
						downloadOtherSet.add(itemId);
					}
				}
				retryDownload();
			} else {					// Called when archive button is pressed (and token was already validated)
				String itemType = (String)
						intent.getExtras().get("type");
				// id : (version)-(file's ID)
				if(itemType.equals("recording"))
					archive(id, 0);
				else
					archive(id, 1);
				
				validateToken();
				
				retryBackup();
			}
			
			googleAuthToken = null;
		}
		
		// Create an index file after cloud-activity is finished
		if(isNewRecording) {
			try {
				Recording.indexAll();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}	
		}
		
		broadcastStatus("end");
	}
	
	private void prepareSettings(String emailAccount) {

		preferences = this.getSharedPreferences(emailAccount, MODE_PRIVATE);

		approvedRecordingSet = (HashSet<String>)
				preferences.getStringSet(approvalKey, new HashSet<String>());
		downloadRecordingSet = (HashSet<String>)
				preferences.getStringSet(downloadKey, new HashSet<String>());
		archivedRecordingSet = (HashSet<String>)
				preferences.getStringSet(archiveKey, new HashSet<String>());
		
		approvedSpeakerSet = (HashSet<String>)
				preferences.getStringSet(approvalSpKey, new HashSet<String>());
		downloadSpeakerSet = (HashSet<String>)
				preferences.getStringSet(downloadSpKey, new HashSet<String>());
		archivedSpeakerSet = (HashSet<String>)
				preferences.getStringSet(archiveSpKey, new HashSet<String>());
		
		approvedOtherSet = (HashSet<String>)
				preferences.getStringSet(approvalOtherKey, new HashSet<String>());
		downloadOtherSet = (HashSet<String>)
				preferences.getStringSet(downloadOtherKey, new HashSet<String>());
		archivedOtherSet = (HashSet<String>)
				preferences.getStringSet(archiveOtherKey, new HashSet<String>());
		
		prefsEditor = preferences.edit();
		
		if(!Aikuma.isDeviceOnline())
			return;
		else if(googleAuthToken == null) {
			try {
				googleAuthToken = GoogleAuthUtil.getToken(
						getApplicationContext(), googleEmailAccount, AikumaSettings.getScope());
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
			
		if(archivedRecordingSet.size() + archivedSpeakerSet.size() + archivedOtherSet.size() == 0 || forceSync) {
			prepareArchiveSet();
		}
		
		Log.i(TAG, "Cloud created(recording-approve):" + approvedRecordingSet.toString());
		Log.i(TAG, "CLoud created(recording-download):" + downloadRecordingSet.toString());
		Log.i(TAG, "Cloud created(recording-archive):" + archivedRecordingSet.toString());
		Log.i(TAG, "Cloud created(speaker-approve):" + approvedSpeakerSet.toString());
		Log.i(TAG, "CLoud created(speaker-download):" + downloadSpeakerSet.toString());
		Log.i(TAG, "Cloud created(speaker-archive):" + archivedSpeakerSet.toString());
		Log.i(TAG, "Cloud created(other-approve):" + approvedOtherSet.toString());
		Log.i(TAG, "CLoud created(other-download):" + downloadOtherSet.toString());
		Log.i(TAG, "Cloud created(otherr-archive):" + archivedOtherSet.toString());
	}
	
	private void prepareArchiveSet() {
		DataStore gd;
		try {
			gd = new GoogleDriveStorage(googleAuthToken, 
					AikumaSettings.ROOT_FOLDER_ID, AikumaSettings.CENTRAL_USER_ID);
		} catch (DataStore.StorageException e) {
			Log.e(TAG, "Failed to initialize GoogleDriveStorage");
			return;
		}

		Log.i(TAG, "Start investigating");
		// Collect archived file-cloudIDs in GoogleCloud
		// TODO: For the case when there are too many files in GoogleDrive, it might need to use FusionIndex
        gd.list(new GoogleDriveStorage.ListItemHandler() {
			@Override
			public boolean processItem(String identifier, Date date) {
				// Classify identifiers and store them in different lists 
				if(identifier.matches(cloudIdFormat)) {
					String relPath = identifier.substring(0, identifier.lastIndexOf('/')-12);
					if(!identifier.endsWith("json")) {
						if(relPath.endsWith(Recording.PATH)) {
							if(identifier.endsWith("txt") || identifier.endsWith(FileModel.SAMPLE_SUFFIX)) {
								archivedOtherSet.add(identifier);
							} else {
								archivedRecordingSet.add(identifier);
							}
						} else if(relPath.endsWith(Speaker.PATH)){
							archivedSpeakerSet.add(identifier);
						}
					}
				}
				return true;
			}
		});
        
        prefsEditor.putStringSet(archiveKey, archivedRecordingSet);
        prefsEditor.putStringSet(archiveSpKey, archivedSpeakerSet);
        prefsEditor.putStringSet(archiveOtherKey, archivedOtherSet);
        
        prefsEditor.commit();
	}
	
	private void collectItemsInDevice() {
		if(recordings != null && speakers != null && others != null)
			return;
		
		recordings = Recording.readAll();
        speakers = Speaker.readAll();
        others = new ArrayList<FileModel>();
        
        for(Recording recording : recordings) {
			if(recording.isOriginal()) {
				if(recording.getTranscriptFile() != null) {
					others.add(new FileModel(recording.getVersionName(), recording.getOwnerId(), 
							recording.getTranscriptId(), "other", "txt"));
				}
				if(recording.getPreviewFile() != null) {
					others.add(new FileModel(recording.getVersionName(), recording.getOwnerId(), 
							recording.getPreviewId(), "preview", "wav"));
				}
			} else {
				others.add(new FileModel(recording.getVersionName(), recording.getOwnerId(), 
						recording.getMapId(), "other", "txt"));
			}
		}
	}
	
	private void autoDownloadFiles() {
		// Search the items not existing in this device  
        // 1. Collect files in device
        collectItemsInDevice();
        
        // 2. Filtering
        logFileInDownloadSet(archivedRecordingSet, false, downloadKey, downloadRecordingSet, recordings);
        logFileInDownloadSet(archivedSpeakerSet, false, downloadSpKey, downloadSpeakerSet, speakers);
        logFileInDownloadSet(archivedOtherSet, true, downloadOtherKey, downloadOtherSet, others);
        
        numOfItemsToDownload = downloadRecordingSet.size();
        
        prefsEditor.commit();
	}
	
	private void retryDownload() {
		if(googleAuthToken == null || !Aikuma.isDeviceOnline())
			return;
        
		DataStore gd;
		try {
			gd = new GoogleDriveStorage(googleAuthToken, 
					AikumaSettings.ROOT_FOLDER_ID, AikumaSettings.CENTRAL_USER_ID);
		} catch (DataStore.StorageException e) {
			Log.e(TAG, "Failed to initialize GoogleDriveStorage");
			return;
		}
		
		retryDownload(gd, 1, downloadOtherKey, downloadOtherSet);
        retryDownload(gd, 0, downloadSpKey, downloadSpeakerSet);
        retryDownload(gd, 0, downloadKey, downloadRecordingSet);
        
        if(numOfItemsToDownload - downloadRecordingSet.size() > 0) {
        	isNewRecording = true;
        }
	}
	
	private void retryDownload(DataStore gd, int state, String dlKey, Set<String> downloadSet) {
		Set<String> itemIdentifiers = new HashSet<String>(downloadSet);
		
		for(String itemIdentifier : itemIdentifiers) {
			Log.i(TAG, "download: " + itemIdentifier);
			broadcastStatus("start");
			FileModel item = FileModel.fromCloudId(itemIdentifier);
			
			String relPath = itemIdentifier.substring(0, itemIdentifier.lastIndexOf('/'));
			File dir = new File(FileIO.getAppRootPath(), relPath);
			dir.mkdirs();
			Log.i(TAG, dir.getAbsolutePath() + ", " + item.getMetadataIdExt());
			try {
				switch(state) {
				case 0:
					InputStream metaIs = gd.load(item.getCloudIdentifier(1));
					FileOutputStream metaFos = 
							new FileOutputStream(new File(dir, item.getMetadataIdExt()));
					if(metaIs == null || metaFos == null)
						break;
					Utils.copyStream(metaIs, metaFos, true);
				case 1:
					InputStream is = gd.load(itemIdentifier);
					FileOutputStream fos = 
							new FileOutputStream(new File(dir, item.getIdExt()));
					if(is == null || fos == null)
						break;
					Utils.copyStream(is, fos, true);
				
					downloadSet.remove(itemIdentifier);
					prefsEditor.putStringSet(dlKey, downloadSet);
					prefsEditor.commit();
					
					broadcastStatus(item.getVersionName() + "-" + item.getId());
				}
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
		collectItemsInDevice();

		logFileInApprovalSet(recordings, false, approvalKey, approvedRecordingSet, archivedRecordingSet);
		logFileInApprovalSet(speakers, false, approvalSpKey, approvedSpeakerSet, archivedSpeakerSet);
		logFileInApprovalSet(others, true, approvalOtherKey, approvedOtherSet, archivedOtherSet);
		
		prefsEditor.commit();
	}

	/**
	 *  Retry function: Upload all recording-items in the Set
	 */
	private void retryBackup() {
		if(googleAuthToken == null || !Aikuma.isDeviceOnline())
			return;
		DataStore gd;
		try {
			gd = new GoogleDriveStorage(googleAuthToken, 
					AikumaSettings.ROOT_FOLDER_ID, AikumaSettings.CENTRAL_USER_ID);
		} catch (DataStore.StorageException e) {
			Log.e(TAG, "Failed to initialize GoogleDriveStorage");
			return;
		}
		
		Index fi = new FusionIndex2(AikumaSettings.getIndexServerUrl(), googleIdToken, googleAuthToken);
		//Index fi = new FusionIndex(googleAuthToken);
		
		// Others
		retryBackup(gd, fi, approvalOtherKey, approvedOtherSet, 
				archiveOtherKey, archivedOtherSet);

		// Speakers
		retryBackup(gd, fi, approvalSpKey, approvedSpeakerSet,
				archiveSpKey, archivedSpeakerSet);
		
		// Recordings
		retryBackup(gd, fi, approvalKey, approvedRecordingSet, 
				archiveKey, archivedRecordingSet);
		
	}
	
	private void retryBackup(DataStore gd, Index fi, 
			String apKey, Set<String> approvedSet, String arKey, Set<String> archivedSet) {
		Set<String> itemIdentifiers = new HashSet<String>(approvedSet);

		for(String itemIdentifier : itemIdentifiers) {
			FileModel item = FileModel.fromCloudId(itemIdentifier);
			if(!archivedSet.contains(itemIdentifier)) {
				broadcastStatus("start");
				
				// Get the current state of archiving
				String[] requestArchiveState = 
						preferences.getString(itemIdentifier, "").split("\\|");
				String requestDate = requestArchiveState[0];
				int archiveProgress = 
						Integer.parseInt(requestArchiveState[1]);
				
				String uri = null;
				if (requestArchiveState.length >= 3 && requestArchiveState[2].length() > 0)
					uri = requestArchiveState[2];
					
				try {
					startArchiving(gd, fi, item, requestDate, uri, archiveProgress,
							apKey, approvedSet, arKey, archivedSet);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			} else {
				updateApprovalArchiveSet(itemIdentifier, 
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
				List<FileModel> recordingSpeakers = recording.getSpeakers();
				List<FileModel> other = new ArrayList<FileModel>();
				if(recording.isOriginal()) {
					if (recording.getTranscriptFile() != null) {
						other.add(new FileModel(recording.getVersionName(), recording.getOwnerId(), 
								recording.getTranscriptId(), "other", "txt"));
					}
					if (recording.getPreviewFile() != null) {
						other.add(new FileModel(recording.getVersionName(), recording.getOwnerId(), 
								recording.getPreviewId(), "preview", "wav"));
					}
				} else {
					other.add(new FileModel(recording.getVersionName(), recording.getOwnerId(), 
							recording.getMapId(), "other", "txt"));
				}
			
				logFileInApprovalSet(oneRecording, false, 
						approvalKey, approvedRecordingSet, archivedRecordingSet);
				logFileInApprovalSet(recordingSpeakers, false, 
						approvalSpKey, approvedSpeakerSet, archivedSpeakerSet);
				logFileInApprovalSet(other, true, 
						approvalOtherKey, approvedOtherSet, archivedOtherSet);
			
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
	 *  Log the file approved to be downloaded
	 *  (Recording, Speaker) 		: 0(metadata-download) -> 1(file-download)
	 *  (Others:mapping, transcript): 						  1(file-download)
	 */
	private void logFileInDownloadSet(Set<String> archivedSet, boolean isOtherType,
			String dlKey, Set<String> downloadSet, List<? extends FileModel> items) {
		Set<String> deviceItemIdSet = new HashSet<String>();
		for(FileModel item : items) {
			deviceItemIdSet.add(item.getCloudIdentifier(0));
		}
		
		for(String archivedItemId : archivedSet) {
			if(!deviceItemIdSet.contains(archivedItemId)) {
				downloadSet.add(archivedItemId);
			}
		}

		prefsEditor.putStringSet(dlKey, downloadSet);
	}
	
	
	/**
	 *  Log the file approved to be uploaded
	 *  MyFile						(Recording, Speaker) : 0(file-upload) -> 1(metadata upload) -> 2(indexing in FusionTable)
	 *  MyFile(others)	  (mapping, transcript, preview) : 3(file-upload)                       -> 2(indexing in FusionTable)
	 *  Other-owner's file			(Recording, Speaker) : 4(file-upload) -> 5(metadata upload)
	 *  Other-owner's file(mapping, transcript, preview) : 6(file-upload)
	 */
	private void logFileInApprovalSet(List<? extends FileModel> items, boolean isOtherType,
			String apKey, Set<String> approvedSet, Set<String> archivedSet) {
		String requestDate = new StandardDateFormat().
				format(new Date()).toString();
		String archiveState;
		
		for(FileModel item : items) {
			String identifier = item.getCloudIdentifier(0);
			if(!archivedSet.contains(identifier) && !approvedSet.contains(identifier)) {
				approvedSet.add(identifier);
				
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
				
				prefsEditor.putString(identifier, archiveState);
			}
		}
		
		prefsEditor.putStringSet(apKey, approvedSet);
	}
	
	
	// If the recording/speaker-item archiving is finished, update the Approval/Archive Set
	private void updateApprovalArchiveSet(String itemCloudId,
			String apKey, Set<String> approvedSet, String arKey, Set<String> archivedSet) {
		prefsEditor.remove(itemCloudId);
		
		approvedSet.remove(itemCloudId);
		prefsEditor.putStringSet(apKey, approvedSet);
		
		archivedSet.add(itemCloudId);
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
	
	private void startArchiving(DataStore gd, Index fi, FileModel item, String requestDate, String uri, int state,
			String apKey, Set<String> approvedSet, String arKey, Set<String> archivedSet) throws IOException {
		String identifier = item.getCloudIdentifier(0);	// path + id + [.wav|.mp4|-small-image.jpg|.txt]
		String itemArchiveState;
		File itemFile;
		Log.i(TAG, "cloud-Id: " + identifier + ", state: " + state);
		
		switch(state) {
		case 0:
		case 3:
		case 4:
		case 6:	
			itemFile = item.getFile(0);
			uri = uploadFile(gd, itemFile, identifier);

			if(uri == null) return;

			if(state == 0) {
				state = 1;
				itemArchiveState = (requestDate + "|1|" + uri);
			}
			else if(state == 3) {
				state = 2;
				itemArchiveState = (requestDate + "|2|" + uri);
			}
			else if(state == 4) {
				state = 5;
				itemArchiveState = (requestDate + "|5|" + uri);
			} else {
				updateApprovalArchiveSet(identifier,
						apKey, approvedSet, arKey, archivedSet);
				return;
			}
			
			prefsEditor.putString(identifier, itemArchiveState);
			prefsEditor.commit();
		case 1:
		case 5:
			if(state != 2) {
				String metaIdentifier = item.getCloudIdentifier(1);		// path + id(recording/speaker) + [-metadata.json]
				Log.i(TAG, "metafile-cloud-Id: " + metaIdentifier + ", state: " + state);
				itemFile = item.getFile(1);
				uri = uploadFile(gd, itemFile, metaIdentifier);

				if(uri == null) return;
				
				if(state == 1) {
					itemArchiveState = (requestDate + "|2|" + uri);
					prefsEditor.putString(identifier, itemArchiveState);
					prefsEditor.commit();
				} else {
					updateApprovalArchiveSet(identifier, 
							apKey, approvedSet, arKey, archivedSet);
					return;
				}
			}	
		case 2:
			Date uploadDate = 
				uploadMetadata(fi, gd, item, identifier, requestDate, uri);
			if(uploadDate == null) return;
			
			//recording.archive(uploadDate.toString(), uri);
			updateApprovalArchiveSet(identifier,
					apKey, approvedSet, arKey, archivedSet);
			return;
		}
	}

	
	//upload the file to Google Drive
	private String uploadFile(DataStore gd, File file, String identifier) throws IOException {
		
		Data data = Data.fromFile(file);
		if (data == null) {
			Log.e(TAG, "Source file doesn't exist(file:" + file.getName() + ", ID:" + identifier +")");
			return null;		
		}
		
		String uri = gd.store(identifier, data);
		if(uri != null)
			Log.i(TAG, "File-upload success");
		else
			Log.i(TAG, "File-Upload failed");
		return uri;
	}
	
	// Upload the metadata of the recording to FusionTable
	private Date uploadMetadata(Index fi, DataStore gd,
			FileModel item, String identifier, String requestDate, String uri) throws IOException {
		
		Date uploadDate = new Date();
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
		
		boolean isShared = gd.share(identifier);
		boolean isIndexed = fi.index(identifier, metadata);
		Log.i(TAG, "(" + isShared +", " + isIndexed + "): " + metadata.toString());
		
		if (isShared && isIndexed) {
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
			googleAuthToken = GoogleAuthUtil.getToken(getApplicationContext(), googleEmailAccount, AikumaSettings.getScope());
			
			googleIdToken = GoogleAuthUtil.getToken(getApplicationContext(), googleEmailAccount, AikumaSettings.getIdTokenScope());
		} catch (Exception e) {
			Log.e(TAG, "Unrecoverable error " + e.getMessage());
		}
	}
	
}
