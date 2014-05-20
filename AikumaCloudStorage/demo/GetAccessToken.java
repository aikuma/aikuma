import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.lp20.aikma.storage.*;

public class GetAccessToken {
	public static void main(String[] args) {
		String client_id = "119115083785-cqbtnha90hobui893c0lc33olghb4uuv.apps.googleusercontent.com";
		String client_secret = "4Yz3JDpqbyf6q-uw0rP2BSnN";
		GoogleAuth auth = new GoogleAuth(client_id, client_secret);
		Desktop desktop = Desktop.getDesktop();
		ArrayList<String> apis = new ArrayList<String>();
		apis.add("https://www.googleapis.com/auth/drive.file");
		URI uri = URI.create(auth.getAuthUrl(apis));
		
		System.out.println("A google page will open where you can login and give permission to access your google drive.");
		try {
			desktop.browse(uri);
		}
		catch (IOException e) {
			System.err.println("Failed to open browser:");
			System.err.println(e.getMessage());
			System.exit(1);
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
		}
		else {
			System.err.println("Failed to get access token.");
			System.exit(1);
		}
	}
}
