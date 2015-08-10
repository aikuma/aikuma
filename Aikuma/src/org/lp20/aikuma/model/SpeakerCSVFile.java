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

public class SpeakerCSVFile {
	
	private static final String TAG = SpeakerCSVFile.class.getSimpleName();
	
	private File mPath;
	
	private int fieldNum;
	private Map<String, Integer> fieldOrder;
	private List<MetadataChunk> metadataList;
	private static Map<String, Speaker> speakerMap = new HashMap<String, Speaker>();
	
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
				metadataList.add(new MetadataChunk(lineBuffer, fieldOrder, metadataProcList));
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
			if(keySet.contains(fieldname)) {
				fieldOrder.put(fieldname, i);
			}
		}
		if(!fieldOrder.keySet().containsAll(requiredKeySet))
			return false;
		return true;
	}
	
	public int getNumOfSources() {
		return metadataList.size();
	}
	
	public MetadataChunk getMetadata(int i) {
		return metadataList.get(i);
	}
	
	public List<MetadataChunk> getMetadataChunks() {
		return metadataList;
	}
	
	private interface metadataProcessor<T> {
		public T processField(String metadataStr) throws IOException;
	}
	
	public class MetadataChunk {
		private Speaker mSpeaker;
		
		public MetadataChunk(Speaker speaker) {
			mSpeaker = speaker;
		}
		
		public MetadataChunk(String[] metadataFields, 
				Map<String, Integer> fieldOrder, Map<String, metadataProcessor> fieldProcessor) throws IOException {
			mSpeaker = (Speaker) fieldProcessor.get(SPEAKER_NAME_KEY).processField(
					metadataFields[fieldOrder.get(SPEAKER_NAME_KEY)]);
		}
		
		public Speaker getSpeaker() { return mSpeaker; }
	}
	
	private static final String SPEAKER_NAME_KEY = "speaker";
	
	private static final Set<String> requiredKeySet = new HashSet<String>();
	private static final Set<String> keySet = new HashSet<String>();
	static {
		keySet.add(SPEAKER_NAME_KEY);
		requiredKeySet.add(SPEAKER_NAME_KEY);
	}
	
	private static final HashMap<String, metadataProcessor> metadataProcList = 
			new HashMap<String, metadataProcessor>();
	static {
		metadataProcessor<String> defaultProcessor = new metadataProcessor<String>() {
			@Override
			public String processField(String metadataStr) {
				return metadataStr;
			}
		};
		metadataProcList.put(SPEAKER_NAME_KEY, new metadataProcessor<Speaker>() {
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
