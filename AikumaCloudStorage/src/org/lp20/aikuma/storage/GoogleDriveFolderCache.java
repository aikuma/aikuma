package org.lp20.aikuma.storage;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The GoogleDriveFolderCache class keeps the directory paths and Google file
 * IDs for fast access without calling Google API.
 *
 * This is a singleton, so use getInstance() to get the instance.
 *
 * The cache can be built in 2 ways. One is to add 3-tuple of Google file ID,
 * directory name (title), and Google file ID of the parent directory. Before
 * starting adding tuples, beginTable() should be called, and finishTable()
 * should be called after all tuples have been added. This approach requires
 * a little care regarding thread synchronization. Because beginTable()
 * creates a lock, finishTable() must be called to unlock.
 *
 * Another way of building the cache is to explicitly add path and Google file
 * ID pairs. Use add() to add a pair. Use getFid() and getPath() to get Google
 * File ID and directory path respectively.
 */
class GoogleDriveFolderCache {
	final Map<String,String> mFids = new HashMap<String,String>();
	final Map<String,String> mPaths = new HashMap<String,String>();
	Map<String,String> mNames;
	Map<String,Set<String>> mPids0;
	Map<String,String> mPids;
	private final ReentrantLock lock = new ReentrantLock();
	private static GoogleDriveFolderCache mObj = null;

	private GoogleDriveFolderCache() {
	}

	/**
	 * Get the singletone instance of the class.
	 */
	public static synchronized GoogleDriveFolderCache getInstance() {
		if (mObj == null)
			mObj = new GoogleDriveFolderCache();
		return mObj;
	}

	/**
	 * This exception signals a problem in building the cache.
	 */
	public class Error extends Exception {
		public Error(String message) {
			super(message);
		}
	}

	private String computePath(String id) throws Error {
		if (mPaths.containsKey(id)) {
			return mPaths.get(id);
		}
		
		String path;
		if (mPids.containsKey(id)) {
			String prefix = computePath(mPids.get(id));
                        String joiner = prefix.equals("/") ? "" : "/";
                        path = prefix + joiner + mNames.get(id);
		} else {
			path = "/";
		}

		mPaths.put(id, path);
		if (mFids.containsKey(path)) {
			String msg = String.format("folder maps to multiple paths: %s (%s)", path, id);
			throw new Error(msg);
		}
		mFids.put(path, id);
		return path;
	}

	/**
	 * Start building the cache.
	 *
	 * This is one way of building the cache. After this, use addToTable() to
	 * add materials to build the cache, and then call finishTable() to finalize
	 * building the cache.
	 */
	public void beginTable() {
		lock.lock();
		mNames = new HashMap<String,String>();
		mPids0 = new HashMap<String,Set<String>>();
		mPids = new HashMap<String,String>();
	}

	/**
	 * Add information for a directory which is later used to build the cache.
	 *
	 * @param id Google file ID of the directory.
	 * @param title Name of the directory.
	 * @param pid Google file ID of the parent directory.
	 */
	public void addToTable(String id, String title, String pid) {
		mNames.put(id, title);
		if (!mPids0.containsKey(id))
			mPids0.put(id, new HashSet<String>());
		mPids0.get(id).add(pid);
	}

	/**
	 * Build the cache using the information added by addToTable().
	 */
	public void finishTable() throws Error {
		for (String id: mPids0.keySet()) {
			for (String pid: mPids0.get(id)) {
				if (mNames.containsKey(pid)) {
					mPids.put(id, pid);
				}
			}
		}
		try {
			for (String id: mNames.keySet())
				computePath(id);
		} catch (Error err) {
			throw err;
		} finally {
			mPids = null;
			mPids0 = null;
			mNames = null;
			lock.unlock();
		}
	}

	/**
	 * Empty the cache.
	 */
	public synchronized void clear() {
		mPaths.clear();
		mFids.clear();
	}

	/**
	 * Add Google file ID and path pair.
	 *
	 * This is the second way of building the cache. The Google file ID and
	 * directory path are explicitly added.
	 *
	 * @param id Google file ID.
	 * @param path Path of the directory.
	 */
	public synchronized void add(String id, String path) {
		mPaths.put(id, path);
		mFids.put(path, id);
	}

	/**
	 * Get directory path corresponding to the given Google file ID.
	 *
	 * @param id Google file ID.
	 * @return Directory path.
	 */
	public synchronized String getPath(String id) {
		return mPaths.get(id);
	}

	/**
	 * Get Google file ID corresponding to the give directory path.
	 *
	 * @param path Directory path.
	 * @return Google file ID.
	 */
	public synchronized String getFid(String path) {
		return mFids.get(path);
	}

	/**
	 * Get list of all directory paths.
	 *
	 * @return List of directory paths.
	 */
	public synchronized List<String> listPaths() {
		return new ArrayList<String>(mFids.keySet());
	}
}
