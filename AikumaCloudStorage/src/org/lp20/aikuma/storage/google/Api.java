package org.lp20.aikuma.storage.google;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.net.Http;
import org.lp20.aikuma.storage.Data;
import org.lp20.aikuma.storage.Utils;

public class Api {
	
	private Logger log;
	private GHttpFactory ghttp;
	
	public Api(TokenManager tm) {
		log = Logger.getLogger(this.getClass().getName());
		ghttp = new GHttpFactory(tm);
	}
	
	public JSONObject copyFile(String fileId, JSONObject meta) {
		try {
			Http http = ghttp.apply("https://www.googleapis.com/drive/v2/files/"+fileId+"/copy")
					.method("POST")
					.header("Content-Type", "application/json")
					.body(meta.toString());
			
			switch(http.code()) {
			case HttpURLConnection.HTTP_OK:
				return (JSONObject) JSONValue.parse(http.read());
			default:
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean deleteFile(String fileId) 
			throws ProtocolException, MalformedURLException {
		String url = "https://www.googleapis.com/drive/v2/files/"+fileId;
		return ghttp.apply(url).method("DELETE").code() == HttpURLConnection.HTTP_OK;
	}
	
	public boolean deletePerm(String fileId, String permissionId) 
			throws ProtocolException, MalformedURLException {
		String url = "https://www.googleapis.com/drive/v2/files/"+fileId+"/permissions/"+permissionId;
		return ghttp.apply(url).method("DELETE").code() == 204;
	}
	
	public InputStream download(String url) {
		try {
			Http http = ghttp.apply(url).method("GET");
			switch(http.code()) {
			case HttpURLConnection.HTTP_OK:
				return http.inputStream();
			default:
				return null;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean emptyTrash() 
			throws ProtocolException, MalformedURLException {
		String url = "https://www.googleapis.com/drive/v2/files/trash";
		return ghttp.apply(url).method("DELETE").code() == 204;
	}
	
	public boolean exist(String q) { return search(q).hasMoreElements(); }

	public JSONObject getInfo(String fileId) {
		String url = "https://www.googleapis.com/drive/v2/files/"+fileId;
		try {
			Http http = ghttp.apply(url).method("GET");
			
			switch(http.code()) {
			case HttpURLConnection.HTTP_OK:
				return (JSONObject) JSONValue.parse(http.read());
			default:
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public JSONObject insertFile(Data data, JSONObject meta) {
		return updateFile("", data, meta);
	}
	
	public JSONObject list(String query, String pageToken) {
		URL url = null;
		
		try {
			Utils.UrlBuilder ub = new Utils.UrlBuilder("https://www.googleapis.com/drive/v2/files/");
			if(pageToken != null && !pageToken.isEmpty())
				ub.addQuery("pageToken", pageToken);
			else if(query != null && !query.isEmpty())
				ub.addQuery("q", query);
			
			url = ub.toUrl();
			
			if(url == null) return null;
			
			Http http = ghttp.apply(url).method("GET");
			switch(http.code()) {
			case HttpURLConnection.HTTP_OK:
				JSONObject obj = (JSONObject) JSONValue.parse(http.read());
				String kind = (String) obj.get("kind");
				if(kind.equals("drive#fileList")) {
					return obj;
				} else {
					log.fine("expected driv#fileList but received: " + obj.get("kind"));
					return null;
				}
			default:
				return null;
			}
		} catch (Exception e) {
			log.fine("exception: " + e.getMessage());
			return null;
		}
	}
	
	public JSONObject makeFile(JSONObject meta) {
		try {
			Http http = ghttp.apply("https://www.googleapis.com/drive/v2/files")
					.method("POST")
					.header("Content-Type", "application/json")
					.body(meta.toString());
			
			switch(http.code()) {
			case HttpURLConnection.HTTP_OK:
				return (JSONObject) JSONValue.parse(http.read());
			default:
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String permId() {
		try {
			Http http = ghttp.apply("https://www.googleapis.com/drive/v2/about?fields=permissionId")
					.method("GET");
			
			switch(http.code()) {
			case HttpURLConnection.HTTP_OK:
				JSONObject obj = (JSONObject) JSONValue.parse(http.read()); 
				return (String) obj.get("permissionId");
			default:
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Search search(String query) {
		return new Search(query, "drive#fileList") {
			protected JSONObject getMore(String query, String pageToken) {
				//log.fine("pageToken: " + pageToken + " query: " + query);
				return list(query, pageToken);
			}
		};
	}
	
	public JSONObject shareWith(String fileId, String email) {
		JSONObject meta = new JSONObject();
		meta.put("type", "user");
	    meta.put("value", email);
	    meta.put("role", "reader");
	    
		try {
			Http http = ghttp.apply("https://www.googleapis.com/drive/v2/files/"+fileId+"/permissions")
					.method("POST")
					.header("Content-Type", "application/json")
					.body(meta.toString());
			
			switch(http.code()) {
		    case HttpURLConnection.HTTP_OK:
		    	return (JSONObject) JSONValue.parse(http.read());
		    default:
		    	return null;
		    }
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public JSONObject trashFile(String fileId) {
		String url = "https://www.googleapis.com/drive/v2/files/"+fileId+"/trash";
		try {
			Http http = ghttp.apply(url).method("POST");
			
			switch(http.code()) {
			case HttpURLConnection.HTTP_OK:
				return (JSONObject) JSONValue.parse(http.read());
			default:
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public JSONObject updateFile(String fileId, Data data, JSONObject meta) {
		String bd = "bdbdbdbdbdbdbdbdbdbd";
		String fid = "";
		String method = "POST";
		if(fileId != "") {
			fid = fileId;
			method = "PUT";
		}
		
		try {
			Http http = ghttp.apply("https://www.googleapis.com/upload/drive/v2/files"+fid+"?uploadType=multipart")
					.method(method)
					.header("Content-Type", "multipart/related; boundary=" + bd)
					.chunked(8192)
					.body("--"+bd+"\r\n")
					.body("Content-Type: application/json\r\n")
					.body("\r\n")
					.body(meta.toString())
					.body("\r\n")
					.body("--"+bd+"\r\n")
					.body("Content-Type: " + data.getMimeType() + "\r\n")
					.body("\r\n")
					.body(data.getInputStream())
					.body("\r\n")
					.body("--"+bd+"--\r\n");
			
			switch(http.code()) {
			case HttpURLConnection.HTTP_OK:
				return (JSONObject) JSONValue.parse(http.read());
			default:
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	public JSONObject updateMetadata(String fileId, JSONObject meta) {
		try {
			Http http = ghttp.apply("https://www.googleapis.com/drive/v2/files/" + fileId)
					.method("PUT")
					.header("Content-Type", "application/json")
					.body(meta.toString());
			
			switch(http.code()) {
			case HttpURLConnection.HTTP_OK:
				return (JSONObject) JSONValue.parse(http.read());
			default:
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}