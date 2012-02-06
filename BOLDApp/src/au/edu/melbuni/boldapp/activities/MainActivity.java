package au.edu.melbuni.boldapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.behaviors.TapAndHoldListen;
import au.edu.melbuni.boldapp.behaviors.TapAndHoldRecord;
import au.edu.melbuni.boldapp.behaviors.TapAndReleaseListen;
import au.edu.melbuni.boldapp.behaviors.TapAndReleaseRecord;
import au.edu.melbuni.boldapp.models.User;

public class MainActivity extends BoldActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		setContent(R.layout.main);
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
		
		checkButtonsEnabled();

		// TODO Remove.
		//
		final ImageButton configurationButton = (ImageButton) findViewById(R.id.configurationButton);
		if (configurationButton != null) {
			configurationButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(v.getContext())
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setMessage("Switch Recording style")
							.setPositiveButton("Press/Hold",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											RecordActivity
													.setBehavior(new TapAndHoldRecord());
										}
									})
							.setNegativeButton("Tap/Release",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											RecordActivity
													.setBehavior(new TapAndReleaseRecord());
										}
									}).show();
				}
			});
			configurationButton
					.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							new AlertDialog.Builder(v.getContext())
									.setIcon(android.R.drawable.ic_dialog_alert)
									.setMessage("Switch Listening style")
									.setPositiveButton(
											"Press/Hold",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													ListenActivity
															.setBehavior(new TapAndHoldListen());
												}
											})
									.setNegativeButton(
											"Tap/Release",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													ListenActivity
															.setBehavior(new TapAndReleaseListen());
												}
											}).show();
							return false;
						}
					});
		}
	}

	private void checkButtonsEnabled() {
		final ImageButton recordButton = (ImageButton) findViewById(R.id.recordButton);
		final ImageButton listenButton = (ImageButton) findViewById(R.id.listenButton);
		
		User user = Bundler.getCurrentUser(this);
		boolean enableButtons = user.hasGivenConsent();
		
		recordButton.setEnabled(enableButtons);
		listenButton.setEnabled(enableButtons);
	};
}