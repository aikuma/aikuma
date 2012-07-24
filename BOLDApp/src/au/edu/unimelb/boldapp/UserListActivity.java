package au.edu.unimelb.boldapp;

import android.app.Activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
//import android.R;
import java.io.File;

import android.util.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class UserListActivity extends ListActivity {

	
	/**
	Loads the usernames of all the users into an array of strings.

	*/
	/*
	private String[] loadUserNames() {
		//Get an array of all the UUIDs from the "users" directory
		File dir = new File(FileIO.getAppRootPath() + "users");
		String[] userUuids = dir.list();

		//Get the usernames from the metadata files
		String[] userNames = new String[userUuids.length];
		JSONParser parser = new JSONParser();
		for (int i=0; i < userUuids.length; i++){
			String jsonStr = FileIO.read("users/" + userUuids[i] +
					"/metadata.json");
			try {
				Object obj = parser.parse(jsonStr);
				JSONObject jsonObj = (JSONObject) obj;
				userNames[i] = jsonObj.get("name").toString();
			} catch (Exception e) {
				Log.e("CaughtExceptions", e.getMessage());
			}
		}
		return userNames;
	}
	*/

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_selection);


		User[] users = FileIO.loadUsers();
		/*
		String[] userNames = new String[users.length];
		for(int i = 0; i < users.length; i++) {
			userNames[i] = users[i].getName();
		}
		*/
		//Use usernames as the array of text to be displayed
		/*
		ArrayAdapter<User> adapter = new ArrayAdapter<User>(this,
				android.R.layout.simple_list_item_1, users);
		setListAdapter(adapter);
		*/
		ArrayAdapter adapter = new UserArrayAdapter(this, users);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		User user = (User) getListAdapter().getItem(position);
		GlobalState.setCurrentUser(user);
		Toast.makeText(this, user.getName() + " selected", Toast.LENGTH_LONG).show();
		this.finish();
	}
}
