package au.edu.melbuni.boldapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import au.edu.melbuni.boldapp.models.Timeline;
import au.edu.melbuni.boldapp.models.User;

public class HTTPClient {
	
	public class SpecificNameValuePair implements NameValuePair {
		
		String name;
		String value;
		
		public SpecificNameValuePair(String name, String value) {
			this.name  = name;
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
	
	// Sends the user to the server.
	//
	public HttpResponse post(User user) {
		return post("/users", user.toHash());
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
	        nameValuePairs.add(new SpecificNameValuePair(
	        	pairs.getKey().toString(),
	        	pairs.getValue().toString()
	        ));
	    }
	    return nameValuePairs;
	}
	
	public HttpResponse post(String path, Map<String, Object> hash) {
		return post(path, remapped(hash));
	}
	
	public HttpResponse post(String path, List<NameValuePair> nameValuePairs) {
		if (client == null) { client = new DefaultHttpClient(); }
		
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
	
}
