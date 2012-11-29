package au.edu.unimelb.boldapp;

import java.io.StringWriter;

import android.util.Log;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.simple.JSONObject;

import au.edu.unimelb.boldapp.sync.Client;

public class SyncActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync);
	}

	/**
	 * When the back button is pressed.
	 *
	 * @param	view	the back button.
	 */
	public void goBack(View view) {
		this.finish();
	}

	/**
	 * When the sync button is pressed, sync with the splash screen.
	 *
	 * @param	view	the sync button.
	 */
	public void sync(View view) {
		// Get the information from the edit views and add to the intent
		EditText editText = (EditText) findViewById(R.id.edit_ip);
		String routerIPAddress = editText.getText().toString();
		editText = (EditText) findViewById(R.id.edit_router_username);
		String routerUsername = editText.getText().toString();
		editText = (EditText) findViewById(R.id.edit_password);
		String routerPassword = editText.getText().toString();

		JSONObject obj = new JSONObject();
		obj.put("ipaddress", routerIPAddress);
		obj.put("username", routerUsername);
		obj.put("password", routerPassword);

		StringWriter stringWriter = new StringWriter();
		try {
			obj.writeJSONString(stringWriter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String jsonText = stringWriter.toString();
		Log.i("ftp", FileIO.getAppRootPath() + "router.json");
		if (!FileIO.write("router.json", jsonText)) {
			Log.i("ftp", "not working");
		} else {
			Intent intent = new Intent(this, SyncSplashActivity.class);
			startActivity(intent);
		}
	}

}
