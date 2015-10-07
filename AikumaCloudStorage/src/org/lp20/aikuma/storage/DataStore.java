package org.lp20.aikuma.storage;

import java.io.InputStream;
import java.util.Date;

public interface DataStore {
	/**
	 * Retrieve a data item from the storage.
	 * @param identifier Unique identifier for the data item to retrieve.
	 * @return An InputStream for the identified data.
	 */
	public InputStream load(String identifier);
	
	/**
	 * Store a data item to the storage.
	 * @param identifier Unique identifier for the data item to store.
	 * @param data An InputStream containing the data.
	 * @return A URI or part of the URI that can be used to download the data item.
	 */
	public String store(String identifier, Data data);
	
	/**
	 * Allow public access to the data item.
	 * @param identifier Identifier for a data item.
	 * @return true on success, false on failure. 
	 */
	public boolean share(String identifier);
	
	/**
	 * Copy a data item from central-storage to private-storage
	 * @param identifier Identifier for a data item.
	 * @return true on success, false on failure.
	 */
	public boolean copy(String identifier);
	
	/**
	 * Provide a callback method for handling list items.
	 * @author haejoong
	 */
	public interface ListItemHandler {
		/**
		 * A callback method handling identifiers in a list. It should return
		 * false if it wants to stop iterating items in the list.
		 * @param identifier Identifier for the item.
		 * @param datetime Modified time of the item.
		 * @return False if iteration should stop.
		 */
		public boolean processItem(String identifier, Date datetime);
	}
	
	/**
	 * List items in the storage and call processItem method of the handler.
	 * The identifier of the item and its modified time is passed to the
	 * processItem method. It should stop iteration if the handler method
	 * returns false.
	 * 
	 * @param handler
	 */
	public void list(ListItemHandler handler);

	public class StorageException extends Exception {};
}
