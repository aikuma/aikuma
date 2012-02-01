package au.edu.melbuni.boldapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import au.edu.melbuni.boldapp.BoldApplication;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Sounder;
import au.edu.melbuni.boldapp.Synchronizer;
import au.edu.melbuni.boldapp.adapters.UserItemAdapter;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.models.User;
import au.edu.melbuni.boldapp.models.Users;

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
		
		final ImageButton configurationButton = (ImageButton) findViewById(R.id.configurationButton);
		if (configurationButton != null) {
			configurationButton
					.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							// WifiManager wifi = (WifiManager)
							// getSystemService( Context.WIFI_SERVICE );
							// MulticastLock lock =
							// wifi.createMulticastLock("bold_multicast_lock");
							// lock.setReferenceCounted(true);
							// lock.acquire();
							//
							// wifi.startScan(); // CHANGE_WIFI_STATE

							// User user =
							// Bundler.getCurrentUser(UserSelectionActivity.this);

							// HttpResponse response = new
							// HTTPClient("http://128.250.22.12:4567").post(user);

							Users users = Bundler
									.getUsers(UserSelectionActivity.this);
							new Synchronizer("192.168.0.199")
									.synchronize(users); // 128.250.22.12 / 128.250.29.213

							Toast toast = Toast.makeText(
									UserSelectionActivity.this, users.getIds()
											.toString(), 2000);
							toast.setGravity(Gravity.TOP, -30, 50);
							toast.show();

							// Save all the data now that we are synced.
							//
							((BoldApplication) getApplication()).save();

							// if (response != null) {
							// new AlertDialog.Builder(v.getContext())
							// .setIcon(android.R.drawable.ic_dialog_alert)
							// .setMessage(response.toString())
							// .setPositiveButton("OK",
							// new DialogInterface.OnClickListener() {
							// @Override
							// public void onClick(DialogInterface dialog,
							// int which) {
							//
							// }
							// }).setNegativeButton("Cancel", new
							// DialogInterface.OnClickListener() {
							// @Override
							// public void onClick(DialogInterface dialog, int
							// which) {
							//
							// }
							// }).show();
							// }

							// List<ScanResult> scanResults =
							// wifi.getScanResults();
							// for (ScanResult scanResult : scanResults) {
							// new AlertDialog.Builder(v.getContext())
							// .setIcon(android.R.drawable.ic_dialog_alert)
							// .setMessage(scanResult.toString())
							// .setPositiveButton("OK",
							// new DialogInterface.OnClickListener() {
							// @Override
							// public void onClick(DialogInterface dialog,
							// int which) {
							//
							// }
							// }).setNegativeButton("Cancel", new
							// DialogInterface.OnClickListener() {
							// @Override
							// public void onClick(DialogInterface dialog, int
							// which) {
							//
							// }
							// }).show();
							// }
							//
							// lock.release();

							return true;
						}
					});
		}

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
