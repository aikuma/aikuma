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

public class TranscriptPlayer extends MarkedPlayer {

	public TranscriptPlayer(Recording recording, final Activity activity)
			throws IOException {
		super(recording, true);
		transcript = new Transcript(recording);
		final Iterator<Segment> segmentIterator = 
				transcript.getSegmentIterator();
		if (segmentIterator.hasNext()) {
			segment = segmentIterator.next();
			setNotificationMarkerPositionSample(segment.getStartSample());
		}
		OnMarkerReachedListener onTranscriptMarkerReachedListener =
				new OnMarkerReachedListener() {
			public void onMarkerReached(MarkedPlayer p) {
				activity.runOnUiThread(new Runnable() {
					public void run() {
						transcriptView = (TextView)
							activity.findViewById(R.id.transcriptView);
						transcriptView.setText(transcript.getTranscriptSegment(TranscriptPlayer.segment));
						if (segmentIterator.hasNext()) {
							TranscriptPlayer.segment = segmentIterator.next();
							TranscriptPlayer.this.setNotificationMarkerPositionSample(
									TranscriptPlayer.segment.getEndSample());
						}
					}
				});
			}
		};
		setOnMarkerReachedListener(onTranscriptMarkerReachedListener);
	}

	private class TranscriptMarkerReachedListener
			extends MarkedPlayer.OnMarkerReachedListener {
		public void onMarkerReached(MarkedPlayer p) {
			transcriptView.setText("different");
		}
	}

/*
	public void play() {
		markedPlayer.play();
	}

	public void pause() {
		markedPlayer.pause();
	}

	public boolean isPlaying() {
		return markedPlayer.isPlaying();
	}

	public int getCurrentMsec() {
		return markedPlayer.getCurrentMsec();
	}

	public long getCurrentSample() {
		return markedPlayer.getCurrentSample();
	}

	public int getDurationMsec() {
		return markedPlayer.getDurationMsec();
	}

	public void seekToMsec(int msec) {
		markedPlayer.seekToMsec(msec);
	}

	public void seekToSample(long sample) {
		markedPlayer.seekToSample(sample);
	}

	public void setAudioStreamType(int type) {
		markedPlayer.setAudioStreamType(type);
	}

	public void release() {
		markedPlayer.release();
	}

	private MarkedPlayer markedPlayer;
	*/
	private static Segment segment;
	private static TextView transcriptView;
	private Transcript transcript;
}
