package au.edu.unimelb.boldapp.audio;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.UUID;

import android.media.AudioTrack;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.util.Log;

import au.edu.unimelb.boldapp.FileIO;

/**
 * A player that allows individual audio files to be played.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class TestPlayer extends AudioTrack implements Runnable{

	private RandomAccessFile file;
	private long start;
	private long end;

	public TestPlayer(UUID uuid) {
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
		play();
	}

	public void test() {
		new Thread(this).start();
	}

	public void run() {
		/*
		try {
			file.seek(WAV_HEADER_SIZE);
			byte[] buffer = new byte[44100*2*2];
			Log.i("testplayer", "1 " + file.read(buffer));
			Log.i("testplayer", "2 " + write(buffer, 0, buffer.length));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/

		try {
			file.seek(Constants.WAV_HEADER_SIZE + 2*start);
			byte[] segment = new byte[2 * ((int) (end-start))];
			file.read(segment);
			write(segment, 0, segment.length);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void play(long start, long end) {
		this.start = start;
		this.end = end;
		Thread t = new Thread(this);
		t.start();
	}
}
