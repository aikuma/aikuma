/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import android.media.AudioFormat;
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
 * Facilitates respeaking of a recording by playing the recording, then
 * stopping playing and recording the user's voice when audio above a threshold
 * is produced.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class PhoneRespeaker implements
		AudioListener, AudioHandler, MicrophoneListener {

	/**
	 * Constructor
	 *
	 * @param	original	The original recording.
	 * @param	respeakingUUID	The UUID of this respeaking.
	 * @param	analyzer	The analyzer that determines what audio constitutes
	 * speech.
	 * @param	rewindAmount	Rewind-amount in msec after each respeaking-segment
	 * @throws	MicException	If there is an issue setting up the microphone.
	 * @throws	IOException	If there is an I/O issue.
	 */
	public PhoneRespeaker(Recording original, UUID respeakingUUID,
			Analyzer analyzer, int rewindAmount) throws MicException, IOException {
		this.analyzer = analyzer;
		this.rewindAmount = rewindAmount;
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
		file.prepare(new File(Recording.getNoSyncRecordingsPath(),
				respeakingUUID + ".wav").getPath());
	}

	/**
	 * Sets the sensitivity of the respeaker to microphone noise
	 *
	 * @param	threshold	The threshold above which audio is considered to be
	 * speech.
	 */
	public void setSensitivity(int threshold) {
		this.analyzer = new ThresholdSpeechAnalyzer(88, 3,
				new AverageRecognizer(threshold, threshold));
	}

	// Prepares the player of the original.
	private void setUpPlayer(Recording original) throws IOException {
		this.player = new SimplePlayer(original, false);
	}

	@Override
	public void onBufferFull(short[] buffer) {
		// This will call back the methods:
		//  * silenceTriggered
		//  * audioTriggered
		analyzer.analyze(this, buffer);
	}

	/**
	 * Resumes the phone respeaking process.
	 */
	public void resume() {
		microphone.listen(this);
		//mapper.markOriginal(player);
		switchToPlay();
	}

	/**
	 * Halts the phone respeaking process, but allows for resumption.
	 */
	public void halt() {
		mapper.store(player, file);
		stopMic();
		player.pause();
		analyzer.reset();
	}

	/**
	 * Stops/finishes the phone respeaking process.
	 */
	public void stop(){
		stopMic();
		player.release();
		try {
			mapper.stop();
		} catch (IOException e) {
			//If the mapping couldn't be written to file, the recording would
			//not have been able to either, which would have resulted in an
			//error. This block is unreachable.
		}
		file.close();
	}

	// Stops the microphone
	private void stopMic() {
		try {
			microphone.stop();
		} catch (MicException e) {
			//Do nothing.
		}
	}

	@Override
	public void audioTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			switchToRecord();
		}
		file.write(buffer);
	}

	// Switches from playing to recording
	private void switchToRecord() {
		player.pause();
		mapper.markRespeaking(player, file);
	}

	// Switches from recording to playing
	private void switchToPlay() {
		player.play();
	}

	@Override
	public void silenceTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			mapper.store(player, file);
			if (this.player.isFinishedPlaying()) {
				stop();
			} else {
				player.rewind(rewindAmount);
				switchToPlay();
			}
		}
	}

	/**
	 * Releases the resources associated with this respeaker.
	 */
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

	/**
	 * Returns the number of channels of the WAV.
	 *
	 * @return	The number of channels of the WAV.
	 */
	public int getNumChannels() {
		if (microphone.getChannelConfiguration() ==
				AudioFormat.CHANNEL_IN_MONO) {
			return 1;
		}else {
			return 2;
		}
	}

	/**
	 * Returns the bits per sample of the WAV
	 *
	 * @return	The bits per sample of the WAV.
	 */
	public int getBitsPerSample() {
		if (microphone.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
			return 16;
		} else {
			return 8;
		}
	}

	/**
	 * Returns the audio mime type (but only the section after the forward
	 * slash)
	 *
	 * @return	The audio format
	 */
	public String getFormat() {
		return "vnd.wave";
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
	private PCMWriter sampleFile;
	/** Player to play the original with. */
	private SimplePlayer player;
	/** Indicates whether the recording has finished playing. */
	private boolean finishedPlaying = false;
	/** The mapper used to store mapping data. */
	private Mapper mapper;

	/** The amount to rewind the original in msec 
	 * after each respeaking-segment. */
	private int rewindAmount;
}
