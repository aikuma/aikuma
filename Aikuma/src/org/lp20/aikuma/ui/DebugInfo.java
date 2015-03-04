package org.lp20.aikuma.ui;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.lp20.aikuma.storage.DataStore;
import org.lp20.aikuma.storage.FusionIndex2;
import org.lp20.aikuma.storage.GoogleDriveStorage;
import org.lp20.aikuma.storage.Index;
import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma2.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UI for debugging/testing communication with cloud-backend
 * 
 * @author Haejoong 
 *
 */
public class DebugInfo extends Activity {
    final String TAG = DebugInfo.class.getName();

    String mEmail;
    boolean mRecovering = false;
    SharedPreferences mPref;
    GoogleDriveStorage mGd;
    FusionIndex2 mFi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_info);

        ((TextView) findViewById(R.id.txtPkgName)).setText(getPackageName());
        ((TextView) findViewById(R.id.txtFingerprint)).setText(appFingerprint());

        /*
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        startActivityForResult(intent, 1004);
        */
        mEmail = AikumaSettings.getCurrentUserId();
        ((TextView) findViewById(R.id.txtEmail)).setText(mEmail);

        ((TextView) findViewById(R.id.txtOldToken)).setText(AikumaSettings.getCurrentUserToken());

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1004:
                if (resultCode == RESULT_OK) {
                    mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    ((TextView) findViewById(R.id.txtEmail)).setText(mEmail);
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "You must select an account", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1008:
                String[] toks = getTokens();
                if (toks != null) {
                    mPref.edit()
                            .putString(AikumaSettings.SETTING_AUTH_TOKEN_KEY, toks[0])
                            .putString("id_token", toks[1])
                            .commit();
                    displayTokens();
                }
                break;
            default:
                break;
        }
    }

    private String getScope() {
        String joiner = "";
        StringBuilder scopeBuilder = new StringBuilder("oauth2:");
        for (String s : GoogleDriveStorage.getScopes()) {
            scopeBuilder.append(joiner);
            scopeBuilder.append(s);
            joiner = " ";
        }
        for (String s: FusionIndex2.getScopes()) {
            scopeBuilder.append(joiner);
            scopeBuilder.append(s);
        }
        return scopeBuilder.toString();
    }

    private String[] getTokens() {
        /*
        try {
            return GoogleAuthUtil.getTokens(DebugInfo.this, mEmail, getScope());
        } catch (IOException e) {
            Log.i(TAG, "Failed to get access token (IOException)");
        } catch (UserRecoverableAuthException e) {
            if (mRecovering == false) {
                mRecovering = true;
                Intent intent = e.getIntent();
                startActivityForResult(intent, 1008);
                return null;
            }
        } catch (GoogleAuthException e) {
            Log.i(TAG, "Failed to get access token (fatal): " + e.getMessage());
        }
        mRecovering = false;
        return null;
        */
        return new String[]{
            AikumaSettings.getCurrentUserToken(),
            AikumaSettings.getCurrentUserIdToken()
        };
    }

    /**
     * asdf
     * @param view asdf
     */
    public void runAuth(View view) {
        new AsyncTask<Void, Void, String[]>() {
            protected String[] doInBackground(Void... params) {
                return getTokens();
            }

            protected void onPostExecute(String[] tokens) {
                if (tokens == null)
                    Log.i(TAG, "failed");
                else {
                    final String[] toks = tokens;
                    mPref.edit()
                            .putString(AikumaSettings.SETTING_AUTH_TOKEN_KEY, tokens[0])
                            .putString("id_token", tokens[1])
                            .commit();
                    new AsyncTask<Void,Void,Void>() {
                        @Override
                        protected Void doInBackground(final Void ... params) {
                            try {
                                mGd = new GoogleDriveStorage(
                                        AikumaSettings.getCurrentUserToken(),
                                        AikumaSettings.ROOT_FOLDER_ID,
                                        AikumaSettings.CENTRAL_USER_ID);
                            } catch (DataStore.StorageException e) {
            			Log.e(TAG, "Failed to initialize GoogleDriveStorage");
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(final Void result) {
                            mFi = new FusionIndex2(AikumaSettings.getIndexServerUrl(), toks[1], toks[0]);
                            displayTokens();
                        }
                    }.execute();
                }
            }
        }.execute(null, null, null);
    }

    /**
     * asdf
     * @param view	asdf
     */
    public void listDocs(View view) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mGd.list(new DataStore.ListItemHandler() {
                    @Override
                    public boolean processItem(String id, Date ts) {
                        final String s = id;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                appendText(s + "\n");
                            }
                        });
                        return true;
                    }
                });
                return null;
            }
        }.execute(null, null, null);
    }

    /**
     * asdf
     * @param view	asdf
     */
    public void listIDs(View view) {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Map<String, String> opt = new HashMap<String, String>();
                mFi.search(opt, new Index.SearchResultProcessor() {
                    @Override
                    public boolean process(Map<String, String> record) {
                        final Map<String,String> r = record;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                appendText(r.get("identifier") + "\n");
                            }
                        });
                        return true;
                    }
                });
                return null;
            }
        }.execute();
    }

    /**
     * asdf
     * @param view	asdf
     */
    public void requestShare(View view) {
        new AsyncTask<Void,Void,Boolean>() {
            final String identifier = ((TextView) findViewById(R.id.identifier)).getText().toString();
            @Override
            protected Boolean doInBackground(Void... params) {
                String email = AikumaSettings.getCurrentUserId();
                try {
                    URL base = new URL(AikumaSettings.getIndexServerUrl());
                    String path = String.format("/file/%s/share/%s", identifier, email);
                    URL url = new URL(base, path);
                    Log.i(TAG, "share url: " + url.toString());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("PUT");
                    con.setDoOutput(false);
                    con.addRequestProperty("X-Aikuma-Auth-Token", AikumaSettings.getCurrentUserIdToken());
                    switch (con.getResponseCode()) {
                        case 200:
                            return true;
                        case 404:
                            return false;
                        default:
                            return false;
                    }
                } catch (MalformedURLException e) {
                    Log.e(TAG, "malformed URL: " + e.getMessage());
                    return false;
                } catch (ProtocolException e) {
                    Log.e(TAG, "protocol error: " + e.getMessage());
                    return false;
                } catch (IOException e) {
                    Log.e(TAG, "io exception: " + e.getMessage());
                    return false;
                }
            }
            @Override
            protected void onPostExecute(final Boolean result) {
                if (result.booleanValue())
                    appendText("ok\n");
                else
                    appendText("error\n");
            }
        }.execute();
    }

    /**
     * asdf
     * @param view	asdf
     */
    public void clearLog(View view) {
        ((TextView) findViewById(R.id.txtGd)).setText("");
    }

    private void appendText(String text) {
        ((TextView) findViewById(R.id.txtGd)).append(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_debug_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayTokens() {
        String accessToken = mPref.getString(AikumaSettings.SETTING_AUTH_TOKEN_KEY, null);
        String idToken = mPref.getString("id_token", null);
        if (accessToken != null)
            ((TextView) findViewById(R.id.txtAccessToken)).setText(accessToken);
        else
            ((TextView) findViewById(R.id.txtAccessToken)).setText("null");
        if (idToken != null)
            ((TextView) findViewById(R.id.txtIdToken)).setText(idToken);
        else
            ((TextView) findViewById(R.id.txtIdToken)).setText("null");
    }

    private String appFingerprint() {
        PackageManager pm = this.getPackageManager();
        String packageName = this.getPackageName();
        int flags = PackageManager.GET_SIGNATURES;

        PackageInfo packageInfo = null;

        try {
            packageInfo = pm.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        Signature[] signatures = packageInfo.signatures;

        byte[] cert = signatures[0].toByteArray();

        InputStream input = new ByteArrayInputStream(cert);

        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            e.printStackTrace();
            return null;
        }
        X509Certificate c = null;
        try {
            c = (X509Certificate) cf.generateCertificate(input);
        } catch (CertificateException e) {
            e.printStackTrace();
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(c.getEncoded());

            StringBuffer hexString = new StringBuffer();
            String prefix = "";
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i]);
                if (appendString.length() == 1) hexString.append("0");
                hexString.append(prefix);
                hexString.append(appendString.toUpperCase());
                prefix = ":";
            }

            return hexString.toString();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return null;
        }
    }
}
