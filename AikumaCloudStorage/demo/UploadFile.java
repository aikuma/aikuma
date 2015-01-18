import java.io.File;

import org.lp20.aikuma.storage.*;


public class UploadFile {
	public static void main(String args[]) {
		if (args.length != 2) {
			System.out.println("Usage: UploadFile <path> <access_token>");
			System.exit(1);
		}
		
		File file = new File(args[0]);
		
		Data data = Data.fromFile(file);
		if (data == null) {
			System.out.println("Failed to open file: " + args[0]);
			System.exit(1);			
		}
		
		GoogleDriveStorage gd = new GoogleDriveStorage(args[1]);
		
		String folderId = gd.createFolder("aikuma");
		
		String download_url = gd.store(file.getName(), data, folderId);
		if (download_url != null) {
			System.out.println("OK");
			System.out.println("File can be downloaded from " + download_url);
		}
		else {
			System.out.println("Upload failed");
		}
	}
}
