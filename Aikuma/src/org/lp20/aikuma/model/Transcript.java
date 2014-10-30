/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.model;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.IdUtils;

import android.util.Log;

import com.google.common.io.Files;

/**
 * An "abstract" representation of a transcript file entity.
 *
 * @author	...
 */
public class Transcript {
	private static final String TAG = "Transcript";
	
	private String id;  // transcript file name
	private String group_id;
	private String person_id;
	private String version_name;
	private String owner_id;
	private String path;
	
	/**
	 * The constructor used when first creating a Transcript. The transcript
	 * is not created in the file system until the save() method is called.
	 * 
	 * @param versionName	The version of the transcription/recording format(v0x)
	 * @param ownerId		The owner ID of the transcription
	 * @param groupId The group ID of the original recording that is going
	 * 	to be transcribed by this Transcript object.
	 * @param transcriberId The person ID of the transcriber.
	 * @throws RuntimeException if the file can't be made.
	 */
	public Transcript(String versionName, String ownerId,
			String groupId, String transcriberId) throws RuntimeException {
		try {
			Speaker.read(versionName, ownerId, transcriberId);
		}
		catch (IOException e) {
			throw new RuntimeException("No such person: " + transcriberId);
		}

		String path = Recording.getRecordingsPath(
				FileIO.getOwnerPath(versionName, ownerId)) + "/" + groupId;
		File f = new File(path);
		if (!f.isDirectory()) {
			throw new RuntimeException("No such recording group: " + groupId);
		}
		
		StringBuilder build = new StringBuilder();
		build.append(groupId);
		build.append("-");
		build.append(transcriberId);
		build.append("-transcript-");
		build.append(IdUtils.randomDigitString(4));
		
		id = build.toString();
		group_id = groupId;
		person_id = transcriberId;
		version_name = versionName;
		owner_id = ownerId;
		path = Recording.getRecordingsPath(
				FileIO.getOwnerPath(versionName, ownerId)) + 
				"/" + group_id + "/" + id + ".txt";
		
		try {
			(new File(path)).createNewFile();
		}
		catch (IOException e) {
			throw new RuntimeException("Can't create transcript on file system.");
		}
	}
	
	/**
	 * The constructor used when reading an existing transcript.
	 * 
	 * @param versionName	The version of the transcription/recording format(v0x)
	 * @param ownerId		The owner ID of the transcription
	 * @param id Transcript file ID.
	 * @throws RuntimeException if the file can't be made.
	 */
	public Transcript(String versionName, String ownerId, 
			String id) throws RuntimeException {
		String[] a = id.split("-");
		this.id = id;
		group_id = a[0];
		person_id = a[1];
		version_name = versionName;
		owner_id = ownerId;
		path = Recording.getRecordingsPath(
				FileIO.getOwnerPath(versionName, ownerId)) + 
				"/" + group_id + "/" + id + ".txt";

		if (!a[2].equals("transcript"))
			throw new RuntimeException("Invalid transcript ID: " + id);
		
		File trs = new File(path);
		if (!trs.exists())
			throw new RuntimeException("No such file: " + path);
		
		try {
			Speaker.read(versionName, ownerId, a[1]);
		}
		catch (IOException e) {
			throw new RuntimeException("No such person: " + a[1]);
		}		
	}
	
	/**
	 * Scan file system to find transcription files and return them as
	 * a list of Transcript objects.
	 * @return List of Transcript objects.
	 */
	public static List<Transcript> readAll() {
		List<Transcript> list = new ArrayList<Transcript>();
		
		// Get a list of version directories
		File[] versionDirs = 
				FileIO.getAppRootPath().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.startsWith("v");
			}	
		});
		
		for(File f1 : versionDirs) {
			String versionName = f1.getName();
			File[] firstHashDirs = f1.listFiles();
			for(File f2 : firstHashDirs) {
				File[] secondHashDirs = f2.listFiles();
				for(File f3 : secondHashDirs) {
					File[] ownerIdDirs = f3.listFiles();
					for(File f : ownerIdDirs) {
						Log.i(TAG, "readAll: " + f.getPath());
						
						String ownerId = f.getName();
						addTranscriptsInDir(list, f, versionName, ownerId);
					}
				}
			}
		}
		return list;
	}

	private static void addTranscriptsInDir(List<Transcript> transcripts, 
			File ownerDir, String versionName, String ownerId) {
		// Constructs a list of directories in the recordings directory.
		File[] recordingPathFiles = Recording.getRecordingsPath(ownerDir).listFiles();
		
		if (recordingPathFiles == null) {
			return;
		}
			
		for (File dir : recordingPathFiles) {
			if (dir.isDirectory() && dir.getName().length() == 8) {
				// For each of those subdirectories, creates a list of files
				// within that end in -transcript.txt
				for (File f: dir.listFiles()) {
					String filename = f.getName();
					String[] a = filename.split("-");
					if (filename.endsWith(".txt") && a[2].equals("transcript")) {
						String trs_id = filename.split("\\.")[0];
						transcripts.add(
								new Transcript(versionName, ownerId, trs_id));
					}
				}
			}
		}
	}
	
	/**
	 * Get transcript as a File.
	 * 
	 * @param versionName	The version of the transcription/recording format(v0x)
	 * @param ownerId		The owner ID of the transcription
	 * @param id Transcript ID
	 * @return A File containing the transcript.
	 */
	public static File getFile(String versionName, String ownerId, String id) {
		try {
			Transcript trs = new Transcript(versionName, ownerId, id);
			return trs.getFile();
		}
		catch (RuntimeException e) {
			return null;
		}
	}
	
	/**
	 * Get transcript as a File.
	 * @return A File containing the transcript.
	 */
	public File getFile() {
		return new File(path);
	}
	
	/**
	 * Store the transcript to file system.
	 * @param text Transcription data.
	 * @throws IOException if there is an exception in the file I/O.
	 */
	public void save(String text) throws IOException {
		File f = new File(path);
		Charset charset = Charset.forName("UTF-8");
		BufferedWriter writer = Files.newWriter(f, charset);
		writer.write(text);
		writer.close();
	}
	
	/**
	 * Get transcript ID.
	 * @return Transcript ID.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Encode transcript metadata as a json object.
	 * @return JSONObject containing metadata for the transcript.
	 */
	public JSONObject encode() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("transcriber", person_id);
		json.put("recording", group_id);
		return json;
	}
}
