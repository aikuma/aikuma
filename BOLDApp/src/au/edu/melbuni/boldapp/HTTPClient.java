package au.edu.melbuni.boldapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONValue;

import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;

public class HTTPClient {

	public class SpecificNameValuePair implements NameValuePair {

		String name;
		String value;

		public SpecificNameValuePair(String name, String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public boolean equals(Object object) {
			if (!(object instanceof SpecificNameValuePair)) {
				return false;
			}
			SpecificNameValuePair pair = (SpecificNameValuePair) object;

			return this.name == pair.getName() && this.value == pair.getValue();
		}

	}

	String serverURI;
	HttpClient client;

	public HTTPClient(String serverURI) {
		this.serverURI = serverURI;
		this.client = null;
	}

	//
	//
	@SuppressWarnings("unchecked")
	public List<String> getUserIds() {
		return (List<String>) getBody("/users");
	}

	// Sends the user to the server.
	//
	public HttpResponse post(User user) {
		return post("/users", user.toHash());
	}
	
	// Gets a user from the server.
	//
	@SuppressWarnings("unchecked")
	public User getUser(String userId) {
		Map<String, Object> hash = (Map<String, Object>) getBody(userId);
		return User.fromHash(hash);
	}

	// Sends the timeline to the server.
	//
	public HttpResponse post(Timeline timeline) {
		return post("/timelines", timeline.toHash());
	}

	public List<NameValuePair> remapped(Map<String, Object> hash) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		Iterator<Entry<String, Object>> iterator = hash.entrySet().iterator();
		while (iterator.hasNext()) {
			final Entry<String, Object> pairs = iterator.next();

			// This is done to enable tests again.
			//
			nameValuePairs.add(new SpecificNameValuePair(pairs.getKey()
					.toString(), pairs.getValue().toString()));
		}
		return nameValuePairs;
	}

	public HttpResponse post(String path, Map<String, Object> hash) {
		return post(path, remapped(hash));
	}

	public HttpResponse post(String path, List<NameValuePair> nameValuePairs) {
		if (client == null) {
			client = new DefaultHttpClient();
		}

		URI server = null;

		try {
			server = new URI(serverURI + path);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		HttpPost post = new HttpPost(server);

		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpResponse response = null;

		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}

	public HttpResponse get(String path) {
		if (client == null) {
			client = new DefaultHttpClient();
		}

		URI server = null;

		try {
			server = new URI(serverURI + path);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		HttpGet get = new HttpGet(server);

		// try {
		// get.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		HttpResponse response = null;

		try {
			response = client.execute(get);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}

	public Object getBody(String path) {
		HttpResponse response = get(path);
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			String body = "";
			try {
				body = getResponseBody(response.getEntity());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return JSONValue.parse(body);
		}
		return null;
	}

	public String getResponseBody(final HttpEntity entity)
			throws IOException, ParseException {

		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}

		InputStream instream = entity.getContent();

		if (instream == null) {
			return "";
		}

		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(

			"HTTP entity too large to be buffered in memory");
		}

		String charset = getContentCharSet(entity);

		if (charset == null) {

			charset = HTTP.DEFAULT_CONTENT_CHARSET;

		}

		Reader reader = new InputStreamReader(instream, charset);

		StringBuilder buffer = new StringBuilder();

		try {

			char[] tmp = new char[1024];

			int l;

			while ((l = reader.read(tmp)) != -1) {

				buffer.append(tmp, 0, l);

			}

		} finally {

			reader.close();

		}

		return buffer.toString();

	}

	public String getContentCharSet(final HttpEntity entity)
			throws ParseException {

		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}

		String charset = null;

		if (entity.getContentType() != null) {

			HeaderElement values[] = entity.getContentType().getElements();

			if (values.length > 0) {

				NameValuePair param = values[0].getParameterByName("charset");

				if (param != null) {

					charset = param.getValue();

				}

			}

		}

		return charset;

	}

}
