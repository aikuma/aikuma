package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.HTTPClient;

@RunWith(CustomTestRunner.class)
public class HTTPClientTest {
	
	HTTPClient http;
	
	@Before
	public void setUp() throws Exception {
		http = new HTTPClient("http://some.server:1234");
	}
	
	@Test
	public void remapped() {
		Map<String, Object> hash = new LinkedHashMap<String, Object>();
		hash.put("name", "some name");
		hash.put("uuid", "some uuid");
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(http.new SpecificNameValuePair("name", "some name"));
		pairs.add(http.new SpecificNameValuePair("uuid", "some uuid"));
		
		assertEquals(
		  pairs,
		  http.remapped(hash)
		);
	}
	
}
