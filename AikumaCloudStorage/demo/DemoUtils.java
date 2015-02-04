import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DemoUtils {
    private static final Logger log = Logger.getLogger(DemoUtils.class.getName());

    public static final String aikumaFile = System.getProperty("user.home") + "/.aikuma";

    public static Properties readProps(String path) {
        File file = new File(path);
	if (file.exists()) {
	    Properties config = new Properties();
	    try {
                config.load(new FileInputStream(file));
            } catch (IOException e) {
                return null;
            }
            return config;
	} else {
            return null;
	}
    }

    public static Properties readProps() {
	return readProps(DemoUtils.aikumaFile);
    }

    public static boolean writeProps(Properties prop, String path) {
        try {
    	    FileOutputStream out = new FileOutputStream(new File(path));
            prop.store(out, "");
            return true;
	} catch (IOException e) {
            log.log(Level.INFO, "error");
            return false;
        }
    }

    public static boolean writeProps(Properties prop) {
        return writeProps(prop, DemoUtils.aikumaFile);
    }
}

