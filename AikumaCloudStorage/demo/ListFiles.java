import java.util.Date;

import org.lp20.aikuma.storage.GoogleDriveStorage;


public class ListFiles {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: DownloadFile <access_token>");
			System.exit(1);
		}

		GoogleDriveStorage gd = new GoogleDriveStorage(args[0]);
		
		gd.list(new GoogleDriveStorage.ListItemHandler() {
			@Override
			public boolean processItem(String identifier, Date date) {
				// TODO Auto-generated method stub
				String datestr = date == null ? "?" : date.toString();
				System.out.format("%s [%s]\n", identifier, datestr);
				return true;
			}
		});
	}
}
