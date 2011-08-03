package au.edu.melbuni.boldapp;

import java.util.ResourceBundle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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
		
		ImageButton userImageButton = (ImageButton) findViewById(R.id.userImageButton);
		if (currentUser != null && currentUser.hasProfileImage()) {
			userImageButton.setImageDrawable(currentUser.getProfileImage());
		} else {
			userImageButton.setImageResource(R.drawable.unknown_user);
		}
	}

	public void addToMenu(int layout) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout menu = (LinearLayout) findViewById(R.id.menu);
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

//		SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
//		SharedPreferences.Editor editor = settings.edit();
//		editor.putString(PREFERENCES_USER_ID, currentUser.getIdentifierString());
//		editor.commit();
	}
}
