package au.edu.melbuni.boldapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import au.edu.melbuni.boldapp.models.Users;

public class MainActivity extends BoldActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundler.load(this);
		Users users = Bundler.getUsers(this);
		if (users.isEmpty()) {
			throw new RuntimeException(users.toString());
		}

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
	};
}