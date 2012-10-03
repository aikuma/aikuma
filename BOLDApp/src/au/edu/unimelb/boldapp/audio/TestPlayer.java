package au.edu.unimelb.boldapp.audio;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import android.media.AudioTrack;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.util.Log;

import au.edu.unimelb.boldapp.FileIO;
import au.edu.unimelb.boldapp.ListenActivity;

/**
 * A player that allows individual audio files to be played.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class TestPlayer extends AudioTrack {

	private RandomAccessFile file;
	public TestPlayer other;
	//public final List<Integer> segments;
	private int segCount;
	private ListenActivity owner;


	public TestPlayer(UUID uuid, final List<Integer> segments) {
		super(AudioManager.STREAM_MUSIC, Constants.SAMPLE_RATE,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				getMinBufferSize(Constants.SAMPLE_RATE,
				AudioFormat.CHANNEL_OUT_MONO,
			AudioFormat.ENCODING_PCM_16BIT), MODE_STREAM);
		try {
			file = new RandomAccessFile(FileIO.getAppRootPath() +
					FileIO.getRecordingsPath() + uuid.toString() + ".wav",
					"r");
		} catch (FileNotFoundException e) {
			// If this ever happens, it's a programming bug.
			e.printStackTrace();
		}

		//this.segments = segments;

		segCount = 1;
		setNotificationMarkerPosition(segments.get(segCount++));
		Log.i("blorg", "first " + getNotificationMarkerPosition());
		new Thread(new PlaySegment()).start();

		setPlaybackPositionUpdateListener(new
				AudioTrack.OnPlaybackPositionUpdateListener(){

			@Override
			public void onMarkerReached(AudioTrack _woeva) {
				Log.i("blorg", " " + getNotificationMarkerPosition());
				if (segCount < segments.size()) {
					setNotificationMarkerPosition(segments.get(segCount++));
				} else {
					try {
						if (getNotificationMarkerPosition() ==
								((int)file.length()
								- Constants.WAV_HEADER_SIZE) / 2) {
							setNotificationMarkerPosition(0);
						} else {
							setNotificationMarkerPosition(((int)file.length() -
									Constants.WAV_HEADER_SIZE) / 2);
						}
					} catch (Exception e) {
					}
				}
				Log.i("blorg", "newmarker " + getNotificationMarkerPosition());
				pause();
				owner.swap();
			}

			@Override
			public void onPeriodicNotification(AudioTrack _woeva) {
				Log.i("blorg", " " + getPlaybackHeadPosition());
			}

		});

	}

	public void setOtherPlayer(TestPlayer other) {
		this.other = other;
	}

	public void setOwner(ListenActivity owner) {
		this.owner = owner;
	}

	@Override
	public void play() {
		Log.i("blorg", "head pos " + getPlaybackHeadPosition());
		Log.i("blorg", "play til " + getNotificationMarkerPosition());
		super.play();
		//Log.i("blorg", "head pos " + getPlaybackHeadPosition());
	}

	private class PlaySegment implements Runnable {

		public void run() {
			try {
				file.seek(Constants.WAV_HEADER_SIZE);
				byte[] segment = new
						byte[(int)file.length()-Constants.WAV_HEADER_SIZE];
				file.read(segment);
				write(segment, 0, segment.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
