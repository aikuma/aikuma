package org.lp20.aikuma.audio.record;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.SimplePlayer;
import org.lp20.aikuma.audio.record.recognizers.AverageRecognizer;
import org.lp20.aikuma.audio.record.analyzers.Analyzer;
import org.lp20.aikuma.audio.record.analyzers.ThresholdSpeechAnalyzer;
import static org.lp20.aikuma.audio.record.Microphone.MicException;
import org.lp20.aikuma.model.Recording;

/**
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class PhoneRespeaker implements
		AudioListener, AudioHandler, MicrophoneListener {

	public PhoneRespeaker(Recording original, UUID respeakingUUID,
			Analyzer analyzer) throws MicException, IOException {
		this.analyzer = analyzer;
		setUpMicrophone(original.getSampleRate());
		setUpFile(respeakingUUID);
		setUpPlayer(original);
		mapper = new Mapper(respeakingUUID);
	}

	/** Sets up the microphone for recording. */
	private void setUpMicrophone(long sampleRate) throws MicException {
		this.microphone = new Microphone(sampleRate);
	}

	/** Sets the file up for writing. */
	private void setUpFile(UUID respeakingUUID) {
		file = PCMWriter.getInstance(
				microphone.getSampleRate(),
				microphone.getChannelConfiguration(),
				microphone.getAudioFormat()
		);
		file.prepare(new File(Recording.getRecordingsPath(),
				respeakingUUID + ".wav").getPath());
	}

	/** Sets the sensitivity of the respeaker to microphone noise */
	public void setSensitivity(int threshold) {
		this.analyzer = new ThresholdSpeechAnalyzer(88, 3,
				new AverageRecognizer(threshold, threshold));
	}

	private void setUpPlayer(Recording original) throws IOException {
		this.player = new SimplePlayer(original, false);
	}

	/** Callback for the microphone */
	public void onBufferFull(short[] buffer) {
		// This will call back the methods:
		//  * silenceTriggered
		//  * audioTriggered
		analyzer.analyze(this, buffer);
	}

	public void resume() {
		microphone.listen(this);
		//mapper.markOriginal(player);
		switchToPlay();
	}

	public void halt() {
		mapper.store(player, file);
		stopMic();
		player.pause();
		analyzer.reset();
	}

	public void stop() {
		stopMic();
		player.release();
		mapper.stop();
		file.close();
	}

	private void stopMic() {
		try {
			microphone.stop();
		} catch (MicException e) {
			//Do nothing.
		}
	}

	public void audioTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			switchToRecord();
		}
		file.write(buffer);
	}

	private void switchToRecord() {
		player.pause();
		mapper.markRespeaking(player, file);
	}

	private void switchToPlay() {
		player.play();
	}

	public void silenceTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			mapper.store(player, file);
			if (this.player.isFinishedPlaying()) {
				stop();
			} else {
				//player.rewind(getRewindAmount());
				switchToPlay();
			}
		}
	}

	public void release() {
		if (player != null) {
			player.release();
		}
	}

	// The following two methods handle silences/speech discovered in the input
	// data.
	//public int getRewindAmount() {
	//	return 650;
	//}

	public SimplePlayer getSimplePlayer() {
		return this.player;
	}

	public int getCurrentMsec() {
		return this.player.sampleToMsec(file.getCurrentSample());
	}

	//public void rewindToSegmentStart() {
	//	int msecs = player.sampleToMsec(mapper.getOriginalStartSample());
	//	msecs = msecs - getRewindAmount();
	//	player.seekTo(msecs >= 0 ? msecs : 0);
	//}

	/** Analyzer that determines whether speech is happening */
	private Analyzer analyzer;
	/** The microphone used to get respeaking data. */
	private Microphone microphone;
	/** The file to write to */
	private PCMWriter file;
	/** Player to play the original with. */
	private SimplePlayer player;
	/** Indicates whether the recording has finished playing. */
	private boolean finishedPlaying = false;
	/** The mapper used to store mapping data. */
	private Mapper mapper;

}
