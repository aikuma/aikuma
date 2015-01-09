package org.lp20.aikuma.copyserver;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import org.mapdb.*;

/**
 * Class for keeping tracking of processing states.
 *
 * It uses a persistent database to manage 2 pieces of information
 * for each file.
 * 
 *   - Whether the item has been copied to the central location.
 *   - Whether the item's process date has been updated.
 *   
 * @author haejoong
 */
public class ProcessLogManager {
    DB db_;
    Map<String,ProcessLog> map_;
    static final String mapName_ = "log";
    Logger log = Logger.getLogger(ProcessLogManager.class.getName());
    
    public ProcessLogManager(String filename) {
        db_ = DBMaker.newFileDB(new File(filename)).closeOnJvmShutdown().make();
        map_ = db_.getHashMap("log");
    }
    
    /**
     * Use to record that file has been copied to central location.
     * 
     * @param itemId
     */
    public boolean setUri(String itemId, String uri) {
        ProcessLog pl = map_.get(itemId);
        if (pl == null)
            pl = new ProcessLog();
        pl.setUri(uri);
        map_.put(itemId, pl);
        db_.commit();
        return true;
    }
    
    /**
     * Use to record that dateProcessed field has been updated.
     * 
     * @param itemId Item ID
     */
    public boolean setDated(String itemId) {
        ProcessLog pl = map_.get(itemId);
        if (pl == null)
            pl = new ProcessLog();
        pl.setDated();
        map_.put(itemId, pl);
        db_.commit();
        return true;
    }
    
    /**
     * Tells whether the file has been copied to the central location.
     * @param itemId
     * @return true on success, false otherwise
     */
    public boolean isCopied(String itemId) {
        ProcessLog pl = map_.get(itemId);
        if (pl == null)
            return false;
        else
            return pl.isCopied();
    }
    
    /**
     * Tells whether the file's copy date has been updated.
     * @param itemId
     * @return
     */
    public boolean isDated(String itemId) {
        ProcessLog pl = map_.get(itemId);
        if (pl == null)
            return false;
        else
            return pl.isDated();
    }
    
    /**
     * Get the data store uri for the file (download url).
     * @param itemId
     * @return uri
     */
    public String getUri(String itemId) {
        ProcessLog pl = map_.get(itemId);
        if (pl == null)
            return null;
        else
            return pl.getUri();
    }
}
