import java.util.Properties;
import java.util.Map;
import org.lp20.aikuma.storage.google.*;
import org.lp20.aikuma.storage.*;

public class GdIndex {
    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Usage: GdIndex <identifier> [<tag> ...]");
            System.exit(1);
        }

        Properties config = DemoUtils.readProps();
        final String accessToken = config.getProperty("access_token");
        String rootName = config.getProperty("aikuma_root_name");
        GoogleDriveIndex gdi = new GoogleDriveIndex(rootName, new TokenManager() {
            @Override
            public String accessToken() {
                return accessToken;
            }
        });

        Map<String,String> meta = new java.util.HashMap<String,String>();
        String identifier = args[0];
        for (int i=1; i < args.length; ++i)
            meta.put(args[i], "");
        gdi.index(identifier, meta);
    }

    static String escape(String s) {
        return s.replaceAll("'", "\\'");
    }
}

