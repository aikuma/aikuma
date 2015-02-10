import java.io.File;
import java.util.Properties;

import org.lp20.aikuma.storage.*;


public class UploadFile {
	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("Usage: UploadFile <path>");
			System.exit(1);
		}
		Properties config = DemoUtils.readProps();
		String accessToken = config.getProperty("access_token");
		String rootId = config.getProperty("aikuma_root_id");
		String email = config.getProperty("central_drive_email");
				
		File file = new File(args[0]);
		Data data = Data.fromFile(file);
		if (data == null) {
			System.out.println("Failed to open file: " + args[0]);
			System.exit(1);			
		}
		
		GoogleDriveStorage gd;
		try {
			gd = new GoogleDriveStorage(accessToken, rootId, email);
		} catch (DataStore.StorageException e) {
			System.out.println("Failed to initialize GD");
			System.exit(1);
			return;
		}
		
		String download_url = gd.store(file.getName(), data);
		if (download_url != null) {
			System.out.println("OK");
			System.out.println("File can be downloaded from " + download_url);
		}
		else {
			System.out.println("Upload failed");
		}
	}
}
