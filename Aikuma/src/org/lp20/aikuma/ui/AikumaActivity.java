/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public abstract class AikumaActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		menuBehaviour = new MenuBehaviour(this);

		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return menuBehaviour.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (safeActivityTransition) {
			return menuBehaviour.safeOnOptionsItemSelected(item,
					safeActivityTransitionMessage);
		} else {
			return menuBehaviour.onOptionsItemSelected(item);
		}
	}

	/**
	 * Provides default back functionality, unless the activity requires safe
	 * transitions, in which case it first notifies the user that they'll lose
	 * data.
	 */
	public void onBackPressed() {
		if (safeActivityTransition) {
			menuBehaviour.safeGoBack(safeActivityTransitionMessage);
		} else {
			this.finish();
		}
	}

	private MenuBehaviour menuBehaviour;
	protected boolean safeActivityTransition;
	protected String safeActivityTransitionMessage;
}
