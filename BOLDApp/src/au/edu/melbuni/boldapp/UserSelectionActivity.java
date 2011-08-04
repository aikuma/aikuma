package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
		System.out.println("Loading users for listing");

		ListView usersListView = (ListView) findViewById(R.id.users);
		
		// TODO What to use here?
		//
		usersListView.setAdapter(Bundler.getUsers(this));
		usersListView.removeAllViews();

		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		usersListView.addView(layoutInflater.inflate(R.layout.new_user_list_item,
				usersListView, false));
		LinearLayout addNewUserLayout = (LinearLayout) findViewById(R.id.addNewUser);
		addNewUserLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Bundler.setCurrentUser(UserSelectionActivity.this, new User());
				startActivityForResult(new Intent(view.getContext(),
						EditUserActivity.class), 0);
			}
		});

		ArrayList<User> users = Bundler.getUsers(this);
		Iterator<User> usersIterator = users.iterator();
		while (usersIterator.hasNext()) {
			final User user = usersIterator.next();

			LinearLayout userLayout = new LinearLayout(this);
			LinearLayout.LayoutParams userLayoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			userLayout.setLayoutParams(userLayoutParams);

			ImageView userImage = new ImageView(this);
			LinearLayout.LayoutParams userImageParams = new LinearLayout.LayoutParams(
					60, 60);
			userImageParams.setMargins(10, 10, 10, 10);
			userImage.setLayoutParams(userImageParams);
			userImage.setImageDrawable(user.getProfileImage());

			TextView userText = new TextView(this);
			LinearLayout.LayoutParams userTextParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT);
			userText.setLayoutParams(userTextParams);
			userText.setTextAppearance(this,
					android.R.style.TextAppearance_Medium);
			userText.setGravity(Gravity.CENTER_VERTICAL);
			userText.setText(user.name);

			userLayout.addView(userImage);
			userLayout.addView(userText);

			userLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Bundler.setCurrentUser(UserSelectionActivity.this, user);
					finish();
				}
			});
			userLayout.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Bundler.setCurrentUser(UserSelectionActivity.this, user);
					startActivityForResult(new Intent(getApplicationContext(),
							EditUserActivity.class), 0);
					return false;
				}
			});

			usersListView.addView(userLayout);
		}

		super.onResume();
	}

	public void configureView(Bundle savedInstanceState) {
		super.configureView(savedInstanceState);

		setContent(R.layout.user_selection);
	};

	public void installBehavior(Bundle savedInstanceState) {

	}

}
