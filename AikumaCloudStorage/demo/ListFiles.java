import java.util.Date;
import java.util.Properties;

import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.DataStore;


public class ListFiles {
	public static void main(String[] args) {
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
		
		gd.list(new GoogleDriveStorage.ListItemHandler() {
			@Override
			public boolean processItem(String identifier, Date date) {
				if (date == null)
					System.out.format("[????-??-?? ??:??:?? ????] %s\n", identifier);
				else
					System.out.format("[%2$tF %2$tT%2$tz] %1s\n", identifier, date);
				return true;
			}
		});
	}
}
