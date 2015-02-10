import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.DataStore;
import org.lp20.aikuma.storage.Utils;


public class DownloadFile {
	public static void main(String argv[]) {
		if (argv.length != 1) {
			System.out.println("Usage: DownloadFile <identifier>");
			System.exit(1);
		}
		

		Properties config = DemoUtils.readProps();
		String accessToken = config.getProperty("access_token");
		String rootId = config.getProperty("aikuma_root_id");
		String email = config.getProperty("central_drive_email");

		GoogleDriveStorage gd;
		try {
			gd = new GoogleDriveStorage(accessToken, rootId, email);
		} catch (DataStore.StorageException e) {
			System.out.println("Failed to initialize GD");
			System.exit(1);
			return;
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
}
