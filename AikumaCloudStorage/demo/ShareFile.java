import java.util.Date;
import java.util.Properties;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.DataStore;


public class ShareFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: ShareFile <filename>");
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
		
		if (gd.share(args[0]))
			System.out.println("Success");
		else
			System.out.println("Failure");
	}

}
