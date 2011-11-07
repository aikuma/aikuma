package au.edu.melbuni.boldapp.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import au.edu.melbuni.boldapp.R;

public class InformedConsent1InfoActivity extends BoldActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
    }
    
    @Override
	public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);
        
     	setContent(R.layout.informed_consent_1_info);
    };
    
    public void installBehavior(Bundle savedInstanceState) {
    	
        final View nextButton = (View) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Save something.
				setResult(RESULT_OK);
				finish();
			}
		});
        
    }
	
}
