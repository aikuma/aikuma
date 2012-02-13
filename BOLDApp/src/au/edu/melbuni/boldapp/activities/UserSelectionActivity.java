package au.edu.melbuni.boldapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Sounder;
import au.edu.melbuni.boldapp.adapters.UserItemAdapter;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.models.User;

public class UserSelectionActivity extends BoldActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		configureView(savedInstanceState);
		installBehavior(savedInstanceState);
	}

	@Override
	protected void onResume() {
		ListView usersListView = (ListView) findViewById(R.id.users);
		((UserItemAdapter) usersListView.getAdapter()).notifyDataSetChanged();

		super.onResume();
	}

	@Override
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
				Bundler.setCurrentUser(UserSelectionActivity.this,
						User.newUnconsentedUser());
				startActivityForResult(new Intent(view.getContext(),
						InformedConsentConfirmActivity.class), 0);
			}
		});

		ListView usersListView = (ListView) findViewById(R.id.users);
		usersListView.setAdapter(new UserItemAdapter(this, Bundler
				.getUsers(this)));

		// Selects the user on a short click.
		//
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

		// Plays the user's audio on long click.
		//
		usersListView
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						User selectedUser = Bundler.getUsers(
								UserSelectionActivity.this).get(position);
						selectedUser.startPlaying(new Player(),
								new OnCompletionListener() {
									@Override
									public void onCompletion(Sounder sounder) {
										((Player) sounder).stopPlaying();
									}
								});
						return true;
					}
				});
	}

}
