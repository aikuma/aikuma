import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;

import org.lp20.aikma.storage.*;


public class UploadFile {
	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("Usage: UploadFile <path>");
			System.exit(1);
		}
		
		File file = new File(args[0]);
		
		Data data = Data.fromFile(file);
		if (data == null) {
			System.out.println("Failed to open file: " + args[0]);
			System.exit(1);			
		}
		
		GoogleDriveStorage gd = connect();
		if (gd == null) {
			System.out.println("Can't connect to google servers");
			System.exit(1);
		}
		
		if (gd.store(file.getName(), data)) {
			System.out.println("OK");
		}
		else {
			System.out.println("Upload failed");
		}
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
