package org.lp20.aikuma.audio;

import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.Iterator;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.Segments.Segment;
import org.lp20.aikuma.model.Transcript;
import org.lp20.aikuma.R;

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

		this.recording = recording;
		this.activity = activity;

		prepare();
	}

	private void prepare() {
		transcript = new Transcript(recording);

		updateTranscriptUi(getCurrentSample());

		/*
		if (transcript.getSegmentList().size() > 0) {
			updateNotificationMarkerPosition(transcript.getSegmentList().get(0));
		}
		*/

		/*
		final Iterator<Segment> segmentIterator = 
				transcript.getSegmentIterator();
		if (segmentIterator.hasNext()) {
			segment = segmentIterator.next();
			setNotificationMarkerPositionSample(segment.getStartSample());
			Log.i("transcript", "set notification marker position to sample: "
					+ segment.getStartSample());
		}
		*/
		OnMarkerReachedListener onTranscriptMarkerReachedListener =
				new OnMarkerReachedListener() {
			public void onMarkerReached(MarkedPlayer p) {
				activity.runOnUiThread(new Runnable() {
					public void run() {
						updateTranscriptUi(getCurrentSample());
						//Log.i("transcript", "getCurrentSample: " + sample);
						/*
						if (segmentIterator.hasNext()) {
							TranscriptPlayer.segment = segmentIterator.next();
							TranscriptPlayer.this.setNotificationMarkerPositionSample(
									TranscriptPlayer.segment.getEndSample());
						}
						*/
					}
				});
			}
		};
		setOnMarkerReachedListener(onTranscriptMarkerReachedListener);
	}

	/**
	 * Sets the notification marker position to be the start of the segment
	 * following the supplied segment.
	 */
	private void updateNotificationMarkerPosition(Segment segment) {
		for (Segment seg : transcript.getSegmentList()) {
			Log.i("transcript", "\tsegment list: " + seg);
		}
		int segIndex = transcript.getSegmentList().indexOf(segment);
		segIndex += 1;
		if (segIndex < transcript.getSegmentList().size()) {
			setNotificationMarkerPositionSample(
					transcript.getSegmentList().get(segIndex).getStartSample());
		}
	}

	private void updateTranscriptUi(long sample) {
		Segment segment = transcript.getSegmentOfSample(sample);
		Log.i("transcript", "get segment of sample: " + segment);
		if (segment != null) {
			// If the sample has a corresponding segment, update accordingly.
			updateNotificationMarkerPosition(segment);
			updateTranscriptUi(segment);
		} else {
			// Otherwise just unset the notification marker position.
			unsetNotificationMarkerPosition();
			emptyTranscriptUi();
		}
	}

	// Update the UI so that the transcript segment is empty.
	private void emptyTranscriptUi() {
		transcriptView = (TextView)
			activity.findViewById(R.id.transcriptView);
		transcriptView.setText("");
	}

	// Update the UI such that the transcript reflects the current segment.
	private void updateTranscriptUi(Segment segment) {
		transcriptView = (TextView)
			activity.findViewById(R.id.transcriptView);
		transcriptView.setText(transcript.getTranscriptSegmentText(segment));
	}

	@Override
	public void seekToMsec(int msec) {
		super.seekToMsec(msec);

		//Find the transcript segment that corresponds to this point in time
		long sample = msecToSample(msec);
		Log.i("transcript", "seekToMsec: " + msec + "sample: " + sample +
		"getCurrentSample: " + getCurrentSample());

		//Update the UI to reflect the current transcript.
		updateTranscriptUi(sample);
	}

	/**
	 * Resets the player with the initial parameters
	 */
	public void reset() {
		seekToSample(0);
		prepare();
	}

	/**
	 * Listener to adjust the transcript view when a transcript marker gets reached.
	 */
	private class TranscriptMarkerReachedListener
			extends MarkedPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedPlayer p) {
			transcriptView.setText("different");
		}
	}

	private static Segment segment;
	private static TextView transcriptView;
	private Transcript transcript;

	//Stored so that the TranscriptPlayer can be reset with the original
	//parameters.
	private Recording recording;
	private Activity activity;
	private Iterator<Segment> iterator;
}
