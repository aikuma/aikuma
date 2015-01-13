package org.lp20.aikuma.servers.index_server;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lp20.aikuma.storage.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles authenticating a service account against Google's OAUTH2 services
 */
public class GoogleServiceAuth {

    private static final Logger log = Logger.getLogger(GoogleServiceAuth.class.getName());

    private String serviceEmail;
    private Key privateKey;
    private static final String KEY_ALIAS = "privatekey";

    private static final String TYP = "TYP"; // requred header field
    private static final String AUD = "https://accounts.google.com/o/oauth2/token"; // required audience field
    private static final String GRANT_TYPE = URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer"); // required POST field

    /**
     *
     * @param serviceEmail the email address of the service account
     * @param privateKey a private {@link java.security.Key} instance to use for signing requests
     */
    public GoogleServiceAuth(String serviceEmail, Key privateKey) {
        this.privateKey = privateKey;
        this.serviceEmail = serviceEmail;
    }

    /**
     * Note: This class expects that the key alias is "privatekey", which is the
     *       value used in Google's PKCS12 files. If you're doing something fancy
     *       you'll need to tweak the constant in this class.
     *
     *       Sorry.
     *
     * @param serviceEmail the email address of the service account
     * @param privateKeyPath a path to a PKCS12 file containing the private key
     * @param password the paswword for the file and key (should be the same)
     */
    public GoogleServiceAuth(String serviceEmail, String privateKeyPath, String password) {
        if (!loadFromFile(privateKeyPath, password)) {
            throw new RuntimeException("Unable to load to load privateKey from " + privateKeyPath);
        }
        this.serviceEmail = serviceEmail;
    }


    /**
     * Get a new access token from Google's servers
     *
     * @param forScopes the scopes for which the token should be valid, separated by spaces
     * @return A token String if successful, null otherwise
     */
    public String getAccessToken(String forScopes) {
        String token = null;
        String jwt = makeJWT(forScopes);
        return makeTokenRequest(jwt);

    }
    private String makeTokenRequest(String jwt) {
        String body = String.format("grant_type=%s&assertion=%s",GRANT_TYPE, jwt);
        try {
            HttpURLConnection cn = (HttpURLConnection) new URL(AUD).openConnection();
            cn.setRequestMethod("POST");
            cn.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(cn.getOutputStream());
            out.write(body);
            out.flush();
            out.close();
            if (cn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String tmp = Utils.readStream(cn.getInputStream());
                JSONObject o = (JSONObject) JSONValue.parse(tmp);
                return (String) o.get("access_token");
            } else {
                log.warning("Unable to get token : response = " + cn.getResponseCode() + ", " + cn.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    private String makeJWT(String forScopes) {

        Map<String, Object> header = new HashMap<>();
        header.put("typ", TYP);

        Map<String, Object> claims = new HashMap<>();
        claims.put("aud", AUD);
        claims.put("iss", this.serviceEmail);
        claims.put("scope", forScopes);
        long iat = System.currentTimeMillis() / 1000l; // time request was made
        claims.put("iat",new Long(iat));
        long exp = iat + (60 * 60); // expiration of request; <= 1 hr
        claims.put("exp", new Long(exp));

        return Jwts.builder().setHeader(header).setClaims(claims).signWith(SignatureAlgorithm.RS256, privateKey).compact();
    }

    private boolean loadFromFile(String path, String password) {
        try {
            File pkcs12 = new File(path);
            KeyStore ks = KeyStore.getInstance("pkcs12");
            ks.load(new FileInputStream(
                    new File("private_key.p12")), password.toCharArray());
            this.privateKey = ks.getKey(KEY_ALIAS, password.toCharArray());
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Check to see if an access_token is valid
     *
     * @param accessToken an OATH2 access token
     * @return true if valid, false otherwise
     */
    public static boolean validateAccessToken(String accessToken) {

        try {
            URL url = new URL("https://www.googleapis.com/oauth2/v2/tokeninfo");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(true);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write("access_token="+ accessToken);
            writer.flush();
            writer.close();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            } else if (con.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                return false;
            } else {
                System.err.println(con.getResponseCode());
                System.err.println(con.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    public static void main(String args[]) {
        String email = "763016806096-8clbsdoi4vkif4t6esk6cghhvcmqapel@developer.gserviceaccount.com";
        GoogleServiceAuth auth = new GoogleServiceAuth(email, "private_key.p12", "notasecret");
        String scopes = "https://www.googleapis.com/auth/fusiontables https://www.googleapis.com/auth/drive.metadata.readonly";
        String jwt = auth.makeJWT(scopes);

        System.out.println("JWT to send:");
        System.out.println(jwt);
        System.out.println();
        System.out.println("Token response:");
        System.out.println(auth.makeTokenRequest(jwt));



    }
}
