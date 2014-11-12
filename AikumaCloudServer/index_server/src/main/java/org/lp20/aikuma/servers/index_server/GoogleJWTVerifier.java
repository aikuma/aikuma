package org.lp20.aikuma.servers.index_server;

/**
 * Created by bob on 11/11/14.
 */
import io.jsonwebtoken.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;
import java.util.logging.Logger;

import static org.lp20.aikuma.storage.Utils.readStream;

/**
 *  A class for verifying Google issued JWT tokens
 */
public class GoogleJWTVerifier implements JWTVerifier {
    private static final Logger log = Logger.getLogger(GoogleJWTVerifier.class.getName());

    private Map<String, Key> keys;
    private Set<String> client_ids;
    private String audience;

    public static String GOOGLE_CERT_URL = "https://www.googleapis.com/oauth2/v1/certs";

    /**
     *
     * @param audience the client_id for the webapp (see developer console for the project)
     * @param client_ids a List of client_ids for apps that can access the webapp
     */
    public GoogleJWTVerifier(String audience, List<String> client_ids) {
        keys = new HashMap<String, Key>();
        this.audience = audience;
        this.client_ids = new HashSet<>();
        for (String id : client_ids) {
            this.client_ids.add(id);
        }
        getCerts();
    }

    /**
     * Extract a key from a X509 certificate string
     * @param data
     * @return
     */
    private Key extractKey(String data) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(data.getBytes()));
            return cert.getPublicKey();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Fetches certificates from Google, populates this.keys
     * @return
     */
    private boolean getCerts() {
        try {
            HttpURLConnection cn = (HttpURLConnection) new URL(GOOGLE_CERT_URL).openConnection();
            cn.setRequestMethod("GET");
            cn.setDoOutput(false);
            if (cn.getResponseCode() == cn.HTTP_OK) {
                String raw = readStream(cn.getInputStream());
                JSONObject tmp = (JSONObject) JSONValue.parse(raw);
                synchronized (keys) {
                    keys.clear();
                    for (String k : (Set<String>) tmp.keySet()) {
                        String v = (String) tmp.get(k);
                        Key key = extractKey(v);
                        keys.put(k, key);
                    }
                    if (keys.size() > 0) {
                        log.info("Successfully got " + keys.size() + " keys");
                        return true;
                    } else {
                        log.warning("Unable to extract keys from " + tmp.keySet().size() + " certificates");
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean verifyToken(String token) {
        String head = token.split("\\.")[0];
        String kid = (String) ((JSONObject) JSONValue.parse(new String(Base64.getDecoder().decode(head)))).get("kid");
        if (!keys.containsKey(kid)) {
            log.info("Unknown kid " + kid + "; refreshing keys");
            if (!getCerts()) {
                return false;
            }
        }
        Key key = keys.get(kid);
        assert key != null;
        try {
            Claims body = (Claims) Jwts.parser().setSigningKey(key).parse(token).getBody();
            String aud = body.getAudience();
            String azp = (String) body.get("azp");
            if (body.getAudience().equals(audience) && client_ids.contains(azp)) {
                return true;
            }
        } catch (JwtException e) {
            log.info(e.getMessage());
        }
        return  false;
    }

    public static void main(String args[]) {
        String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjNjMmE1MTI0MzQ0ODBiNTAxMWZlMTRjMmFmYzZmYmUzMTU5OTVkZDAifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTE2MzgwNzI0NDAwMDU3NDM3ODI0IiwiYXpwIjoiNzYzMDE2ODA2MDk2LXFhaTByYWNoOGh0OGxucGxoaTJzNjFucjVxOXExcDlvLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJycGFya2VybGRjQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdWQiOiI3NjMwMTY4MDYwOTYtcmc0ZXJubmhzaGlmYmMzZzE5M2lpYm1nMDMzN3ZzaTIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJpYXQiOjE0MTU3NjYxMDcsImV4cCI6MTQxNTc3MDAwN30.u_yX6Wy2hjczF2wlQkaRzNVOR386YI3FezEKUETxB7d3jTO5pBw3EDQ6WRZw1HR4PaA48pzztrwWKPgw9kbS-t5qH6zyH5oS-uhchRM5PNaXU2g5VktO4vAoLarzRL9a_xDAS8EX4ITIzLtggXC7TELB8nagm8L378IR943wc3Q";
        String audience = "763016806096-rg4ernnhshifbc3g193iibmg0337vsi2.apps.googleusercontent.com";
        List<String> client_ids = new ArrayList<>(1);
        client_ids.add("763016806096-qai0rach8ht8lnplhi2s61nr5q9q1p9o.apps.googleusercontent.com");
        GoogleJWTVerifier tv = new GoogleJWTVerifier(audience, client_ids);
        boolean ok = tv.verifyToken(token);
        System.out.println("Token was " + (ok ? "valid" : "invalid"));
    }

}
