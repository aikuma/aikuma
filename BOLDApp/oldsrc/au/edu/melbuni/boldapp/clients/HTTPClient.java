//package au.edu.melbuni.boldapp.clients;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.io.UnsupportedEncodingException;
//import java.net.HttpURLConnection;
//import java.net.ProtocolException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import org.apache.http.HeaderElement;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.ParseException;
//import org.apache.http.StatusLine;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.ContentBody;
//import org.apache.http.entity.mime.content.FileBody;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.protocol.HTTP;
//import org.json.simple.JSONValue;
//
//import au.edu.melbuni.boldapp.models.Segment;
//import au.edu.melbuni.boldapp.models.Timeline;
//import au.edu.melbuni.boldapp.models.User;
//import au.edu.melbuni.boldapp.models.Users;
//
//public class HTTPClient extends Client {
//
//	public class SpecificNameValuePair implements NameValuePair {
//
//		String name;
//		String value;
//
//		public SpecificNameValuePair(String name, String value) {
//			this.name = name;
//			this.value = value;
//		}
//
//		@Override
//		public String getName() {
//			return name;
//		}
//
//		@Override
//		public String getValue() {
//			return value;
//		}
//
//		@Override
//		public boolean equals(Object object) {
//			if (!(object instanceof SpecificNameValuePair)) {
//				return false;
//			}
//			SpecificNameValuePair pair = (SpecificNameValuePair) object;
//
//			return this.name == pair.getName() && this.value == pair.getValue();
//		}
//
//	}
//
//	HttpClient client;
//
//	public HTTPClient(String serverURI) {
//		super(serverURI);
//		this.client = null;
//	}
//	
//	@Override
//	public Object getClient() {
//		return client;
//	}
//
//	//
//	//
//	@Override
//	@SuppressWarnings("unchecked")
//	public List<String> getUserIds() {
//		return (List<String>) getBody("/users/ids");
//	}
//	
//	// Gets a user from the server.
//	//
//	// Note: Also gets the user picture.
//	//
//	@Override
//	@SuppressWarnings("unchecked")
//	public User getUser(String userId) {
//		Map<String, Object> hash = (Map<String, Object>) getBody("/user/" + userId + ".json");
//		User user = User.fromHash(hash);
//		user.putProfileImage(getByteArray("/user/" + userId + "/picture.png"));
//		return user;
//	}
//	
//	@Override
//	@SuppressWarnings("unchecked")
//	public List<String> getTimelineIds() {
//		return (List<String>) getBody("/timelines/ids");
//	}
//
//	@Override
//	@SuppressWarnings("unchecked")
//	public Timeline getTimeline(String timelineId, String userId, Users users) {
//		// TODO No user id needed?
//		Map<String, Object> hash = (Map<String, Object>) getBody("/timeline/" + timelineId + ".json");
//		Timeline timeline = Timeline.fromHash(users, hash);
////		timeline.putProfileImage(getImage("/timeline/" + timelineId + "/picture.png"));
//		return timeline;
//	}
//
//	@Override
//	@SuppressWarnings("unchecked")
//	public List<String> getSegmentIds(String timelineId) {
//		return (List<String>) getBody("/timeline/" + timelineId + "/segments/ids");
//	}
//	
////	@Override
////	public boolean doesExist(User user) {
////		return parseResponseExists(get("/user/" + user.getIdentifier()));
////	}
////	
////	@Override
////	public boolean doesExist(Timeline timeline) {
////		return parseResponseExists(get("/timeline/" + timeline.getIdentifier()));
////	}
//
//	@Override
//	@SuppressWarnings("unchecked")
//	public Segment getSegment(String timelineId, String segmentId) {
//		if (doesSegmentExist(timelineId, segmentId)) {
//			Map<String, Object> hash = (Map<String, Object>) getBody("/segment/" + segmentId + ".json");
//			Segment segment = Segment.fromHash(hash);
//			// TODO Refactor!!!
//			byte[] bytes = getByteArray("/segment/" + segmentId + "/soundfile.gp3");
//			if (bytes != null) {
//				segment.putSoundfile(timelineId, bytes);
//			} else {
//				return null;
//			}
//			return segment;
//		} else {
//			return null;
//		}
//	}
//
//	// Sends the user to the server.
//	//
//	// Note: Includes sending his user picture.
//	//
//	@Override
//	public boolean post(User user) {
//		HttpResponse response = post("/users", user.toHash());
//		postFile("/user/" + user.getIdentifier() + "/picture", user.getProfileImagePath());
//		return isResponseOk(response);
//	}
//
//	// Sends the timeline to the server.
//	//
//	@Override
//	public boolean post(Timeline timeline) {
//		HttpResponse response =  post("/timelines", timeline.toHash());
//		return isResponseOk(response);
//	}
//
//	@Override
//	public boolean post(Segment segment, String timelineId) {
//		HttpResponse response = post("/timeline/" + timelineId + "/segments", segment.toHash());
//		postFile("/segment/" + segment.getIdentifier() + "/soundfile", segment.getSoundfilePath(timelineId));
//		return isResponseOk(response);
//	}
//	
//	public boolean isResponseOk(HttpResponse response) {
//		return response.getStatusLine().getStatusCode() == 200;
//	}
//	
//	@Override
//	public boolean doesSegmentExist(String timelineId, String segmentId) {
//		return parseResponseExists(get("/segment/" + segmentId));
//	}
//	
//	public boolean parseResponseExists(HttpResponse response) {
//		if (response.getStatusLine().getStatusCode() == 404) {
//			return false;
//		} else {
//			return true;
//		}
//	}
//
//	public List<NameValuePair> remapped(Map<String, Object> hash) {
//		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//
//		Iterator<Entry<String, Object>> iterator = hash.entrySet().iterator();
//		while (iterator.hasNext()) {
//			final Entry<String, Object> pairs = iterator.next();
//
//			// This is done to enable tests again.
//			//
//			nameValuePairs.add(new SpecificNameValuePair(pairs.getKey()
//					.toString(), pairs.getValue().toString()));
//		}
//		return nameValuePairs;
//	}
//
//	public HttpResponse post(String path, Map<String, Object> hash) {
//		return post(path, remapped(hash));
//	}
//
//	public HttpResponse post(String path, List<NameValuePair> nameValuePairs) {
//		HttpPost post = new HttpPost(getServerURI(path));
//
//		try {
//			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return execute(post);
//	}
//	
//	public HttpResponse postFile(String path, String filePath) {
//		HttpPost post = new HttpPost(getServerURI(path));
//		
//		ContentBody file = new FileBody(new File(filePath));
//		MultipartEntity entity = new MultipartEntity();
//		entity.addPart("file", file);
//		post.setEntity(entity);
//
//		return execute(post);
//	}
//	
//	@Override
//	public URI getServerURI(String path) {
//		URI server = null;
//
//		try {
//			server = new URI(serverURI + path);
//		} catch (URISyntaxException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		return server;
//	}
//	
//	@Override
//	public void lazilyInitializeClient() {
//		if (client == null) {
//			client = new DefaultHttpClient();
//		}
//	}
//	
//	public HttpResponse execute(HttpPost post) {
//		lazilyInitializeClient();
//		
//		HttpResponse response = null;
//
//		try {
//			response = client.execute(post);
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return response;
//	}
//
//	public HttpResponse get(String path) {
//		if (client == null) {
//			client = new DefaultHttpClient();
//		}
//
//		URI server = null;
//
//		try {
//			server = new URI(serverURI + path);
//		} catch (URISyntaxException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		HttpGet get = new HttpGet(server);
//
//		// try {
//		// get.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//		// } catch (UnsupportedEncodingException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// }
//
//		HttpResponse response = null;
//
//		try {
//			response = client.execute(get);
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return response;
//	}
//	
//	// TODO Refactor.
//	//
//	public byte[] getByteArray(String path) {
//	    HttpURLConnection connection = null;
//		try {
//			connection = (HttpURLConnection) getServerURI(path).toURL().openConnection();
//		} catch (IOException e) {
//			throw new RuntimeException(e.toString());
//		}
//	    try {
//			connection.setRequestMethod("GET");
//		} catch (ProtocolException e) {
//			throw new RuntimeException(e.toString());
//		}
//	    connection.setDoInput(true);
//	    connection.setDoOutput(true);
//	    connection.setUseCaches(false);
////	    connection.addRequestProperty("Accept","image/png, image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-shockwave-flash, */*");
////	    connection.addRequestProperty("Accept-Language", "en-us,zh-cn;q=0.5");
////	    connection.addRequestProperty("Accept-Encoding", "gzip, deflate");
////	    connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 2.0.50727; MS-RTC LM 8)");
//	    try {
//			connection.connect();
//		} catch (IOException e) {
//			throw new RuntimeException(e.toString());
//		}
//	    
//	    // Read into buffer. 
//	    //
//	    InputStream is = null;
//	    ByteArrayOutputStream os = new ByteArrayOutputStream();
//	    byte[] buffer = new byte[1024];
//		try {
//			is = connection.getInputStream();
//			int byteRead = 0;
//			byteRead = is.read(buffer);
//		    while(byteRead != -1)
//		    {
//		    	os.write(buffer, 0, byteRead);
//		        byteRead = is.read(buffer);
//		    }
//		} catch (IOException e) {
//			// throw new RuntimeException(e.toString());
//			return null;
//		}
//	    
//	    return os.toByteArray();
//	}
//
//	public Object getBody(String path) {
//		HttpResponse response = get(path);
//		StatusLine statusLine = response.getStatusLine();
//		if (statusLine.getStatusCode() == 200) {
//			String body = "";
//			try {
//				body = getResponseBody(response.getEntity());
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			return JSONValue.parse(body);
//		}
//		return null;
//	}
//
//	public String getResponseBody(final HttpEntity entity)
//			throws IOException, ParseException {
//
//		if (entity == null) {
//			throw new IllegalArgumentException("HTTP entity may not be null");
//		}
//
//		InputStream instream = entity.getContent();
//
//		if (instream == null) {
//			return "";
//		}
//
//		if (entity.getContentLength() > Integer.MAX_VALUE) {
//			throw new IllegalArgumentException(
//
//			"HTTP entity too large to be buffered in memory");
//		}
//
//		String charset = getContentCharSet(entity);
//
//		if (charset == null) {
//
//			charset = HTTP.DEFAULT_CONTENT_CHARSET;
//
//		}
//
//		Reader reader = new InputStreamReader(instream, charset);
//
//		StringBuilder buffer = new StringBuilder();
//
//		try {
//
//			char[] tmp = new char[1024];
//
//			int l;
//
//			while ((l = reader.read(tmp)) != -1) {
//
//				buffer.append(tmp, 0, l);
//
//			}
//
//		} finally {
//
//			reader.close();
//
//		}
//
//		return buffer.toString();
//
//	}
//
//	public String getContentCharSet(final HttpEntity entity)
//			throws ParseException {
//
//		if (entity == null) {
//			throw new IllegalArgumentException("HTTP entity may not be null");
//		}
//
//		String charset = null;
//
//		if (entity.getContentType() != null) {
//
//			HeaderElement values[] = entity.getContentType().getElements();
//
//			if (values.length > 0) {
//
//				NameValuePair param = values[0].getParameterByName("charset");
//
//				if (param != null) {
//
//					charset = param.getValue();
//
//				}
//
//			}
//
//		}
//
//		return charset;
//
//	}
//
//	@Override
//	public boolean doesTimelineExist(String timelineId) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean doesUserExist(String userId) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//}
