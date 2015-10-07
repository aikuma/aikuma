package org.lp20.aikuma.storage.google;

import java.net.MalformedURLException;
import java.net.URL;

import org.lp20.aikuma.net.Http;

public class GHttpFactory {

	private TokenManager tm;
	
	public GHttpFactory(TokenManager tm) {
		this.tm = tm;
	}
	
	public GHttp apply(URL url) { return new GHttp(url).accessToken(tm.accessToken()); }
	
	public GHttp apply(String url) throws MalformedURLException { return apply(new URL(url)); }
	
	public class GHttp extends Http {

		public GHttp(URL url) {
			super(url);
		}
		
		public GHttp accessToken(String authKey) {
		    header("Authorization", "Bearer " + authKey);
		    return this;
		}
	}
}