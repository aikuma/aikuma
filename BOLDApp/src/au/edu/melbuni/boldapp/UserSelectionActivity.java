package au.edu.melbuni.boldapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class UserSelectionActivity extends BoldActivity {
	
	static final int TAKE_USER_PICTURE = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     	
        configureView(savedInstanceState);
     	installBehavior(savedInstanceState);
    }
    
    public void configureView(Bundle savedInstanceState) {
        super.configureView(savedInstanceState);
        
    	LinearLayout users = (LinearLayout) findViewById(R.id.users);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        users.addView(layoutInflater.inflate(R.layout.new_user_list_item, users, false));
        
     	setContent(R.layout.user_selection);
    };
    
    public void installBehavior(Bundle savedInstanceState) {
    	addNewUserButton();
    }
    
    public void addNewUserButton() {
    	LinearLayout users = (LinearLayout) findViewById(R.id.users);
        users.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		startActivityForResult(new Intent(view.getContext(), NewUserActivity.class), 0);
        	}
        });
    }
    
}
