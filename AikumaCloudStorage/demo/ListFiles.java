import org.lp20.aikma.storage.GoogleDriveStorage;


public class ListFiles {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: DownloadFile <access_token>");
			System.exit(1);
		}

		GoogleDriveStorage gd = new GoogleDriveStorage(args[0]);
		
		gd.list(new GoogleDriveStorage.ListItemHandler() {
			@Override
			public boolean processItem(String identifier) {
				// TODO Auto-generated method stub
				System.out.println(identifier);
				return false;
			}
		});
	}
}
