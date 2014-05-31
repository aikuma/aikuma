import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lp20.aikma.storage.Data;
import org.lp20.aikma.storage.GoogleDriveStorage;
import org.lp20.aikma.storage.Utils;


public class DownloadFile {
	public static void main(String argv[]) {
		if (argv.length != 1) {
			System.out.println("Usage: DownloadFile <identifier>");
			System.exit(1);
		}
		
		GoogleDriveStorage gd = connect();
		if (gd == null) {
			System.out.println("Can't connect to google servers");
			System.exit(1);
		}
		
		InputStream is = gd.load(argv[0]);
		if (is == null) {
			System.out.println("Download failed: " + argv[0]);
			System.exit(1);
		}
		
		File file = new File(argv[0]);
		file = new File(file.getName());
		Pattern p = Pattern.compile("^(.*?)\\s*(\\((\\d+)\\))?(\\.[^.]+)?$");
		while (file.exists()) {
			String filename = file.getName();
			Matcher m = p.matcher(filename);
			if (m.matches() == false) {
				filename += " (1)";
			}
			else if (m.group(2) == null) {
				filename = m.group(1) + " (1)";
				if (m.group(4) != null)
					filename += m.group(4); 
			}
			else {
				String n = String.valueOf(Integer.parseInt(m.group(3)) + 1);
				filename = m.group(1) + " (" + n + ")" + m.group(4); 
			}
			file = new File(filename);
		}

		try {
			FileOutputStream os = new FileOutputStream(file);
			Utils.copyStream(is, os, true);
		}
		catch (IOException e) {
			System.out.println("Failed to save the file: " + file.getName());
			System.exit(1);;
		}
		
		System.out.println("File saved: " + file.getAbsolutePath());
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
