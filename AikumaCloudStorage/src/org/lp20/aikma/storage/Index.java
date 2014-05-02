package org.lp20.aikma.storage;

import java.util.List;
import java.util.Map;

public interface Index {

	/**
	 * Downloads metadata for the item.
	 * 
	 * @param identifier
	 * @return The metadata for the item as a key-value pairs. Null if item is
	 *   not found in the index.
	 */
	public abstract Map<String, String> get_item_metadata(String identifier);

	/**
	 * Search the index according to the specified constraints.
	 * TODO: Specify the structure of the constraints param.
	 * @param constraints
	 * @return A list of item ids.
	 */
	public abstract List<String> search(Map<String,String> constraints);

	/**
	 * Index an item. If item already exists, it gets updated.
	 * For metadata, the following keys are required:
	 * 
	 *   - data_store_uri
	 *   - file_type
	 *   - language
	 *   - spakers (comma-separated list)
	 * 
	 * @param identifier
	 * @param metadata
	 */
	public abstract void index(String identifier, Map<String,String> metadata);

}