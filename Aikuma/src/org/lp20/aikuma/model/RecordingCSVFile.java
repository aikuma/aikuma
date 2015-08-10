package org.lp20.aikuma.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.util.AikumaSettings;

import au.com.bytecode.opencsv.CSVReader;

public class RecordingCSVFile {
	
	private static final String TAG = RecordingCSVFile.class.getSimpleName();
	
	private File mPath;
	
	private int fieldNum;
	private Map<String, Integer> fieldOrder;
	private List<MetadataChunk> metadataList;
	private List<String> filenameList;
	private static Map<String, Speaker> speakerMap = new HashMap<String, Speaker>();
	
	public RecordingCSVFile(File filepath, String filename) throws IOException {
		if(AikumaSettings.getCurrentUserId() == null)
			throw new IOException("Can't be created: UserID is necessary");
		
		mPath = filepath;
		metadataList = new ArrayList<MetadataChunk>();
		filenameList = new ArrayList<String>();
		speakerMap.clear();
		List<Speaker> deviceSpeakers = Speaker.readAll(AikumaSettings.getCurrentUserId());
		for(Speaker speaker : deviceSpeakers) {
			speakerMap.put(speaker.getName().toLowerCase(), speaker);
		}
		
		
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
			if(lineBuffer[fieldOrder.get(FILENAME_KEY)].isEmpty())
				continue;
			
			boolean isFileExist = new File(mPath, 
					lineBuffer[fieldOrder.get(FILENAME_KEY)] + "." + FileModel.AUDIO_EXT).exists();
			if(fieldOrder.containsKey(IMAGE_KEY)) {
				isFileExist &= new File(mPath, 
						lineBuffer[fieldOrder.get(IMAGE_KEY)] + "." + FileModel.IMAGE_EXT).exists();
			}
			if(!isFileExist) {
				csvReader.close();
				throw new IOException("Open-error: " + lineBuffer[fieldOrder.get(FILENAME_KEY)]  + " doesn't exist");
			}
			try {
				metadataList.add(new MetadataChunk(lineBuffer, fieldOrder, metadataProcList));		
			} catch (IOException e) {
				csvReader.close();
				throw new IOException(e.getMessage());
			}
		}
		csvReader.close();
	}
	
	private boolean checkHeader(String[] header) {
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
		private String mFileName;
		private String mImageName;
		private String mTitle;
		private String mComments;
		private Date mDate;
		private List<Language> mLanguages;
		private List<Speaker> mSpeakers;
		
		public MetadataChunk(String filename, String imagename, String title, String comments, 
				Date date, List<Language> languages, List<Speaker> speakers) {
			mFileName = filename;
			mImageName = imagename;
			mTitle = title;
			mComments = comments;
			mDate = date;
			mLanguages = languages;
			mSpeakers = speakers;
		}
		
		public MetadataChunk(String[] metadataFields, 
				Map<String, Integer> fieldOrder, Map<String, metadataProcessor> fieldProcessor) throws IOException {
			mFileName = (String) fieldProcessor.get(FILENAME_KEY).processField(
					metadataFields[fieldOrder.get(FILENAME_KEY)]) + "." + FileModel.AUDIO_EXT;
			if(filenameList.contains(mFileName))
				throw new IOException("Parse-error: There are duplicate filenames (" + mFileName + ")");
			filenameList.add(mFileName);
			
			if(fieldOrder.containsKey(IMAGE_KEY)) {
				mImageName = (String) fieldProcessor.get(IMAGE_KEY).processField(
						metadataFields[fieldOrder.get(IMAGE_KEY)]) + "." + FileModel.IMAGE_EXT;
			}
			mTitle = (String) fieldProcessor.get(TITLE_KEY).processField(
					metadataFields[fieldOrder.get(TITLE_KEY)]);
			mComments = (String) fieldProcessor.get(COMMENTS_KEY).processField(
					metadataFields[fieldOrder.get(COMMENTS_KEY)]);
			if(fieldOrder.containsKey(DESCRIPTION_KEY)) {
				String description = (String) fieldProcessor.get(DESCRIPTION_KEY).processField(
						metadataFields[fieldOrder.get(DESCRIPTION_KEY)]);
				if(!mComments.isEmpty())
					mComments = "\n" + mComments;
				mComments = description + mComments; 
			}
			mDate = (Date) fieldProcessor.get(DATE_KEY).processField(
					metadataFields[fieldOrder.get(DATE_KEY)]);
			mLanguages = (List<Language>) fieldProcessor.get(LANGUAGES_KEY).processField(
					metadataFields[fieldOrder.get(LANGUAGES_KEY)]);
			mSpeakers = (List<Speaker>) fieldProcessor.get(SPEAKERS_KEY).processField(
					metadataFields[fieldOrder.get(SPEAKERS_KEY)]);
		}
		
		public String getFileName() { return mFileName; }
		public String getImageName() { return mImageName; }
		public String getTitle() { return mTitle; }
		public String getComments() { return mComments; }
		public Date getDate() { return mDate; }
		public List<Language> getLanguages() { return mLanguages; }
		public List<Speaker> getSpeakers() { return mSpeakers; }
	}
	
	private static final String FILENAME_KEY = "filename";
	private static final String IMAGE_KEY = "imagename";
	private static final String TITLE_KEY = "title";
	private static final String DATE_KEY = "date";
	private static final String SPEAKERS_KEY = "speakers";
	private static final String LANGUAGES_KEY = "languages";
	private static final String DESCRIPTION_KEY = "description";
	private static final String COMMENTS_KEY = "comments";
	
	private static final Set<String> requiredKeySet = new HashSet<String>();
	private static final Set<String> keySet = new HashSet<String>();
	static {
		keySet.add(FILENAME_KEY);
		keySet.add(IMAGE_KEY);
		keySet.add(TITLE_KEY);
		keySet.add(DATE_KEY);
		keySet.add(SPEAKERS_KEY);
		keySet.add(LANGUAGES_KEY);
		keySet.add(DESCRIPTION_KEY);
		keySet.add(COMMENTS_KEY);
		
		requiredKeySet.add(FILENAME_KEY);
		requiredKeySet.add(TITLE_KEY);
		requiredKeySet.add(DATE_KEY);
		requiredKeySet.add(SPEAKERS_KEY);
		requiredKeySet.add(LANGUAGES_KEY);
		requiredKeySet.add(COMMENTS_KEY);
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
		metadataProcList.put(FILENAME_KEY, defaultProcessor);
		metadataProcList.put(IMAGE_KEY, defaultProcessor);
		metadataProcList.put(TITLE_KEY, defaultProcessor);
		metadataProcList.put(DATE_KEY, new metadataProcessor<Date>() {
			private SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
			@Override
			public Date processField(String dateStr) throws IOException {
				try {
					return dateParser.parse(dateStr);
				} catch (ParseException e) {
					throw new IOException("Parse-error: date is not correctly formatted (" + dateStr + ")");
				}
			}
		});
		metadataProcList.put(SPEAKERS_KEY, new metadataProcessor<List<Speaker>>() {
			@Override
			public List<Speaker> processField(String metadataStr) throws IOException {
				List<Speaker> speakerList = new ArrayList<Speaker>();
				metadataStr = metadataStr.trim();
				String[] speakerNames = metadataStr.split("\\s*,\\s*");
				for(String spkName : speakerNames) {
					String spkNameKey = spkName.toLowerCase();
					if(speakerMap.containsKey(spkNameKey)) {
						speakerList.add(speakerMap.get(spkNameKey));
					} else {
						throw new IOException("Parse-error: Speaker doesn't exist (" + spkName + ")");
					}
				}
				return speakerList;
			}
		});
		metadataProcList.put(LANGUAGES_KEY, new metadataProcessor<List<Language>>() {
			@Override
			public List<Language> processField(String metadataStr) throws IOException {
				List<Language> langList = new ArrayList<Language>();
				Map<String, String> langCodeMap = Aikuma.getLanguageCodeMap();
				metadataStr = metadataStr.toLowerCase();
				String[] langCodes = metadataStr.split("\\s*,\\s*");
				for(String langCode : langCodes) {
					String langName = langCodeMap.get(langCode);
					
					if(langName == null) {
						throw new IOException("Parse-error: ISO639 code is not correct (" + langCode + ")");
					} else {
						// ??? Check duplicates ???
						langList.add(new Language(langName, langCode));
					}
				}
				return langList;
			}
		});
		metadataProcList.put(DESCRIPTION_KEY, defaultProcessor);
		metadataProcList.put(COMMENTS_KEY, defaultProcessor);
	}
}
