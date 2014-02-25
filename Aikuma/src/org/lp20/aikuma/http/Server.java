package org.lp20.aikuma.http;
import org.apache.http.conn.util.InetAddressUtils;
import org.json.simple.JSONObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.util.ImageUtils;

import android.content.res.AssetManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
	private static Server server_ = null;  // singleton server object
	private static AssetManager am_;
	private Proc proc_;
	private String current_host_;  // hostname/ip of running instance
	private int current_port_;     // port of running instance

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
			@Override
			public Response run(IHTTPSession session) {
				return serveIndex(session.getUri());
			}
		}).add(new Proc() {
			// serve recordings by uuid
			@Override
			public Response run(IHTTPSession session) {
				return serveRecording(session.getUri());
			}
		}).add(new Proc() {
			// serve recordings by uuid
			@Override
			public Response run(IHTTPSession session) {
				return serveMapFile(session.getUri());
			}
		}).add(new Proc() {
			@Override
			public Response run(IHTTPSession session) {
				return serveShapeFile(session.getUri());
			}
		}).add(new Proc() {
			// serve recordings by uuid
			@Override
			public Response run(IHTTPSession session) {
				return serveSpeakerImage(session.getUri());
			}
		}).add(new Proc() {
			// serve recordings by uuid
			@Override
			public Response run(IHTTPSession session) {
				return serveSpeakerSmallImage(session.getUri());
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
		if (host_ == null || port_ <= 0 || port_ > 65535)
			return null;
		
		if (server_ == null)
			server_ = new Server(host_, port_);

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

	public String getHost() {
		return host_;
	}
	
	public int getPort() {
		return port_;
	}
	
	public String getActiveHost() {
		return isAlive() ? current_host_ : null;
	}
	
	public int getActivePort() {
		return isAlive() ? current_port_ : null;
	}
	
	@Override
	public void start() throws IOException {
		current_host_ = host_;
		current_port_ = port_;
		super.start();
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		return proc_.exec(session);
	}
	
	private Response serveIndex(String path) {
		if (path.equals("/")) {
			Response r = serveAsset("/transcriber.html");
			return r == null ? mkNotFoundResponse(path) : r;
		}
		
		String a[] = path.split("/");
		if (a.length == 2 && a[1].equals("index.json")) {
			JSONObject originals = new JSONObject();
			JSONObject commentaries = new JSONObject();
			JSONObject speakers = new JSONObject();
			for (Recording r: Recording.readAll()) {
				if (r.isOriginal())
					originals.put(r.getUUID().toString(), r.encode());
				else
					commentaries.put(r.getUUID().toString(), r.encode());
			}
			for (Speaker r: Speaker.readAll()) {
				speakers.put(r.getUUID().toString(), r.encode());
			}
			JSONObject index = new JSONObject();
			index.put("originals", originals);
			index.put("commentaries", commentaries);
			index.put("speakers", speakers);
			return new Response(Status.OK, "application/json", index.toString());
		}
		else {
			return null;
		}
	}
	
	private Response serveRecording(String path) {
		String[] a = path.split("/");
		if (a.length != 3 || !a[1].equals("recording"))
			return null;

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
	
	private Response serveMapFile(String path) {
		String[] a = path.split("/");
		if (a.length != 4 || !a[1].equals("recording") || !a[3].equals("mapfile"))
			return null;
		
		try {
			File mapfile = new File(Recording.getRecordingsPath(), a[2] + ".map");
			InputStream is = new FileInputStream(mapfile);
			return new Response(Status.OK, "text/plain", is);
		}
		catch (FileNotFoundException e) {
			return mkNotFoundResponse(path);
		}
	}
	
	private Response serveShapeFile(String path) {
		String[] a = path.split("/");
		if (a.length != 4 || !a[1].equals("recording") || !a[3].equals("shapefile"))
			return null;
		
		try {
			File mapfile = new File(Recording.getRecordingsPath(), a[2] + ".shape");
			InputStream is = new FileInputStream(mapfile);
			return new Response(Status.OK, "application/octet-stream", is);
		}
		catch (FileNotFoundException e) {
			return mkNotFoundResponse(path);
		}
	}
	
	private Response serveSpeakerImage(String path) {
		String[] a = path.split("/");
		if (a.length != 4 || !a[1].equals("speaker") || !a[3].equals("image"))
			return null;
		
		try {
			UUID uuid = UUID.fromString(a[2]);
			InputStream is = new FileInputStream(ImageUtils.getImageFile(uuid));
			return new Response(Status.OK, "image/jpeg", is);
		}
		catch (IOException e) {
			return mkNotFoundResponse(path);
		}
		catch (IllegalArgumentException e) {
			return mkNotFoundResponse(path);
		}
	}
	
	private Response serveSpeakerSmallImage(String path) {
		String[] a = path.split("/");
		if (a.length != 4 || !a[1].equals("speaker") || !a[3].equals("smallimage"))
			return null;
		
		try {
			UUID uuid = UUID.fromString(a[2]);
			InputStream is = new FileInputStream(ImageUtils.getSmallImageFile(uuid));
			return new Response(Status.OK, "image/jpeg", is);
		}
		catch (IOException e) {
			return mkNotFoundResponse(path);
		}
		catch (IllegalArgumentException e) {
			return mkNotFoundResponse(path);
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
