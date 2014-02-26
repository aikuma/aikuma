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
import org.lp20.aikuma.model.*;
import org.lp20.aikuma.util.IdUtils;
import com.google.common.io.Files;

public class Transcript {
	private String id;  // transcript file name
	private String group_id;
	private String person_id;
	private String path;
	
	/**
	 * The constructor used when first creating a Transcript. The transcript
	 * is not created in the file system until the save() method is called.
	 * 
	 * @param groupId The group ID of the original recording that is going
	 * 	to be transcribed by this Transcript object.
	 * @param transcriberId The person ID of the transcriber.
	 */
	public Transcript(String groupId, String transcriberId) throws RuntimeException {
		try {
			Speaker.read(transcriberId);
		}
		catch (IOException e) {
			throw new RuntimeException("No such person: " + transcriberId);
		}

		String path = Recording.getRecordingsPath() + "/" + groupId;
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
		path = Recording.getRecordingsPath() + "/" + group_id + "/" + id + ".txt";
		
		try {
			(new File(path)).createNewFile();
		}
		catch (IOException e) {
			throw new RuntimeException("Can't create transcript on file system.");
		}
	}
	
	/**
	 * The constructor used when reading an existing transcript.
	 * @param id Transcript file ID.
	 * @throws RuntimeException
	 */
	public Transcript(String id) throws RuntimeException {
		String[] a = id.split("-");
		this.id = id;
		group_id = a[0];
		person_id = a[1];
		path = Recording.getRecordingsPath() + "/" + group_id + "/" + id + ".txt";

		if (!a[2].equals("transcript"))
			throw new RuntimeException("Invalid transcript ID: " + id);
		
		File trs = new File(path);
		if (!trs.exists())
			throw new RuntimeException("No such file: " + path);
		
		try {
			Speaker.read(a[1]);
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
		
		for (File dir: Recording.getRecordingsPath().listFiles()) {
			if (dir.isDirectory() && dir.getName().length() == 8) {
				for (File f: dir.listFiles()) {
					String filename = f.getName();
					String[] a = filename.split("-");
					if (filename.endsWith(".txt") && a[2].equals("transcript")) {
						String trs_id = filename.split("\\.")[0];
						list.add(new Transcript(trs_id));
					}
				}
			}
		}
		
		return list;
	}
	
	/**
	 * Get transcript as a File.
	 * @param id Transcript ID
	 * @return A File containing the transcript.
	 */
	public static File getFile(String id) {
		try {
			Transcript trs = new Transcript(id);
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
