package au.edu.melbuni.boldapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;
import au.edu.melbuni.boldapp.models.User;

public class BoldActivity extends Activity {

	public static final String PREFERENCES = "BOLDPreferences";
	public static final String PREFERENCES_USER_ID = "currentUserId";

	AlertDialog helpDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onResume() {
		super.onResume();

		User currentUser = Bundler.getCurrentUser(this);

		// Set current user.
		//
		ImageButton userImageButton = (ImageButton) findViewById(R.id.userImageButton);
		if (userImageButton != null) {
			if (currentUser != null && currentUser.hasProfileImage()) {
				userImageButton.setImageDrawable(currentUser.getProfileImage());
			} else {
				userImageButton.setImageResource(R.drawable.unknown_user);
			}
			TextView userTextView = (TextView) findViewById(R.id.userText);
			if (currentUser != null && currentUser.name != null) {
				userTextView.setText(currentUser.name);
			} else {
				userTextView.setText("?");
			}
		}
	}

	// Installs the help and enables the help button.
	//
	public void installHelp(final int helpLayout) {
		// final View helpButton = findViewById(R.id.helpButton);
		//
		// helpButton.setEnabled(true);
		//
		// helpButton.setOnTouchListener(new View.OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent motionEvent) {
		// if (helpDialog == null) {
		// LayoutInflater layoutInflater = (LayoutInflater)
		// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// View newView = layoutInflater.inflate(helpLayout, null, false);
		//
		// helpDialog = new AlertDialog.Builder(v.getContext()).
		// setView(newView).
		// create();
		// }
		//
		// WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		// params.copyFrom(helpDialog.getWindow().getAttributes());
		//
		// params.dimAmount = 0.8f;
		// params.gravity = Gravity.RIGHT;
		//
		// DisplayMetrics metrics = getResources().getDisplayMetrics();
		// int width = metrics.widthPixels;
		// int height = metrics.heightPixels;
		//
		// params.width = 4 * width / 5;
		// params.height = height;
		//
		// helpDialog.show();
		//
		// helpDialog.getWindow().setAttributes(params);
		//
		// return false;
		// }
		// });
		// helpButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// helpDialog.hide();
		// }
		// });
	}

	public void addToMenu(int layout) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout menu = (LinearLayout) findViewById(R.id.menu);
		LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		menu.setLayoutParams(menuParams);
		menu.addView(layoutInflater.inflate(layout, menu, false));
	}

	public void setContent(int layout) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View newView = layoutInflater.inflate(layout, null, false);
		setContent(newView);
	}

	public void setContent(View newView) {
		FrameLayout content = (FrameLayout) findViewById(R.id.content);
		content.addView(newView);
	}

	public void configureView(Bundle savedInstanceState) {
		setContentView(R.layout.base);

		addToMenu(R.layout.user);
		// addToMenu(R.layout.help);
		// addToMenu(R.layout.configuration);
		addToMenu(R.layout.navigation);

		// Menu behavior.
		//
		ImageButton userImageButton = (ImageButton) findViewById(R.id.userImageButton);
		userImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivityForResult(new Intent(view.getContext(),
						UserSelectionActivity.class), 0);
			}
		});

		// Menu behavior.
		//
		ImageButton navigationButton = (ImageButton) findViewById(R.id.navigationButton);
		navigationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				BoldActivity.this.finish();
			}
		});
	}
}
