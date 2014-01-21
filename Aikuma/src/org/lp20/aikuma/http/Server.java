package org.lp20.aikuma.http;
import org.apache.http.conn.util.InetAddressUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.lp20.aikuma.http.NanoHTTPD;
import org.lp20.aikuma.http.NanoHTTPD.Response.Status;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;

import android.content.res.AssetManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.net.NetworkInterface;
import java.net.InetAddress;

public class Server extends NanoHTTPD {
	private static String host_;    // hostname
	private static int port_ = -1;  // port number
	private static Server server_;  // singleton server object
	private static AssetManager am_;
	private Proc proc_;

	/**
	 * Protected constructor used by a factory function.
	 * 
	 * @param host hostname
	 * @param port port number
	 */
	protected Server(String host, int port) {
		super(host, port);
		
		// Sets up a request processing chain.
		proc_ = (new Proc() {
			// serve recordings by uuid
			@Override
			public Response run(IHTTPSession session) {
				return serveRecording(session.getUri());
			}
		}).add(new Proc() {
			@Override
			public Response run(IHTTPSession session) {
				return serveIndex(session.getUri());
			}
		}).add(new Proc() {
			// serve static assets
			@Override
			public Response run(IHTTPSession session) {
				return serveAsset(session.getUri());
			}
		});
	}
	
	/**
	 * Factory function that returns a singleton Server object.
	 * Hostname and port must be set before this method is called.
	 * Use setHost() and setPort() to set hostname and port.
	 * 
	 * @return Server or null of server can't be created.
	 */
	public static Server getServer() {
		if (port_ < 0) {
			return null;
		}
		
		if (server_ == null) {
			server_ = new Server(host_, port_);
		}
		return server_;
	}
	
	/**
	 * Configure hostname.
	 * 
	 * @param host
	 */
	public static void setHost(String host) {
		host_ = host;
	}
	
	/**
	 * Configure port number.
	 * 
	 * @param port
	 */
	public static void setPort(int port) {
		port_ = port;
	}
	
	/**
	 * Set FileServer object.
	 * 
	 * @param fileServer
	 */
	public static void setAssetManager(AssetManager am) {
		am_ = am;
	}
	
	/**
	 * Destroy the Server singleton object.
	 */
	public static void destroyServer() {
		if (server_ != null) {
			server_.stop();
			server_ = null;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static Map<String,String> getIpAddresses() {
		Enumeration<NetworkInterface> ifs;
		try {
			ifs = NetworkInterface.getNetworkInterfaces();
		}
		catch (java.net.SocketException e) {
			return null;
		}
		Map<String,String> ips = new HashMap<String,String>();
		while (ifs.hasMoreElements()) {
			NetworkInterface ff = ifs.nextElement();
			Enumeration<InetAddress> addrs = ff.getInetAddresses();
			while (addrs.hasMoreElements()) {
				InetAddress addr = addrs.nextElement();
				if (!addr.isLoopbackAddress()) {
					String ip = addr.getHostAddress();
					if (InetAddressUtils.isIPv4Address(ip)) {
						ips.put(ff.getName(), ip);
					}
				}
			}
		}
		return ips;
	}

	@Override
	public void start() throws IOException {
		super.start();
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		return proc_.exec(session);
	}
	
	private Response serveIndex(String path) {
		if (path.startsWith("/index/")) {
			List<Recording> rs = Recording.readAll();
			Iterator<Recording> it = rs.iterator();
			JSONObject index = new JSONObject();
			while (it.hasNext()) {
				Recording r = it.next();
				if (r.isOriginal()) {
					JSONObject obj = new JSONObject();
					
					// add speaker information
					JSONObject speakers = new JSONObject();
					for (UUID spkr_uuid : r.getSpeakersUUIDs()) {
						try {
							speakers.put(spkr_uuid.toString(), Speaker.read(spkr_uuid).encode());
						}
						catch (IOException e) {
							continue;
						}
					}
					obj.put("speakers", speakers);
					
					// add respeakings
					JSONObject respeakings = new JSONObject();
					for (Recording rspk : r.getRespeakings()) {
						respeakings.put(rspk.getUUID().toString(), rspk.encode());
					}
					obj.put("respeakings",  respeakings);
					obj.put("original", r.encode());
					
					index.put(r.getUUID().toString(), obj);
				}
			}
			return new Response(Status.OK, "application/json", index.toString());
		}
		else {
			return null;
		}
	}
	
	private Response serveRecording(String path) {
		if (path.startsWith("/recording/")) {
			String[] a = path.split("/");
			if (a.length < 3) {
				return mkNotFoundResponse(path);
			}
			try {
				UUID uuid = UUID.fromString(a[2]);
				InputStream is = new FileInputStream(Recording.read(uuid).getFile());
				return new Response(Status.OK, "audio/wave", is);
			}
			catch (IOException e) {
				return mkNotFoundResponse(path);
			}
			catch (IllegalArgumentException e) {
				return mkNotFoundResponse(path);
			}			
		}
		else {
			return null;
		}
	}
	
	private Response serveAsset(String path) {
		try {
			InputStream is = am_.open(path.substring(1));
			String mimeType = guessMimeType(path);
			return new Response(Status.OK, mimeType, is);
		}
		catch (IOException e) {
			return mkNotFoundResponse(path);
		}		
	}
	
	private String guessMimeType(String path) {
		if (path.endsWith(".html")) {
			return "text/html";
		}
		else if (path.endsWith(".js")) {
			return "application/javascript";
		}
		else if (path.endsWith(".css")) {
			return "text/css";
		}
		else {
			return "application/octet-stream";
		}
	}

	private Response mkNotFoundResponse(String s) {
		String msg = "Not found: " + s;
		return new Response(Status.NOT_FOUND, MIME_PLAINTEXT, msg);
	}
}
