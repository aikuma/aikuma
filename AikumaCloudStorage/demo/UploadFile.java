import java.io.File;

import org.lp20.aikma.storage.*;


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
		
		if (gd.store(file.getName(), data)) {
			System.out.println("OK");
		}
		else {
			System.out.println("Upload failed");
		}
	}
}
