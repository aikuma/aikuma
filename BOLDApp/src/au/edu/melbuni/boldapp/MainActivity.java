package au.edu.melbuni.boldapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends BoldActivity {
	
	@Override
	public void finish() {
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage("Really quit?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.super.finish();
            }
        })
        .setNegativeButton("No", null)
        .show();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        configureView();
     	installBehavior();
    }
    
    public void configureView() {
    	super.configureView();
    	
    	setContent(R.layout.main);
    };
    
    public void installBehavior() {
        final ImageButton recordButton = (ImageButton) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	startActivityForResult(new Intent(view.getContext(), RecordActivity.class), 0);
            }
        });
        final ImageButton listenButton = (ImageButton) findViewById(R.id.listenButton);
        listenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	startActivityForResult(new Intent(view.getContext(), OriginalChoiceActivity.class), 0);
            }
        });
    };
}