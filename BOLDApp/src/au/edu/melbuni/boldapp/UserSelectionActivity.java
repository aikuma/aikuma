package au.edu.melbuni.boldapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class UserSelectionActivity extends BoldActivity {

	static final int TAKE_USER_PICTURE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		configureView(savedInstanceState);
		installBehavior(savedInstanceState);
	}

	@Override
	protected void onResume() {
		ListView usersListView = (ListView) findViewById(R.id.users);
		usersListView.setAdapter(new UserItemAdapter(this, Bundler.getUsers(this)));

		usersListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Bundler.setCurrentUser(UserSelectionActivity.this,
								Bundler.getUsers(UserSelectionActivity.this)
										.get(position));
						finish();
					}
				});

		usersListView
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						Bundler.setCurrentUser(UserSelectionActivity.this,
								Bundler.getUsers(UserSelectionActivity.this)
										.get(position));
						startActivityForResult(
								new Intent(getApplicationContext(),
										EditUserActivity.class), 0);
						return false;
					}
				});

		super.onResume();
	}

	public void configureView(Bundle savedInstanceState) {
		super.configureView(savedInstanceState);

		setContent(R.layout.user_selection);
	};

	public void installBehavior(Bundle savedInstanceState) {
		// New User
		//
		LinearLayout addNewUserLayout = (LinearLayout) findViewById(R.id.addNewUser);
		addNewUserLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Bundler.setCurrentUser(UserSelectionActivity.this, new User());
				startActivityForResult(new Intent(view.getContext(),
						EditUserActivity.class), 0);
			}
		});
	}

}
