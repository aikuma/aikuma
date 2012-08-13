package au.edu.unimelb.boldapp;

import android.app.ListActivity;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.util.Log;

/**
 * Activity that allows for the changing of currentUser, and links to the
 * creation of new users.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class UserSelectionActivity extends ListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_selection);

		FileIO.loadUsers();
		ArrayAdapter adapter = new UserArrayAdapter(this,
				GlobalState.getUsers());
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		User user = (User) getListAdapter().getItem(position);
		GlobalState.setCurrentUser(user);
		Toast.makeText(this,
				user.getName() + " selected", Toast.LENGTH_LONG).show();
		UserSelectionActivity.this.finish();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public void goBack(View view){
		UserSelectionActivity.this.finish();
	}

	public void createUser(View view) {
		Intent intent = new Intent(this, CreateUserActivity.class);
		startActivity(intent);
		this.finish();
	}
}
