/*
	Copyright (C) 2013-2015, The Aikuma Project
	AUTHORS: Sangyeop Lee
*/
package org.lp20.aikuma.ui;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageButton;
import org.lp20.aikuma2.R;


/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class AddSpeakerActivity1 extends AikumaActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_speaker1);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//Lets method in superclass(AikumaAcitivity) know 
		//to ask user if they are willing to
		//discard new data on an activity transition via the menu.
		safeActivityTransition = false;
		safeActivityTransitionMessage = 
				"This will discard the new speaker's name.";
		
		ImageButton okButton = (ImageButton) findViewById(R.id.okButton1);
		okButton.setImageResource(R.drawable.ok_disabled_48);
		okButton.setEnabled(false);
		
		EditText textField = (EditText) findViewById(R.id.speakerName);
		textField.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				//Restrict to alphanumeric input
				int strLen = s.length();
				if(strLen != 0) {
					char c = s.charAt(strLen - 1);
					if(!(Character.isLetterOrDigit(c) || Character.isSpaceChar(c))) {
						s.delete(strLen - 1, strLen);
					}
				}
				
				// Button update
				if(s.length() > 0) {
					ImageButton okButton = 
							(ImageButton) findViewById(R.id.okButton1);
					okButton.setImageResource(R.drawable.ok_48);
					okButton.setEnabled(true);
					safeActivityTransition = true;
				} else {
					ImageButton okButton = 
							(ImageButton) findViewById(R.id.okButton1);
					okButton.setImageResource(R.drawable.ok_disabled_48);
					okButton.setEnabled(false);
					safeActivityTransition = false;
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}	
		});
		
		textField.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if((event.getAction() == KeyEvent.ACTION_DOWN && 
						(event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
					onOkButtonPressed(null);
					return true;
				}
				return false;
			}
			
		});
	}



	/**
	 * Called when the user is ready to confirm the name of the speaker.
	 *
	 * @param	view	The OK button.
	 */
	public void onOkButtonPressed(View view) {
		EditText textField = (EditText) findViewById(R.id.speakerName);
		EditText commentField = (EditText) findViewById(R.id.speakerComments);
		String name = textField.getText().toString();
		String comments = commentField.getText().toString();	// can be empty instead of null
		
		Intent intent = new Intent(this, AddSpeakerActivity4.class);
		intent.putExtra("origin", getIntent().getExtras().getInt("origin"));
		intent.putExtra("name", name);
		intent.putExtra("comments", comments);
		startActivity(intent);
	}
}