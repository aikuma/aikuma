package org.lp20.aikuma.storage;

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
    public abstract Map<String, String> getItemMetadata(String identifier);

    /**
     * Search the index according to the specified constraints.
     * TODO: Specify the structure of the constraints param.
     * @param constraints
     * @return A list of item ids.
     */
    public abstract List<String> search(Map<String,String> constraints);

    /**
     * Search the index according to the specified constraints and run results
     * through the provided result processor.
     *
     * @param constraints
     * @param processor SearchResultProcessor object to process the search resultis.
     */
    public abstract void search(Map<String, String> constraints, SearchResultProcessor processor);

    /**
     * Index an item. If item already exists, it gets updated.
     * For metadata, the following keys are required:
     * 
     *   - data_store_uri - the Google Drive URI
     *   - item_id        - A way of grouping multiple items under a single identifier
     *   - file_type      - Mime type
     *   - language       - language
     *   - speakers (comma-separated list) - speakers
     * 
     * @param identifier
     * @param metadata
     */
    public abstract boolean index(String identifier, Map<String,String> metadata);


    /**
     * Update metadata for an identifier
     * @param identifier entry identifier
     * @param metadata  entry metadata
     */
    public abstract boolean update(String identifier, Map<String,String> metadata);


    /**
     * Provides a method that processes search results one at a time.
     */
    public interface SearchResultProcessor {
        /**
         * Process search result.
         * @param result key-value pairs
         * @return true to process more results, or false to stop processing.
         */
        public boolean process(Map<String, String> result);
    }

}
