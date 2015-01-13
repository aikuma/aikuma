package org.lp20.aikuma.servers.index_server;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    private static final String HTTP_ENDPOINT = "https://10.0.2.2:8080/index/";
    String accountEmail = null;

    // The ID below should be changed to the credential you generated for the index_server app
    String server_client_id = "763016806096-rg4ernnhshifbc3g193iibmg0337vsi2.apps.googleusercontent.com";

    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        breakSSL();
        pickUserAccount();
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    String scope = "audience:server:client_id:" + server_client_id;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                accountEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                System.out.println(accountEmail);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You must select an account", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void doQuery(View v) {
        final String queryText = ((EditText) this.findViewById(R.id.queryText)).getText().toString();
        if (accountEmail == null) {
            pickUserAccount();
            return;
        }
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    System.out.println("In query task");
                    String authToken = null;
                    try {
                        authToken = GoogleAuthUtil.getToken(MyActivity.this, accountEmail, scope);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GoogleAuthException e) {
                        e.printStackTrace();
                    }
                    if (authToken == null) {
                        return "Couldn't get auth token";
                    }
                    try {
                        System.out.println("Calling query; text is " + queryText);
                        URL url = new URL(HTTP_ENDPOINT +  queryText);
                        HttpURLConnection cn = (HttpURLConnection) url.openConnection();
                        cn.addRequestProperty("X-Aikuma-Auth-Token", authToken);

                        if (cn.getResponseCode() == cn.HTTP_OK) {
                            BufferedReader r = new BufferedReader(new InputStreamReader(cn.getInputStream()));
                            String line;
                            StringBuffer buffer = new StringBuffer();
                            while ((line = r.readLine()) != null) {
                                buffer.append(line);
                            }
                            return buffer.toString();
                        } else {
                            System.out.println(cn.getResponseCode() + " " + cn.getResponseMessage());
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return "An error occurred during processing";
                }

                @Override
                protected void onPostExecute(String result) {
                    TextView view = (TextView) MyActivity.this.findViewById(R.id.resultText);
                    view.setText(result);
                }
            }.execute(null, null, null);
    }

    /**
     * NB: this method makes SSL accept any certificate for any URL.
     *     This is a bad for security - don't use it for anything but
     *     testing/development.
     */
    private void breakSSL() {
        Log.w("MyActivity", "breakSSL was called; you want to disable this for production");
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        try {
            ctx.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            }, null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
            return;
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }
}

