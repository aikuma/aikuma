package org.lp20.aikuma.storage.google;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.lp20.aikuma.storage.Index;
import org.lp20.aikuma.storage.DataStore;


public class GoogleDriveIndex implements Index {
	
	private Logger log;
	private Api api;
	private GHttpFactory ghttp;
	private String rootTitle;
	private String rootId;			// Used for index/update by central-server
	private String sharedRootId;	// Used for search/getMetadata by each user
	
	public GoogleDriveIndex(String rootTitle, TokenManager tm) 
			throws DataStore.StorageException {
		log = Logger.getLogger(this.getClass().getName());
		api = new Api(tm);
		ghttp = new GHttpFactory(tm);
		this.rootTitle = rootTitle;
		rootId = findAikumaRoot(false);
		sharedRootId = findAikumaRoot(true);	// Central-root folder shared with me
		if(rootId == null)
			throw new DataStore.StorageException();
	}
	
	private String findAikumaRoot(boolean isShared) {
		String query = null;
		if(isShared)
			query = String.format("title='%s' and sharedWithMe and trashed=false", rootTitle);
		else
			query = String.format("title='%s' and 'root' in parents and trashed=false", rootTitle);
		
		Search res = api.search(query);
		try {
			if(res.hasMoreElements()) {
				// TODO: It should throw an error when there are more than one root.
				JSONObject tmp = res.nextElement();
				if(tmp != null)
					return (String) tmp.get("id");
			}
		} catch(NoSuchElementException e) {
			log.severe(e.getMessage());
		} catch (Search.Error e) {
			log.severe(e.getMessage());
		}
		return null;
	}
	
	private String mergeTags(String tags1, String tags2) {
		Set<String> tmpSet = new HashSet<String>(
				Arrays.asList(String.format("%s %s", tags1, tags2).split("\\s+")));
		return joinStringSet(tmpSet, " ");
	}
	
	private String joinStringSet(Set<String> segments, String delimiter) {
		StringBuilder sb = new StringBuilder();
		String joiner = "";
		for(String seg : segments) {
			sb.append(joiner).append(seg);
			joiner = delimiter;
		}
		
		return sb.toString();
	}
	
	private String mapToTags(Map<String, String> map) {
		Set<String> tmpSet = new HashSet<String>();
		for(String key : map.keySet()) {
			String value = map.get(key);
			String tmp = key.replaceAll("\\s+", "_");
			if(!value.equals("")) {
				String[] values = value.split("\\|");
				String keyValuePair;
				for(String val : values) {	//multi-value variable
					keyValuePair = tmp + "__" + val.replaceAll("\\s+", "_");
					tmpSet.add(keyValuePair);
				}
				//tmp += "__" + value.replaceAll("\\s+", "_");
			} else {
				tmpSet.add(tmp);
			}
		}
		return joinStringSet(tmpSet, " ");
	}

	private Map<String, String> tagsToMap(String str) {
		Map<String, String> tagMap = new HashMap<String, String>();
		if(str == null)
			return tagMap;
		String[] tags = str.trim().split("\\s+");
		for(String tag : tags) {
			String[] pair = tag.split("__", 2);
			if(pair.length == 2) {
				String val = tagMap.get(pair[0]);
				if(val != null) {
					tagMap.put(pair[0], val + "|" + pair[1]);
				} else {
					tagMap.put(pair[0], pair[1]);
				}
			}
			else
				tagMap.put(pair[0], "");
		}
		return tagMap;
	}
	
	private String escape(String str) {
		return str.replaceAll("'", "\\'");
	}
	
	public Map<String, String> getItemMetadata(String identifier) {
		String query = String.format(
				"title='%s' and '%s' in parents and trashed=false", identifier, sharedRootId);
		Search res = api.search(query);
		try {
			if(res.hasMoreElements()) {
				JSONObject tmp = res.nextElement();
				if(tmp != null) {
					return tagsToMap((String) tmp.get("description"));
				}
			}
		} catch(NoSuchElementException e) {
			log.severe(e.getMessage());
		} catch (Search.Error e) {
			log.severe(e.getMessage());
		}
		
		return new HashMap<String, String>();
	}
	
	public List<String> search(Map<String, String> constraints) {
		List<String> tagList = new ArrayList<String>();
		if(sharedRootId == null)
			return tagList;
		
		String tags = escape(mapToTags(constraints));
		String query = String.format(
				"fullText contains '%s' and '%s' in parents and trashed=false", tags, sharedRootId);
		Search res = api.search(query);
		try {
			while(res.hasMoreElements()) {
				JSONObject tmp = res.nextElement();
				if(tmp != null)
					tagList.add((String) tmp.get("title"));
			}
		} catch(NoSuchElementException e) {
			log.severe(e.getMessage());
		} catch (Search.Error e) {
			log.severe(e.getMessage());
		}
		
		return tagList;
	}
	
	public void search(Map<String, String> constraints, 
			Index.SearchResultProcessor processor) {
		if(sharedRootId == null)
			return;
		
		String tags = mapToTags(constraints);
		String query = String.format(
				"fullText contains '%s' and '%s' in parents and trashed = false", escape(tags), sharedRootId);
		System.out.println(query);
		search(query, processor);
	}
	
	public void search(String query, Index.SearchResultProcessor processor) {
		String conj = "";
		if(!query.trim().equals(""))
			conj = "and";
		Search res = api.search(query);
		try {
			while(res.hasMoreElements()) {
				JSONObject tmp = res.nextElement();
				if(tmp != null) {
					Map<String, String> meta = tagsToMap((String) tmp.get("description"));
					meta.put("identifier", (String) tmp.get("title"));
					if(!processor.process(meta))
						break;
				}
			}
		} catch(NoSuchElementException e) {
			log.severe(e.getMessage());
		} catch (Search.Error e) {
			log.severe("query error");
		}
	}
	
	public boolean index(String identifier, Map<String, String> metadata) {
		String query = String.format(
				"title='%s' and '%s' in parents and trashed=false", identifier, rootId);
		Search res = api.search(query);
		try {
			if(res.hasMoreElements()) {
				JSONObject tmp = res.nextElement();
				if(tmp != null) {
					Map<String, String> metaMap = new HashMap<String, String>();
					metaMap.put("description", mapToTags(metadata));
					JSONObject meta = new JSONObject(metaMap);
					
					if(api.updateMetadata((String) tmp.get("id"), meta) != null)
						return true;
				}
			}
		} catch(NoSuchElementException e) {
			log.severe(e.getMessage());
		} catch (Search.Error e) {
			log.severe(e.getMessage());
		}
		
		
		return false;
	}
	
	public boolean update(String identifier, Map<String, String> metadata) {
		String query = String.format(
				"title='%s' and '%s' in parents and trashed=false", identifier, rootId);
		Search res = api.search(query);
		try {
			if(res.hasMoreElements()) {
				JSONObject tmp = res.nextElement();
				if(tmp != null) {
					String tags = mergeTags((String) tmp.get("description"), mapToTags(metadata));
					Map<String, String> metaMap = new HashMap<String, String>();
					metaMap.put("description", tags);
					JSONObject meta = new JSONObject(metaMap);
					
					if(api.updateMetadata((String) tmp.get("id"), meta) != null)
						return true;
				}
			}
		} catch(NoSuchElementException e) {
			log.severe(e.getMessage());
		} catch (Search.Error e) {
			log.severe(e.getMessage());
		}
		
		return false;
	}
}
/*
@throws[DataStore.StorageException]("if provided root folder it not found")
class GoogleDriveIndex(rootTitle: String, tm: TokenManager) extends Index {

  
  private implicit def functionToSearchResultProcessor(
		    f: JMap[String,String] => Boolean
		  ): Index.SearchResultProcessor = {
		    new Index.SearchResultProcessor {
		      override def process(result: JMap[String,String]): Boolean = f(result)
		    }
		  }

		  class SearchObj(search: Search) extends Iterator[JSONObject] {
		    def next = search.nextElement
		    def hasNext = search.hasMoreElements
		    def first = if (hasNext) Some(next) else None
		  }

		  private implicit def search2searchObj(search: Search) = new SearchObj(search)

}
*/