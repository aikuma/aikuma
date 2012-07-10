package au.edu.melbuni.boldapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.Sounds;
import au.edu.melbuni.boldapp.Synchronizer;
import au.edu.melbuni.boldapp.models.User;

public class MainActivity extends BoldActivity {

	boolean listening = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Sounds.prepare(this.getApplicationContext());
		Sounds.beep();
		Sounds.beepbeep();
		
		Bundler.load(this);
		// Users users = Bundler.getUsers(this);
		// if (users.isEmpty()) {
		// throw new RuntimeException(users.toString());
		// }

		configureView(savedInstanceState);
		installBehavior(savedInstanceState);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Bundler.save(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Bundler.save(this);
	}

	@Override
	protected void onResume() {
		checkButtonsEnabled();
		super.onResume();
	}

	@Override
	public void finish() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage("Really quit?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Bundler.save(MainActivity.this);
								MainActivity.super.finish();
							}
						}).setNegativeButton("No", null).show();
	}

	@Override
	public void configureView(Bundle savedInstanceState) {
		super.configureView(savedInstanceState);

		addToMenu(R.layout.configuration);
		setContent(R.layout.main);

		setExitNavigation();
	};

	public void installBehavior(Bundle savedInstanceState) {
		final ImageButton recordButton = (ImageButton) findViewById(R.id.recordButton);
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivityForResult(new Intent(view.getContext(),
						RecordActivity.class), 0);
			}
		});
		final ImageButton listenButton = (ImageButton) findViewById(R.id.listenButton);
		listenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivityForResult(new Intent(view.getContext(),
						OriginalSelectionActivity.class), 0);
			}
		});
		final ImageButton respeakButton = (ImageButton) findViewById(R.id.respeakButton);
		respeakButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				startActivityForResult(new Intent(view.getContext(),
						RespeakSelectionActivity.class), 0);
				// TranscribeActivity.class), 0);
				return false;
			}
		});
		final ImageButton translateButton = (ImageButton) findViewById(R.id.translateButton);
		translateButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				startActivityForResult(new Intent(view.getContext(),
						OldRespeakActivity.class), 0); // TODO Replace with correct activity.
				// TranscribeActivity.class), 0);
				return false;
			}
		});

		checkButtonsEnabled();

		final ImageButton configurationButton = (ImageButton) findViewById(R.id.configurationButton);
		if (configurationButton != null) {
			configurationButton
					.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							try {
								Synchronizer synchronizer = Synchronizer
										.getDefault();
								int[] usersAndTimelinesSynced = synchronizer
										.synchronize(MainActivity.this);

								// Reload all.
								//
								Bundler.load(MainActivity.this);

								Toast toast = Toast.makeText(MainActivity.this,
										"Synchronized "
												+ usersAndTimelinesSynced[0]
												+ " users / "
												+ usersAndTimelinesSynced[1]
												+ " timelines.", 2000);
								toast.setGravity(Gravity.TOP, -30, 50);
								toast.show();
							} catch (RuntimeException e) {
								System.err.println(e.getMessage());

								Toast toast = Toast.makeText(MainActivity.this,
										"Go closer, plis.", 2000);
								toast.setGravity(Gravity.TOP, -30, 50);
								toast.show();
							}
							return false;
						}
					});
		}
	}

	private void checkButtonsEnabled() {
		final TextView startText = (TextView) findViewById(R.id.startText);
		final ImageButton recordButton = (ImageButton) findViewById(R.id.recordButton);
		final ImageButton listenButton = (ImageButton) findViewById(R.id.listenButton);

		User user = Bundler.getCurrentUser(this);
		int visible = user.hasGivenConsent() ? View.VISIBLE : View.INVISIBLE;
		int gone = user.hasGivenConsent() ? View.GONE : View.VISIBLE;

		startText.setVisibility(gone);
		recordButton.setVisibility(visible);
		listenButton.setVisibility(visible);
	};
}