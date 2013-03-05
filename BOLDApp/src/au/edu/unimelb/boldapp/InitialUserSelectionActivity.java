package au.edu.unimelb.aikuma;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.regex.Pattern;

import au.edu.unimelb.aikuma.sync.SyncForActivity;

/**
 * Activity that allows for the changing of currentUser, and links to the
 * CreateUserActivity.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class InitialUserSelectionActivity extends ListActivity {
	/**
	 * Called when the activity is initially created.
	 *
	 * @param	savedInstanceState	Data the activity most recently supplied to
	 * onSaveInstanceState(Bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.initial_user_selection);
		TextView link = (TextView) findViewById(R.id.Url);
		setAsLink(link, link.getText().toString());
	}

	/**
	 * Called when the activity starts.
	 */
	@Override
	public void onStart() {
		super.onStart();

		// Load users into file and set the list array adapter. Doing this in
		// this method ensures users that have just been created appear when
		// the current user returns from the CreateUserActivity
		GlobalState.loadUsers();
		ArrayAdapter adapter = new UserArrayAdapter(this,
				GlobalState.getUsers().toArray(new User[0]));
		setListAdapter(adapter);

		GlobalState.loadLangCodeMap(getResources());
	}

	private void setAsLink(TextView view, String url){
		Pattern pattern = Pattern.compile(url);
		Linkify.addLinks(view, pattern, "http://");
		view.setText(Html.fromHtml(
				"<a href='http://"+url+"'>"+url+"</a>"));
	}

	/**
	 * When the list item is clicked.
	 *
	 * @param	l		the listview
	 * @param	v		the view that was clicked
	 * @param	positon	position in the list and array
	 * @param 	id		id
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		User user = (User) getListAdapter().getItem(position);
		GlobalState.setCurrentUser(user);
		Toast.makeText(this,
				user.getName() + " selected", Toast.LENGTH_LONG).show();
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	/**
	 * Starts the CreateUserActivity
	 *
	 * @param	view	The button that was clicked.
	 */
	public void createUser(View view) {
		Intent intent = new Intent(this, CreateUserActivity.class);
		startActivity(intent);
	}

	public void syncActivity(View view) {
		//SyncForActivity.sync(this);
		//Intent intent = new Intent(this, SyncActivity.class);
		//startActivity(intent);

		Server server = new Server("us1.hostedftp.com",
				"stevenbird1@gmail.com", "DMD819");
		Intent intent = new Intent(this, SyncSplashActivity.class);
		intent.putExtra("ServerInfo", server);
		startActivity(intent);

	}
}
