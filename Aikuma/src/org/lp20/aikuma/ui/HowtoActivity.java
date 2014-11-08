/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma2.R;
import android.net.Uri;
import android.content.Intent;

/**
 * An activity that gives basic instructions on how to use Aikuma.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class HowtoActivity extends AikumaActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.howto);
	}

	/**
	 * Opens the howto from lp20.org in a browser.
	 *
	 * @param	_button	The button pressed
	 */
	public void openHelpInBrowser(View _button) {
		Uri uri = Uri.parse("http://lp20.org/aikuma/howto.html");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
}
