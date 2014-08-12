import java.util.Date;

import org.lp20.aikuma.storage.GoogleDriveStorage;


public class ShareFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: DownloadFile <access_token> <filename>");
			System.exit(1);
		}

		GoogleDriveStorage gd = new GoogleDriveStorage(args[0]);
		
		if (gd.share(args[1]))
			System.out.println("Success");
		else
			System.out.println("Failure");
	}

}
