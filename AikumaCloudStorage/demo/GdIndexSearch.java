import java.util.Properties;
import java.util.Map;
import org.lp20.aikuma.storage.google.*;
import org.lp20.aikuma.storage.*;

public class GdIndexSearch {
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Usage: GdIndexSearch <tag>");
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

        String q = String.format("fullText contains '%s'", escape(args[0]));
        gdi.search(q, new Index.SearchResultProcessor() {
            @Override
            public boolean process(Map<String,String> meta) {
                for (String key: meta.keySet()) {
                    System.out.print(key);
                    System.out.print(" -> ");
                    System.out.println(meta.get(key));
                }
                System.out.println();
                return true;
            }
        });
    }

    static String escape(String s) {
        return s.replaceAll("'", "\\'");
    }
}

