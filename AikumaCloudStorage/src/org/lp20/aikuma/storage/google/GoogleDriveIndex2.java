package org.lp20.aikuma.storage.google;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Map;

import org.lp20.aikuma.net.Http;
import org.lp20.aikuma.storage.Utils;
import org.lp20.aikuma.storage.DataStore;

public class GoogleDriveIndex2 extends GoogleDriveIndex {

	private String baseUrl;
	private String idToken;
	
	public GoogleDriveIndex2(String rootTitle, TokenManager tm, String baseUrl, String idToken) 
			throws DataStore.StorageException {
		super(rootTitle, tm);
		this.baseUrl = baseUrl;
		this.idToken = idToken;
	}
	
	public boolean index(String identifier, Map<String, String> metadata) {
		return call("POST", identifier, metadata);
	}
	
	public boolean update(String identifier, Map<String, String> metadata) {
		return call("PUT", identifier, metadata);
	}
	
	private boolean call(String method, String identifier, Map<String, String> metadata) {
		if(identifier == null || metadata == null)
			return false;
		
		String url = String.format("%s/%s", baseUrl, identifier);
		Http http = null;
		try {
			http = Http.apply(url)
						  .method(method)
						  .header("X-Aikuma-Auth-Token", idToken)
						  .header("Content-Type", "application/x-www-form-urlencoded")
						  .body(Utils.map2query(metadata));
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(http != null && http.code() == 202)
			return true;
		else
			return false;
	}
}