package au.edu.melbuni.boldapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OriginalChoiceActivity extends BoldActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
     	configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
    }
    
    public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);

		setContent(R.layout.original_choice);
    };
    
    public void installBehavior(Bundle savedInstanceState) {
        final Button originalChoiceButton = (Button) findViewById(R.id.fakeOriginalChoiceButton);
        originalChoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View view) {
            	startActivityForResult(new Intent(view.getContext(), ListenActivity.class), 0);
            }
        });
    };
}
