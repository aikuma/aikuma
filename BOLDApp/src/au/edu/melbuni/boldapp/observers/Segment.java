package au.edu.melbuni.boldapp.observers;

import java.util.Observable;
import java.util.Observer;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.View;

public class Segment implements Observer {
	
	private View view;

	public Segment(View view) {
		if (view == null) {
			throw new IllegalArgumentException("View for the segment observer cannot be null.");
		}
		
		this.view = view;
	}

	@Override
	public void update(Observable observable, Object data) {
		au.edu.melbuni.boldapp.models.Segment segment = (au.edu.melbuni.boldapp.models.Segment) observable;
		
		if (segment.isRecording()) {
			view.getBackground().setColorFilter(Color.RED, Mode.MULTIPLY);
			return;
		}
		if (segment.isPlaying()) {
			view.getBackground().setColorFilter(Color.GREEN, Mode.MULTIPLY);
			return;
		}
		if (segment.isSelected()) {
			view.getBackground().clearColorFilter();
		} else {
			view.getBackground().setColorFilter(Color.GRAY, Mode.MULTIPLY);
		}
	}
}
