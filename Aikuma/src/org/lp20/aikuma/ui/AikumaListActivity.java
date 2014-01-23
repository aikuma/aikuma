/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.ActionBar;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public abstract class AikumaListActivity extends ListActivity {
	
	/**
	 * Called when the activity starts.
	 *
	 * @param	savedInstanceState	Non-null if the activity is to load up a
	 * saved state.
	 */
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

	/**
	 * Called to create the options menu using MenuBehaviour.
	 *
	 * @param	menu	The menu in question.
	 * @return	Always true.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return menuBehaviour.onCreateOptionsMenu(menu);
	}

	/**
	 * Called to create the options menu functionality using MenuBehaviour.
	 *
	 * @param	item	The menu item in question.
	 * @return	Always true.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (safeActivityTransition == true) {
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
	/**
	 * Indicates whether a safe activity transition should take place (warn
	 * about data loss.
	 */
	protected boolean safeActivityTransition;
	/**
	 * The string that should be used in the warning message if safe activity
	 * transitions are on.
	 */
	protected String safeActivityTransitionMessage;
}
