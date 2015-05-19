import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Properties;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.lp20.aikuma.storage.*;
import org.lp20.aikuma.storage.google.*;

public class GetAccessToken {
	private static final Logger log = Logger.getLogger(GetAccessToken.class.getName());

	public static void main(String[] args) {
		Properties config = DemoUtils.readProps();
		String client_id = config.getProperty("client_id");
		String client_secret = config.getProperty("client_secret");
		log.log(Level.INFO, "client id: " + client_id);
		log.log(Level.INFO, "client secret: " + client_secret);
		GoogleAuth auth = new GoogleAuth(client_id, client_secret);
		Desktop desktop = Desktop.getDesktop();
		ArrayList<String> apis = new ArrayList<String>();
		for (String scope: GoogleDriveStorage.getScopes()) {
			apis.add(scope);
		}
		for (String scope: FusionIndex.getScopes()) {
			apis.add(scope);
		}
	        if (true) {
			URI uri = URI.create(auth.getAuthUrl(apis));
			System.out.println("A google page will open where you can login and give permission to access your google drive.");
	    		try {
			    	desktop.browse(uri);
	    		} catch (IOException e) {
				System.err.println("Failed to open browser:");
				System.err.println(e.getMessage());
				System.exit(1);
	    		}
		} else {
		    	// This is to support Bob's workflow, where he uses the non-default browser for Google stuff; ignore, please
			System.out.println("Browse to this url, and grant permissions.");
			System.out.println(auth.getAuthUrl(apis));
			System.out.println();
		}


		System.out.println("Once you give the permission, a new page opens with authorization code.");
		System.out.print("Please copy and paste the token here: ");
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String code = new String("");
		try {
			String line = bufferedReader.readLine();
			code = line.trim();
		}
		catch (IOException e) {
			System.err.println("Failed to get user input:");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		if (auth.requestAccessToken(code)) {
			System.out.println("Access token: " + auth.getAccessToken());
			System.out.println("Refresh token: " + auth.getRefreshToken());
			config.put("access_token", auth.getAccessToken());
			config.put("refresh_token", auth.getRefreshToken());
			DemoUtils.writeProps(config);
		}
		else {
			System.err.println("Failed to get access token.");
			System.exit(1);
		}
	}
}
