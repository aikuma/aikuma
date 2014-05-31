import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.lp20.aikma.storage.GoogleDriveStorage;


public class ListFiles {
	public static void main(String[] args) {
		GoogleDriveStorage gd = connect();
		if (gd == null) {
			System.out.println("Can't connect to google servers");
			System.exit(1);
		}

		gd.list(new GoogleDriveStorage.ListItemHandler() {
			@Override
			public boolean processItem(String identifier) {
				// TODO Auto-generated method stub
				System.out.println(identifier);
				return false;
			}
		});
	}
	
	private static GoogleDriveStorage connect() {
		String client_id = "119115083785-cqbtnha90hobui893c0lc33olghb4uuv.apps.googleusercontent.com";
		String client_secret = "4Yz3JDpqbyf6q-uw0rP2BSnN";
		GoogleDriveStorage gd = new GoogleDriveStorage(client_id, client_secret);
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.browse(URI.create(gd.getAuthUrl()));
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
		if (gd.obtainAccessToken(code))
			return gd;
		else
			return null;
	}
}
