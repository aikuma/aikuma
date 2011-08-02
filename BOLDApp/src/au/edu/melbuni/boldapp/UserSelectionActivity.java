package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UserSelectionActivity extends BoldActivity {

	static final int TAKE_USER_PICTURE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		configureView(savedInstanceState);
		installBehavior(savedInstanceState);
	}

	public void configureView(Bundle savedInstanceState) {
		super.configureView(savedInstanceState);

		setContent(R.layout.user_selection);

		LinearLayout userLayout = (LinearLayout) findViewById(R.id.users);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		userLayout.addView(layoutInflater.inflate(R.layout.new_user_list_item,
				userLayout, false));

		if (savedInstanceState != null) {
			ArrayList<User> users = Bundler.getUsers(savedInstanceState);
			if (users != null) {
				Iterator<User> usersIterator = users.iterator();
				while (usersIterator.hasNext()) {
					User user = usersIterator.next();
					TextView userView = new TextView(getApplicationContext());
					userView.setText(user.name);
					
					// TODO Make selectable.
					//
					
					userLayout.addView(userView);
				}
			}
		}
	};

	public void installBehavior(Bundle savedInstanceState) {
		addNewUserButton();
	}

	public void addNewUserButton() {
		LinearLayout users = (LinearLayout) findViewById(R.id.users);
		users.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivityForResult(new Intent(view.getContext(),
						NewUserActivity.class), 0);
			}
		});
	}

}
