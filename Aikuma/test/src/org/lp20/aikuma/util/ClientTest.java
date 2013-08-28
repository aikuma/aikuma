package org.lp20.aikuma.util;

import java.io.File;
import android.test.AndroidTestCase;

public class ClientTest extends AndroidTestCase {

	public void testConnectAndLogin() throws Exception {
		Client client = new Client(new File("."),new File("."));
		assertFalse(client.isConnected());
		client.connect("192.168.1.1");
		assertTrue(client.isConnected());
		assertTrue(client.login("admin", "admin"));
		assertTrue(client.logout());
		client.connect("192.168.1.1");
		assertFalse(client.login("some silly user", "some silly password"));
		client.disconnect();
	}
	/*
	@Test
	public void testConnectAndLogin() throws Exception {
		Client client = new Client(new File("."), new File("."));
		assertFalse(client.isConnected());
		client.connect("us1.hostedftp.com");
		assertTrue(client.isConnected());
		assertTrue(client.login("g987868@rmqkr.net", "donkeyman"));
		assertTrue(client.logout());
		client.connect("us1.hostedftp.com");
		assertFalse(client.login("some silly user", "some silly password"));
		client.disconnect();
	}
	*/

	public void testPush() throws Exception {
		Client client = new Client(new
		File("/home/oadams/aikuma/Aikuma/test/src/org/lp20/aikuma/util/ClientTest.java"), new File("/"));
		client.connect("192.168.1.1");
		client.login("admin", "admin");
		client.pushFile2(new File("Client.java"));
		client.logout();
		client.disconnect();
	}
}
