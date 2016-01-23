package org.lp20.aikuma.ui;

import org.lp20.aikuma.util.AikumaSettings;
import org.lp20.aikuma2.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * An activity for User agreement for sharing
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class ConsentActivity extends AikumaActivity {

	private SharedPreferences preferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consent);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		setupPublicShareConsentCheckbox();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	private void setupPublicShareConsentCheckbox() {
		boolean isPublicShareAgreed = preferences.getBoolean(
				AikumaSettings.PUBLIC_SHARE_CONSENT_KEY, false);
		CheckBox consentCheckbox = (CheckBox) 
				findViewById(R.id.consentCheckbox);
		if(isPublicShareAgreed) {
			consentCheckbox.setChecked(isPublicShareAgreed);
			consentCheckbox.setEnabled(false);
		}
	}
	
	/**
	 * Callback function when an user agrees about the public share terms
	 * 
	 * @param checkBox	Agreement checkbox
	 */
	public void onConsentCheckbox(View checkBox) {
		boolean isChecked = ((CheckBox) checkBox).isChecked();
		
		if(isChecked) {
			AikumaSettings.isPublicShareEnabled = true;
			Editor prefsEditor = preferences.edit();
			prefsEditor.putBoolean(AikumaSettings.PUBLIC_SHARE_CONSENT_KEY, isChecked);
			prefsEditor.commit();
		}
		
		setupPublicShareConsentCheckbox();
	}

}
