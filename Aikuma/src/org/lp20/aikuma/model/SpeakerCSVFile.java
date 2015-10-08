/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lp20.aikuma.util.AikumaSettings;

import au.com.bytecode.opencsv.CSVReader;

/**
 * The csv file of a list of speakers
 * (used for bulk-import of speakers)
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class SpeakerCSVFile {
	
	private static final String TAG = SpeakerCSVFile.class.getSimpleName();
	
	private File mPath;
	
	private int fieldNum;
	private Map<String, Integer> fieldOrder;
	private List<MetadataChunk> metadataList;
	private static Map<String, Speaker> speakerMap = new HashMap<String, Speaker>();
	
	/**
	 * Constructor of the SpeakerCSVFile
	 * 
	 * @param filepath		The folder-path where the csv-file is stored
	 * @param filename		The csv-file name
	 * @throws IOException	IOException
	 */
	public SpeakerCSVFile(File filepath, String filename) throws IOException {
		if(AikumaSettings.getCurrentUserId() == null)
			throw new IOException("Can't be created: UserID is necessary");
		
		mPath = filepath;
		metadataList = new ArrayList<MetadataChunk>();
		speakerMap.clear();

		CSVReader csvReader = new CSVReader(new FileReader(new File(filepath, filename)), ',');
		
		String[] lineBuffer;
		lineBuffer = csvReader.readNext();
		fieldNum = lineBuffer.length;
		fieldOrder = new HashMap<String, Integer>(fieldNum);
		
		boolean isHeaderValid = checkHeader(lineBuffer);
		if(!isHeaderValid) {
			csvReader.close();
			throw new IOException("Parse-error: Header is invalid");
		}
			
		while((lineBuffer = csvReader.readNext()) != null) {
			// If filename is not given, just skip the row.
			if(lineBuffer[fieldOrder.get(SPEAKER_NAME_KEY)].isEmpty())
				continue;
			
			try {
				metadataList.add(new MetadataChunk(lineBuffer, fieldOrder, METADATA_PROC_LIST));
			} catch(IOException e) {
				csvReader.close();
				throw new IOException(e.getMessage());
			}
		}
		csvReader.close();
	}
	
	private boolean checkHeader(String[] header) {
		if(fieldNum != 1) return false;
		
		for (int i = 0; i < fieldNum; i++) {
			String fieldname = header[i].toLowerCase();
			if(KEY_SET.contains(fieldname)) {
				fieldOrder.put(fieldname, i);
			}
		}
		if(!fieldOrder.keySet().containsAll(REQUIRED_KEY_SET))
			return false;
		return true;
	}
	
	public int getNumOfSources() {
		return metadataList.size();
	}
	
	/**@param	i	the order of the speaker
	 * @return the metadata of ith speaker in the list */
	public MetadataChunk getMetadata(int i) {
		return metadataList.get(i);
	}
	
	public List<MetadataChunk> getMetadataChunks() {
		return metadataList;
	}
	
	/** Interface to process each metadata field */
	private interface metadataProcessor<T> {
		/** Interface function to process each field of metadata in the csv file */
		public T processField(String metadataStr) throws IOException;
	}
	
	/**
	 * The csv file's metadata structure for each speaker
	 *
	 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
	 */
	public class MetadataChunk {
		private Speaker mSpeaker;
		
		/**
		 * Constructor of the speaker metadata structure
		 * @param speaker	The speaker
		 */
		public MetadataChunk(Speaker speaker) {
			mSpeaker = speaker;
		}
		
		/**
		 * Constructor of the speaker metadata structure in the csv file
		 * @param metadataFields	The fields of metadata
		 * @param fieldOrder		The order of each field
		 * @param fieldProcessor	Interface-function to process each field
		 * @throws IOException		IOException
		 */
		public MetadataChunk(String[] metadataFields, 
				Map<String, Integer> fieldOrder, Map<String, metadataProcessor> fieldProcessor) throws IOException {
			mSpeaker = (Speaker) fieldProcessor.get(SPEAKER_NAME_KEY).processField(
					metadataFields[fieldOrder.get(SPEAKER_NAME_KEY)]);
		}
		
		public Speaker getSpeaker() { return mSpeaker; }
	}
	
	private static final String SPEAKER_NAME_KEY = "speaker";
	
	private static final Set<String> REQUIRED_KEY_SET = new HashSet<String>();
	private static final Set<String> KEY_SET = new HashSet<String>();
	static {
		KEY_SET.add(SPEAKER_NAME_KEY);
		REQUIRED_KEY_SET.add(SPEAKER_NAME_KEY);
	}
	
	private static final HashMap<String, metadataProcessor> METADATA_PROC_LIST = 
			new HashMap<String, metadataProcessor>();
	static {
		metadataProcessor<String> defaultProcessor = new metadataProcessor<String>() {
			@Override
			public String processField(String metadataStr) {
				return metadataStr;
			}
		};
		METADATA_PROC_LIST.put(SPEAKER_NAME_KEY, new metadataProcessor<Speaker>() {
			@Override
			public Speaker processField(String metadataStr) {
				String spkName = metadataStr.trim();
				String spkNameKey = spkName.toLowerCase();
				if(speakerMap.containsKey(spkNameKey)) {
					return speakerMap.get(spkNameKey);
				} else {
					Speaker newSpeaker = new Speaker(spkName, "", new Date(), 
							AikumaSettings.getLatestVersion(), AikumaSettings.getCurrentUserId());
					speakerMap.put(spkNameKey, newSpeaker);
					return newSpeaker;
				}
			}
		});
	}
}
