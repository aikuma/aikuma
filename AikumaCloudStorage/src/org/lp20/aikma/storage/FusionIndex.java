package org.lp20.aikma.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FusionIndex interfaces with the Aikuma Fusion Tables and the Aikuma Web Server
 * 
 *   - to search items in the Aikuma index, and
 *   - to make collected data public by registering it with the Aikuma index.
 * 
 * @author haejoong
 *
 */
public class FusionIndex implements Index {
	
	/* (non-Javadoc)
	 * @see org.lp20.aikma.storage.Index#get_item_metadata(java.lang.String)
	 */
	@Override
	public Map<String,String> get_item_metadata(String aikuma_file_path) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.lp20.aikma.storage.Index#search(java.util.Map)
	 */
	@Override
	public List<String> search(Map<String,String> constraints) {
		List<String> res = new ArrayList<String>();
		return res;
	}
	
	/* (non-Javadoc)
	 * @see org.lp20.aikma.storage.Index#index(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	public void index(String aikuma_file_path, Map<String,String> metadata) {
		// Send the metadata to the server using the Aikuma Web API.
	}
}
