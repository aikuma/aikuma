package au.edu.melbuni.boldapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import au.edu.melbuni.boldapp.Bundler;
import au.edu.melbuni.boldapp.R;

public class InformedConsentNameActivity extends BoldActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
    }
    
    @Override
	public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);
        
     	setContent(R.layout.informed_consent_name);
    };
    
    public void installBehavior(Bundle savedInstanceState) {

    	final EditText userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        userNameEditText.addTextChangedListener(new TextWatcher(){
            @Override
			public void afterTextChanged(Editable s) {
            	Bundler.getCurrentUser(InformedConsentNameActivity.this).name = userNameEditText.getText().toString();
            }
            @Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
			public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        
	    final Button nextButton = (Button) findViewById(R.id.nextButton);
	    nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO Save something here instead of up there.
				setResult(RESULT_OK);
				finish();
			}
        });
    }
    
    protected void setUserTextFromCurrentUser() {
    	final EditText userNameEditText = (EditText) findViewById(R.id.newUserNameEditText);
    	userNameEditText.setText(Bundler.getCurrentUser(this).name);
    }
	
}
