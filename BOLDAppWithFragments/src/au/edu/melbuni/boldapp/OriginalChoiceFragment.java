package au.edu.melbuni.boldapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class OriginalChoiceFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    // Inflate the layout for this fragment
	    View view = inflater.inflate(R.layout.original_choice, container, false);
	    
        // Set button actions.
        //
        final Button fakeOriginalChoiceButton = (Button) view.findViewById(R.id.fakeOriginalChoiceButton);
        fakeOriginalChoiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	// Add the original choice activity on the stack
            	// with the instruction to move on to the record
            	// activity after having chosen an original.
                startActivityForResult(new Intent(view.getContext(), RecordActivity.class), 0);
            }
        });
        
        return view;
	}
}