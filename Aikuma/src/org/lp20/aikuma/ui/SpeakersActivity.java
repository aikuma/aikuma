package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.os.Bundle;
import org.lp20.aikuma.R;

public class SpeakersActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speakers);
	}

}
