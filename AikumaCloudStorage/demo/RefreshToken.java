import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Properties;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.lp20.aikuma.storage.*;

public class RefreshToken {
	private static final Logger log = Logger.getLogger(GetAccessToken.class.getName());

	public static void main(String[] args) {
		Properties config = DemoUtils.readProps();
		String clientId = config.getProperty("client_id");
		String clientSecret = config.getProperty("client_secret");
		String refreshToken = config.getProperty("refresh_token");
		log.log(Level.INFO, "client id: " + clientId);
		log.log(Level.INFO, "client secret: " + clientSecret);
		log.log(Level.INFO, "refresh token: " + refreshToken);
		log.log(Level.INFO, "old access token: " + config.getProperty("access_token"));
		GoogleAuth auth = new GoogleAuth(clientId, clientSecret);
		
		if (auth.refreshAccessToken(refreshToken)) {
			System.out.println("Access token: " + auth.getAccessToken());
			config.put("access_token", auth.getAccessToken());
			DemoUtils.writeProps(config);
		}
		else {
			System.err.println("Failed to get access token.");
			System.exit(1);
		}
	}
}
