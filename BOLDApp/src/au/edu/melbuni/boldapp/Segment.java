package au.edu.melbuni.boldapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class Segment {

	TimeLine timeLine = null;
	String identifier = null;

	Button view = null;
	private boolean selected;

	public Segment(final TimeLine timeLine, int id) {
		this.timeLine = timeLine;
		this.identifier = timeLine.identifier + new Integer(id).toString();
		this.view = new Button(timeLine.getContext());

		this.view.setWidth(100);

		this.view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				timeLine.setSelectedForRecording(Segment.this);
				timeLine.setSelectedForPlaying(Segment.this);
			}
		});
		this.view.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				new AlertDialog.Builder(Segment.this.view.getContext())
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setMessage("Delete?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Segment.this.timeLine.remove(Segment.this);
									}
								}).setNegativeButton("No", null).show();

				return false;
			}
		});
	}

	public void addTo(LinearLayout layout) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				layout.getLayoutParams());
		params.setMargins(0, 0, 5, 0);
		layout.addView(view, params);
	}

	public void removeFrom(LinearLayout layout) {
		layout.removeView(view);
	}

	public void startPlaying(Player player) {
		player.startPlaying(identifier);

		view.setBackgroundColor(Color.GREEN);
	}

	public void stopPlaying(Player player) {
		player.stopPlaying();
		resetColor();
	}

	public void startRecording(Recorder recorder) {
		// Animation scale = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.0f);
		// scale.setDuration(500);
		// scale.setRepeatCount(Animation.INFINITE);
		// view.startAnimation(scale);

		recorder.startRecording(identifier);

		view.setBackgroundColor(Color.RED);
	}

	public void stopRecording(Recorder recorder) {
		// view.clearAnimation();

		recorder.stopRecording();
		resetColor();
	}

	public void select() {
		this.selected = true;
		resetColor();
	}

	public void unselect() {
		this.selected = false;
		resetColor();
	}

	public void resetColor() {
		if (selected) {
			view.setBackgroundColor(Color.LTGRAY);
		} else {
			view.setBackgroundColor(Color.GRAY);
		}
	}

}
