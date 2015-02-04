import java.util.Date;
import java.util.Properties;

import org.lp20.aikuma.storage.GoogleDriveStorage;


public class ListFiles {
	public static void main(String[] args) {
		if (args.length > 1) {
			System.out.println("Usage: ListFile [<access_token>]");
			System.exit(1);
		}
		String accessToken;
		if (args.length == 0) {
		    Properties config = DemoUtils.readProps();
		    accessToken = config.getProperty("access_token");
		} else {
		    accessToken = args[0];
		}

		GoogleDriveStorage gd = new GoogleDriveStorage(accessToken);
		
		gd.list(new GoogleDriveStorage.ListItemHandler() {
			@Override
			public boolean processItem(String identifier, Date date) {
				String datestr = date == null ? "?" : date.toString();
				System.out.format("%s [%s]\n", identifier, datestr);
				return true;
			}
		});
	}
}
