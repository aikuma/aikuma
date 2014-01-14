package org.lp20.aikuma.http;
import org.apache.http.conn.util.InetAddressUtils;
import org.lp20.aikuma.http.NanoHTTPD;

import java.io.IOException;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InetAddress;

public class Server extends NanoHTTPD {
	private static int port_;
	private static Server server_;

	protected Server(int port) {
		super(port);
	}
	
	public static Server getServer() {
		if (server_ == null) {
			server_ = new Server(port_);
		}
		return server_;
	}
	
	public static void setPort(int port) {
		port_ = port;
	}
	
	public static void destroyServer() {
		if (server_ != null) {
			server_.stop();
			server_ = null;
		}
	}

	@Override
	public void start() throws IOException {
		getIpAddress();
		super.start();
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		return new Response("hello");
	}
	
	private String getIpAddress() {
		Enumeration<NetworkInterface> ifs;
		try {
			ifs = NetworkInterface.getNetworkInterfaces();
		}
		catch (java.net.SocketException e) {
			return null;
		}
		while (ifs.hasMoreElements()) {
			Enumeration<InetAddress> addrs = ifs.nextElement().getInetAddresses();
			while (addrs.hasMoreElements()) {
				InetAddress addr = addrs.nextElement();
				if (!addr.isLoopbackAddress()) {
					String ip = addr.getHostAddress();
					if (InetAddressUtils.isIPv4Address(ip)) {
						android.util.Log.e("http--", addr.getHostName());
						return ip;
					}
				}
			}
		}
		return null;
	}
}
