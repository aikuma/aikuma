package org.lp20.aikuma.http;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.json.simple.JSONObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Speaker;
import org.lp20.aikuma.model.Transcript;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.ImageUtils;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * A web server class designed for a web-based transcription tool
 * (see github.com/langtech/transcriber).
 * 
 * @author Haejoong Lee	<haejoong@ldc.upenn.edu>
 *
 */
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
				Map<String,String> headers = session.getHeaders();
				int offset = 0;
				int len = 0;
				String v = headers.get("range");
				if (v != null) {
					android.util.Log.d("aa", v);
					Pattern p = Pattern.compile("bytes=(\\d+)-(\\d*)");
					Matcher m = p.matcher(v);
					if (m.matches()) {
						offset = Integer.parseInt(m.group(1));
						if (!m.group(2).trim().equals("")) {
							int i = Integer.parseInt(m.group(2));
							len = i - offset + 1;
						}
					}
					android.util.Log.d("aa", ".." + offset + ".." + len);
				}
				return serveRecording(session.getUri(), offset, len);
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
			// create, save and load transcripts
			@Override
			public Response run(IHTTPSession session) {
				return serveTranscript(session);
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
	 * @param host Hostname.
	 */
	public static void setHost(String host) {
		host_ = host;
	}
	
	/**
	 * Configure port number.
	 * 
	 * @param port Port number.
	 */
	public static void setPort(int port) {
		port_ = port;
	}
	
	/**
	 * Set AssetManager object to be used for serving static assets, e.g.
	 * audio and image files.
	 * 
	 * @param am AssetManager for the app.
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
	 * Identify available IP addresses of active network interfaces.
	 * @return A hash of interface name and IP address.
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

	/**
	 * Returns the hostname set by setHost() method.
	 * @return a hostname (IP address).
	 */
	public String getHost() {
		return host_;
	}
	
	/**
	 * Returns the port number set by setPort() method.
	 * @return a port number.
	 */
	public int getPort() {
		return port_;
	}
	
	/**
	 * Returns the hostname of the active server instance.
	 * @return hostname, or null if the server is not running.
	 */
	public String getActiveHost() {
		return isAlive() ? current_host_ : null;
	}
	
	/**
	 * Returns the port number of the active server instance.
	 * @return port number, or null if the server is not running.
	 */
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
			JSONObject transcripts = new JSONObject();
			for (Recording r: Recording.readAll()) {
				if (r.isOriginal())
					originals.put(r.getId().toString(), r.encode());
				else
					commentaries.put(r.getId().toString(), r.encode());
			}
			for (Speaker r: Speaker.readAll()) {
				speakers.put(r.getId().toString(), r.encode());
			}
			for (Transcript t: Transcript.readAll()) {
				transcripts.put(t.getId().toString(), t.encode());
			}
			JSONObject index = new JSONObject();
			index.put("originals", originals);
			index.put("commentaries", commentaries);
			index.put("speakers", speakers);
			index.put("transcripts", transcripts);
			return new Response(Status.OK, "application/json", index.toString());
		}
		else {
			return null;
		}
	}
	
	private Response serveRecording(String path, int offset, int len) {
		// GET /recording/versionName/owner_id/filename
		
		String[] a = path.split("/");
		if (a.length != 5 || !a[1].equals("recording"))
			return null;

		try {
			File f = Recording.read(a[2], a[3], a[4]).getFile();
			if (f == null) {
				return mkNotFoundResponse(path);
			}
			RandomAccessFile rf = new RandomAccessFile(f, "r");
			int n = (int) rf.length();
			if (len == 0) {
				len = n - offset;
			}
			if (n < offset + len || offset < 0) {
				rf.close();
				return new Response(Status.RANGE_NOT_SATISFIABLE, "text/plain", "invalid range");
			}
			if (len > 2 * 1024 * 1024) {
				len = 2 * 1024 * 1024;
			}
			byte[] buffer = new byte[2 * 1024 * 1024];
			rf.read(buffer, offset, len);
			rf.close();
			InputStream is = new ByteArrayInputStream(buffer, 0, len);
			Response r = new Response(Status.PARTIAL_CONTENT, "audio/wave", is);
			r.addHeader("content-range", "bytes " + offset + "-" + (offset+len-1) + "/" + len);
			return r;
		}
		catch (IOException e) {
			return mkNotFoundResponse(path);
		}
			
	}
	
	private Response serveMapFile(String path) {
		// GET /recording/versionName/owner_id/filename/mapfile
		String[] a = path.split("/");
		if (a.length != 6 || !a[1].equals("recording") || !a[5].equals("mapfile"))
			return null;
		
		String group_id = Recording.getGroupIdFromId(a[4]);

		try {
			File mapfile = new File(Recording.getRecordingsPath(
					FileIO.getOwnerPath(a[2], a[3])), group_id + "/" + a[4] + ".map");
			InputStream is = new FileInputStream(mapfile);
			return new Response(Status.OK, "text/plain", is);
		}
		catch (FileNotFoundException e) {
			return mkNotFoundResponse(path);
		}
	}
	
	private Response serveShapeFile(String path) {
		// GET /recording/versionName/owner_id/filename/shapefile
		String[] a = path.split("/");
		if (a.length != 6 || !a[1].equals("recording") || !a[5].equals("shapefile"))
			return null;
		
		try {
			File shapefile = new File(Recording.getRecordingsPath(
					FileIO.getOwnerPath(a[2], a[3])), a[4] + ".shape");
			InputStream is = new FileInputStream(shapefile);
			return new Response(Status.OK, "application/octet-stream", is);
		}
		catch (FileNotFoundException e) {
			return mkNotFoundResponse(path);
		}
	}
	
	private Response serveSpeakerImage(String path) {
		// GET /speaker/versionName/owner_id/filename/image
		String[] a = path.split("/");
		if (a.length != 6 || !a[1].equals("speaker") || !a[5].equals("image"))
			return null;
		
		try {
			InputStream is = new FileInputStream(Speaker.read(a[2], a[3], a[4]).getImageFile());
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
		// GET /recording/versionName/owner_id/filename/smallimage
		String[] a = path.split("/");
		if (a.length != 6 || !a[1].equals("speaker") || !a[5].equals("smallimage"))
			return null;
		
		try {
			InputStream is = new FileInputStream(Speaker.read(a[2], a[3], a[4]).getSmallImageFile());
			return new Response(Status.OK, "image/jpeg", is);
		}
		catch (IOException e) {
			return mkNotFoundResponse(path);
		}
		catch (IllegalArgumentException e) {
			return mkNotFoundResponse(path);
		}
	}

	private Response serveTranscript(IHTTPSession session) {
		// GET /transcript/ignored/new_id
		// GET /transcript/versionNum/owner_id/filename
		// PUT /transcript/versionNum/owner_id/filename
		String path = session.getUri();
		String[] a = path.split("/");
		if (a.length < 2 || !a[1].equals("transcript"))
			return null;
		Method method = session.getMethod();
		if (a.length == 5 && method == Method.GET) {
			// GET /transcript/versionNum/owner_id/FILENAME
			Transcript trs = new Transcript(a[2], a[3], a[4]);
			try {
				InputStream is = new FileInputStream(trs.getFile());
				return new Response(Status.OK, MIME_PLAINTEXT, is);
			}
			catch (FileNotFoundException e) {
				return mkNotFoundResponse(path);
			}
		}
		else if (a.length == 5 && method == Method.PUT) {
			InputStream is = session.getInputStream();
			String text;
			try {
				// TODO: Coundn't use IOUtil due to IOException.
				// Hopefully, N is not too big.
				int N = Integer.parseInt(session.getHeaders().get("content-length"));
				byte[] buffer = new byte[N];
				is.read(buffer);
				text = new String(buffer, 0, N, "UTF-8");
			}
			catch (IOException e) {
				return new Response(Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Failed to read input data.");
			}

			try {
				Transcript trs = new Transcript(a[2], a[3], a[4]);
				trs.save(text);
				return new Response(Status.OK, MIME_PLAINTEXT, trs.getId());
			}
			catch (IOException e) {
				return new Response(Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Failed to save transcript (I/O error).");
			}
			catch (RuntimeException e) {
				return new Response(Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Failed to save transcript.");
			}
		}
		else if (a.length == 6 && a[5].equals("new_id") && method == Method.GET) {
			// GET /transcript/versionNum/ownre_id/FILENAME/new_id
			String filename = newTranscript(a[2], a[3], a[4]);
			if (filename == null)
				return mkNotFoundResponse(path);
			else
				return new Response(Status.OK, MIME_PLAINTEXT, filename);
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
	
	/**
	 * Create a new transcript object and a new filename for the transcript.
	 * From the filename given as the first parameter, only the group ID and
	 * the user ID are used. The rest of the filename is ignored.
	 * 
	 * The transcript file is not actually created in the file system until
	 * save() is called.
	 * 
	 * @param filename 
	 * @return a new filename of the transcript.
	 */
	private String newTranscript(String versionName, String ownerId, 
			String filename) {
		String a[] = filename.split("-");
		try {
			Transcript trs = new Transcript(versionName, ownerId, a[0], a[1]);
			return trs.getId();
		}
		catch (RuntimeException e) {
			return null;
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
