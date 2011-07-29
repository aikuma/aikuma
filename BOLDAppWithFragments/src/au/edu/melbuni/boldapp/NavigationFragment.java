package au.edu.melbuni.boldapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class NavigationFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    // Inflate the layout for this fragment
	    View view = inflater.inflate(R.layout.navigation, container, false);
	    
        // Set button actions.
        //
        final Button button = (Button) view.findViewById(R.id.navigationButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	System.out.println("Back");
            	getActivity().finish();
            }
        });
	    
	    return view;
	}
}