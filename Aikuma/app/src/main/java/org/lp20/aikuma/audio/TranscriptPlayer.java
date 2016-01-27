package org.lp20.aikuma.audio;

import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.Iterator;

import org.lp20.aikuma.audio.Player.OnCompletionListener;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Segments.Segment;
import org.lp20.aikuma.model.TempTranscript;
import org.lp20.aikuma2.R;

import android.util.Log;

/**
 * A player that plays a recording and additionally presents an existing
 * transctiption in an appropriate activity.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com
 */
public class TranscriptPlayer extends MarkedPlayer {

	/**
	 * Constructor
	 *
	 * @param	recording	The recording to be played
	 * @param	activity	The activity to modify as the transcriptions
	 * change.
	 * @throws	IOException	If there is an issue reading transcriptions or the
	 * recording
	 */
	public TranscriptPlayer(Recording recording, final Activity activity)
			throws IOException {
		super(recording, true);

		this.activity = activity;

		transcript = recording.getTranscript();

		if(transcript != null) {
			updateTranscriptStatus(getCurrentSample());

			OnMarkerReachedListener onTranscriptMarkerReachedListener =
					new OnMarkerReachedListener() {
				public void onMarkerReached(MarkedPlayer p) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							updateTranscriptStatus(getCurrentSample());
						}
					});
				}
			};

			setOnMarkerReachedListener(onTranscriptMarkerReachedListener);
		}
		
	}

	@Override
	public void play() {
		super.play();
		if(transcript != null) {
			updateTranscriptStatus(getCurrentSample());
		}
	}

	// Updates the transcript Ui and prepares the notification marker position.
	private void updateTranscriptStatus(long sample) {
		Segment segment = transcript.getSegmentOfSample(sample);
		updateTranscriptUi(segment);
		setNotificationMarkerPosition(segment);
	}

	// Update the UI such that the transcript reflects the current sample.
	private void updateTranscriptUi(long sample) {
		Segment segment = transcript.getSegmentOfSample(sample);
		updateTranscriptUi(segment);
	}

	// Update the UI such that the transcript reflects the current segment.
	private void updateTranscriptUi(Segment segment) {
		transcriptView = (TextView)
			activity.findViewById(R.id.transcriptView);
		transcriptView2 = (TextView)
			activity.findViewById(R.id.transcriptView2);
		if (segment != null) {
			transcriptView.setText(
					transcript.getTranscriptPair(segment).transcript);
			transcriptView2.setText(
					transcript.getTranscriptPair(segment).translation);
		} else {
			transcriptView.setText("");
		}
	}

	@Override
	public void seekToMsec(int msec) {
		super.seekToMsec(msec);

		//Find the transcript segment that corresponds to this point in time
		long sample = msecToSample(msec);

		//Update the UI to reflect the current transcript.
		updateTranscriptStatus(sample);
	}

	
	/*
	/**
	 * Listener to adjust the transcript view when a transcript marker gets reached.
	private class TranscriptMarkerReachedListener
			extends MarkedPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedPlayer p) {
			transcriptView.setText("different");
		}
	}
	*/

	private static Segment segment;
	private static TextView transcriptView;
	private static TextView transcriptView2;
	private TempTranscript transcript;
	private Activity activity;
}
