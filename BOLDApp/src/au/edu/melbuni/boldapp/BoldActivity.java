package au.edu.melbuni.boldapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BoldActivity extends Activity {

	public static final String PREFERENCES = "BOLDPreferences";
	public static final String PREFERENCES_USER_ID = "currentUserId";
	protected User currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		currentUser = Bundler.getCurrentUser(this);
		
		// Set current user.
		//
		ImageButton userImageButton = (ImageButton) findViewById(R.id.userImageButton);
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

	public void addToMenu(int layout) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout menu = (LinearLayout) findViewById(R.id.menu);
		LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		menu.setLayoutParams(menuParams);
		menu.addView(layoutInflater.inflate(layout, menu, false));
	}

	public void setContent(int layout) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		FrameLayout content = (FrameLayout) findViewById(R.id.content);
		content.addView(layoutInflater.inflate(layout, content, false));
	}

	public void configureView(Bundle savedInstanceState) {
		setContentView(R.layout.base);

		addToMenu(R.layout.user);
		addToMenu(R.layout.help);

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
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
