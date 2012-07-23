package au.edu.unimelb.boldapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import android.util.Log;

public class UserSelectionActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.user_selection);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public void goBack(View view){
		UserSelectionActivity.this.finish();
	}

	public void createUser(View view) {
		Intent intent = new Intent(this, CreateUserActivity.class);
		startActivity(intent);
	}
	public void changeUser(View view) {
		Intent intent = new Intent(this, UserListActivity.class);
		startActivity(intent);
	}
}
