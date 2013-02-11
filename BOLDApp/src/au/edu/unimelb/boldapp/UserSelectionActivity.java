package au.edu.unimelb.aikuma;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * Activity that allows for the changing of currentUser, and links to the
 * CreateUserActivity (with a back button)
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class UserSelectionActivity extends InitialUserSelectionActivity {
	/**
	 * Called when the activity is initially created.
	 *
	 * @param	savedInstanceState	Data the activity most recently supplied to
	 * onSaveInstanceState(Bundle).
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_selection);
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
		this.finish();
	}

	/**
	 * When the on screen back button is pressed.
	 *
	 * @param	view	The button that was pressed.
	 */
	public void goBack(View view){
		this.finish();
	}
}
