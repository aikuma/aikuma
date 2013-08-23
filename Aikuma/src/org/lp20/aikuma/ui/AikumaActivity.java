package org.lp20.aikuma.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public abstract class AikumaActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		menuBehaviour = new MenuBehaviour(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return menuBehaviour.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (safeActivityTransition == true) {
			return menuBehaviour.safeOnOptionsItemSelected(item);
		} else {
			return menuBehaviour.onOptionsItemSelected(item);
		}
	}

	private MenuBehaviour menuBehaviour;
	protected boolean safeActivityTransition;
}
