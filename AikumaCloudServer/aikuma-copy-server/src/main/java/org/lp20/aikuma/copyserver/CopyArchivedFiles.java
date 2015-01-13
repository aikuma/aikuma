package org.lp20.aikuma.copyserver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.lp20.aikuma.storage.*;

/**
 * For each file in fusion table that meets the following conditions,
 * copy it over to the central google drive and update date_processed field.
 * 
 * - File is uploaded to personal google drive.
 * - File is approved for archive.
 * - File hasn't been copied to central google drive.
 * 
 * Names of copied files are kept in a local database to avoid duplicated
 * copies.
 * 
 * @author haejoong
 */
public class CopyArchivedFiles {
	static final Logger LOG = Logger.getLogger(CopyArchivedFiles.class.getName());
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: CopyArchivedFiles <credential file> <process log>");
			System.exit(1);
		}

		String credentialFile = args[0];
		String processLogFile = args[1];
		
		GoogleCredentialManager cm = new GoogleCredentialManager(credentialFile);
		ProcessLogManager pm = new ProcessLogManager(processLogFile);
		
		String accessToken = cm.getAccessToken();
		DataStore ds = new GoogleDriveStorage(accessToken);
		FusionIndex fi = new FusionIndex(accessToken);
		
		HashMap<String,String> constraints = new HashMap<String,String>();
		
		List<String> items;
		
		try {
			items = fi.search(constraints);
		} catch (InvalidAccessTokenException e) {
			LOG.warning("access token expired");
			System.exit(1);
			throw e;
		}
		
		for (String item_id: items) {
			Map<String,String> meta = fi.getItemMetadata(item_id);
			String url = meta.get("data_store_uri");
			String date_approved = meta.get("date_approved");
			String date_backedup = meta.get("date_backedup");
			
			if (url == null || url.isEmpty() || url.equals("NA"))
				continue;
			if (date_approved == null || date_approved.isEmpty())
				continue;
			if (date_backedup == null || date_backedup.isEmpty())
				continue;
			if (pm.isCopied(item_id) && pm.isDated(item_id))
				continue;
			
			if (!pm.isCopied(item_id)) {
				Data data;
				try {
					data = downloadUrl(item_id, url);
				} catch (Exception e1) {
					LOG.warning(item_id + ": failed to download: " + e1.getMessage());
					data = null;
				}

				if (data == null) continue;
				
				String uri = ds.store(item_id, data);
				if (uri == null) {
					LOG.warning(item_id + ": failed to copy to central location");
					continue;
				}
				
				if (!ds.share(item_id)) {
					LOG.warning(item_id + ": failed to share");
					continue;
				}
				
				if (!pm.setUri(item_id, uri)) {
					LOG.warning(item_id + ": failed to update local db");
					continue;
				}
				
				LOG.info(item_id + ": copied");
			}
				
			if (!pm.isDated(item_id)) {
				Map<String,String> m = new HashMap<String,String>();
				m.put("central_data_store_uri", pm.getUri(item_id));
				String date = LocalDateTime.now(ZoneId.of("UTC")).format(
						DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
				m.put("date_processed", date);
				if (!fi.update(item_id, m)) {
					LOG.warning(item_id + ": failed to update date_processed");
					continue;
				}
				if (!pm.setDated(item_id)) {
					LOG.warning(item_id + ": failed to update local db(2)");
				}
				
				LOG.info(item_id + ": processed date updated");
			}
		}
	}
	
	static String getMimeType(HttpURLConnection con) {
		String f;
		int i = 0;
		while ((f = con.getHeaderField(i)) != null) {
			String k = con.getHeaderFieldKey(i++);
			if (k != null && k.equals("Content-Type")) {
				return f.split(";")[0];
			}
		}
		return null;
	}
	
	static Data downloadUrl(String item_id, String url) throws MalformedURLException, IOException {
		LOG.info(item_id + ": download requested: " + url);
		HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
		con.setInstanceFollowRedirects(true);
		con.setDoOutput(false);
		con.setRequestMethod("GET");
		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
			LOG.warning(item_id + ": download failed: " + url);
			return null;
		}
		String mimeType = getMimeType(con);
		if (!mimeType.split("/")[0].equals("audio")) {
			LOG.warning(item_id + ": wrong mime type: " + mimeType);
			return null;
		}
		return new Data(con.getInputStream(), mimeType);
	}
}
