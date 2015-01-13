package org.lp20.aikuma.copyserver;

/**
 * A prcess log recording whether a file has been copied, and
 * whether the date of copy has been recorded.
 * 
 * @author haejoong
 */
public class ProcessLog implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	String uri;
	boolean dated;
	
	/**
	 * Create a new log.
	 */
	public ProcessLog() {
		uri = null;
		dated = false;
	}
	
	/**
	 * Tells whether the file has been copied.
	 * @return
	 */
	public boolean isCopied() {
		return uri != null;
	}
	
	/**
	 * Tells whether the date of copy has been recorded.
	 * @return
	 */
	public boolean isDated() {
		return dated;
	}
	
	/**
	 * Get data store uri for the file (download url).
	 * @return uri
	 */
	public String getUri() {
		return uri;
	}
	
	/**
	 * Records that the file has been copied.
	 * @param v data store uri for the file (download url)
	 */
	public void setUri(String v) {
		uri = v;
	}
	
	/**
	 * Records that the date of copy has been recorded.
	 */
	public void setDated() {
		dated = true;
	}
}
