package org.lp20.aikuma.copyserver;
import java.io.IOException;
import java.util.logging.Logger;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

/**
 * Class for keeping tracking of processing states.
 * Manages 2 pieces of information for each file.
 * 
 *   - Whether the item has been copied to the central location.
 *   - Whether the item's process date has been updated.
 *   
 * @author haejoong
 */
public class ProcessLogManager {
	RecordManager rm_;
	Logger log = Logger.getLogger(ProcessLogManager.class.getName());
	
	public ProcessLogManager(String filename) {
		try {
			rm_ = RecordManagerFactory.createRecordManager(filename);
		} catch (IOException e) {
			throw new RuntimeException("failed to open db file: " + e.getMessage());
		}
	}
	
	/**
	 * Use to record that file has been copied to central location.
	 * 
	 * @param itemId
	 */
	public boolean setUri(String itemId, String uri) {
		try {
			long recid = rm_.getNamedObject(itemId);
			if (recid == 0) {
				ProcessLog pl = new ProcessLog();
				pl.setUri(uri);
				recid = rm_.insert(pl);
				rm_.setNamedObject(itemId, recid);
			}
			else {
				ProcessLog pl = (ProcessLog) rm_.fetch(recid);
				pl.setUri(uri);
				rm_.update(recid,  pl);
			}
			rm_.commit();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			log.info(e.getMessage());
			try {
				rm_.rollback();
			} catch (IOException e2) {
				log.info(e2.getMessage());
			}
			return false;
		}
	}
	
	/**
	 * Use to record that dateProcessed field has been updated.
	 * 
	 * @param itemId Item ID
	 */
	public boolean setDated(String itemId) {
		try {
			long recid = rm_.getNamedObject(itemId);
			if (recid == 0) {
				ProcessLog pl = new ProcessLog();
				pl.setDated();
				recid = rm_.insert(pl);
				rm_.setNamedObject(itemId, recid);
			}
			else {
				ProcessLog pl = (ProcessLog) rm_.fetch(recid);
				pl.setDated();
				rm_.update(recid,  pl);
			}
			rm_.commit();
			return true;
		} catch (IOException e) {
			try {
				rm_.rollback();
			} catch (IOException e2) {
				// ignore
			}
			return false;
		}		
	}
	
	/**
	 * Tells whether the file has been copied to the central location.
	 * @param itemId
	 * @return true on success, false otherwise
	 */
	public boolean isCopied(String itemId) {
		try {
			long recid = rm_.getNamedObject(itemId);
			if (recid == 0) {
				return false;
			}
			else {
				ProcessLog pl = (ProcessLog) rm_.fetch(recid);
				return pl.isCopied();
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to access the log: " + e.getMessage());
		}
	}
	
	/**
	 * Tells whether the file's copy date has been updated.
	 * @param itemId
	 * @return
	 */
	public boolean isDated(String itemId) {
		try {
			long recid = rm_.getNamedObject(itemId);
			if (recid == 0) {
				return false;
			}
			else {
				ProcessLog pl = (ProcessLog) rm_.fetch(recid);
				return pl.isDated();
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to acess the log: " + e.getMessage());
		}
	}
	
	/**
	 * Get the data store uri for the file (download url).
	 * @param itemId
	 * @return uri
	 */
	public String getUri(String itemId) {
		try {
			long recid = rm_.getNamedObject(itemId);
			if (recid == 0) {
				return null;
			}
			else {
				ProcessLog pl = (ProcessLog) rm_.fetch(recid);
				return pl.getUri();
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to acess the log: " + e.getMessage());
		}
	}
}
