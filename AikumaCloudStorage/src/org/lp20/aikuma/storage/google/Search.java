package org.lp20.aikuma.storage.google;

import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class Search {
	
	private Logger log;
	
	private String mNextPageToken;
	private int mNumItems;
	private int mIdx;
	private JSONArray mArr;
	private boolean mErr;
	
	private String mQuery;
	private String mKind;
	
	public Search(String query, String kind) {
		this.mQuery = query;
		this.mKind = kind;
		
		this.log = Logger.getLogger("Search");
		this.mNextPageToken = null;
		this.mNumItems = 0;
		this.mIdx = 0;
		this.mArr = null;
		this.mErr = false;
		
		processListObj(getMore(query, mNextPageToken));
	}


	public int nextIdx() {
		int x = mIdx;
		mIdx += 1;
		return x;
	}
	
	protected abstract JSONObject getMore(String query, String pageToken);
	
	public boolean hasMoreElements() {
		return mErr || mNextPageToken != null || mNumItems > mIdx;
	}
	
	public JSONObject nextElement() throws NoSuchElementException, Error {
		if(mErr)
			throw new Error("if error occurred while processing query result");
		
		if(mNumItems <= mIdx && mNextPageToken != null)
			processListObj(getMore(mQuery, mNextPageToken));
		
		if(mNumItems > mIdx)
			return (JSONObject) mArr.get(nextIdx());
		else
			throw new NoSuchElementException("if there's no more item to return");
	}
	
	public void processListObj(JSONObject obj) {
		if(obj == null) {
			log.fine("received null");
			mNextPageToken = null;
			mNumItems = 0;
			mIdx = 0;
			mErr = true;
		} else if(!mKind.equals((String) obj.get("kind"))) {
		      log.log(Level.FINE, "wrong kind received: %s", obj.get("kind"));
		      mErr = true;
		} else {
			mArr = (JSONArray) obj.get("items");
			mIdx = 0;
			mNumItems = mArr.size();
			mNextPageToken = (String) obj.get("nextPageToken");
			log.fine("items: $mNumItems next token: $mNextPageToken");
		}
	}
	
	
	public class Error extends Exception {
		public Error(String msg) { super(msg); }
	}
}