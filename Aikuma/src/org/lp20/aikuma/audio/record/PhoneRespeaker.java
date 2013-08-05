package org.lp20.aikuma.audio.recording.PhoneRespeaker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import org.lp20.aikuma.audio.Player;
import org.lp20.aikuma.audio.record.analyzers.Analyzer;
import org.lp20.aikuma.audio.record.analyzers.ThresholdSpeechAnalyzer;
import org.lp20.aikuma.audio.recognizers.AverageRecognizer;

/** Respeaker used to get input from eg. a microphone and
 *  output into a file.tIn addition, it also 
 * 
 *  Usage:
 *    AudioRespeaker respeaker = new AudioRespeaker();
 *    respeaker.prepare(
 *      "/mnt/sdcard/bold/recordings/source_file.wav",
 *      "/mnt/sdcard/bold/recordings/target_file.wav"
 *    );
 *    respeaker.listen();
 *    respeaker.pause();
 *    respeaker.resume();
 *    respeaker.stop();
 *
 *  Note that stopping the respeaker closes and finalizes the WAV file.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class AudioRespeaker implements AudioListener, AudioHandler {
	
	private Analyzer analyzer;
	
	/** The microphone used to get respeaking data. */
	private Microphone microphone;
	
	/** Player to play the original with. */
	private Player player;
	
	/** File to write to */
	private PCMFile file;
	
	/** The mapper used to store mapping data. */
	private Mapper mapper;

	/** Indicates whether the recording has finished playing. */
	private boolean finishedPlaying = false;
	
	/** Default constructor. */
	public AudioRespeaker(ThresholdSpeechAnalyzer analyzer, boolean
			shouldPlayThroughSpeaker) {
		this.analyzer = analyzer;
		
		microphone = new Microphone();
		player = new Player();
		file = PCMFile.getInstance(microphone);
		mapper = new Mapper();
		
		if (shouldPlayThroughSpeaker) {
			this.playThroughSpeaker();
		} else {
			this.playThroughEarpiece();
		}
	}
	
	public void setSensitivity(int threshold) {
		this.analyzer = new ThresholdSpeechAnalyzer(88, 3,
				new AverageRecognizer(threshold, threshold));
	}
  
	/** Prepare the respeaker by setting a source file and a target file. */
	public void prepare(String sourceFilename, String targetFilename,
			String mappingFilename) {
		player.prepare(sourceFilename);
		file.prepare(new File(targetFilename));
		mapper.prepare(mappingFilename);
	}

	public void stop() {
		microphone.stop();
		mapper.store(player, file);
		player.stop();
		mapper.stop();
		file.close();
	}

	/** Pause listening to the microphone. */
	public void pause() {
		microphone.stop();
		player.pause();
		// Reset the analyzer to default values so it doesn't assume speech on
		// resuming.
		analyzer.reset();
	}
	
	/** Wait for a respeaking. */
	public void listen() {
		mapper.markRespeaking(player, file);
		microphone.listen(this); // This object's onBufferFull() is called.
	}

	/** Resume playing. */
	public void resume() {
		microphone.listen(this);
		rewindToSegmentStart();
		mapper.markOriginal(player);
		switchToPlay();
	}
	
	public void rewindToSegmentStart() {
		int msecs = player.sampleToMsec(mapper.getOriginalStartSample());
		msecs = msecs - getRewindAmount();
		player.seekTo(msecs >= 0 ? msecs : 0);
	}
  
	/*
	 * Switches the mode to play mode.
	 */
	protected void switchToPlay() {
		player.resume();
	}

	/** Switches the mode to record mode. */
	protected void switchToRecord() {
		player.pause();
		mapper.markRespeaking(player, file);
	}
	
	public void onBufferFull(short[] buffer) {
		analyzer.analyze(this, buffer);
	}

	public void audioTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			switchToRecord();
		}
		file.write(buffer);
	}

	public void silenceTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			if (getFinishedPlaying()) {
				stop();
			} else {
				mapper.store(player, file);
				player.rewind(getRewindAmount());
				switchToPlay();
			}
		}
	}
	
	public void setOnCompletionListener(OnCompletionListener ocl) {
		player.setOnCompletionListener(ocl);
	}

	public void playThroughEarpiece() {
		player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
	}

	public void playThroughSpeaker() {
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}

	public boolean getFinishedPlaying() {
		return finishedPlaying;
	}
	
	public void setFinishedPlaying(boolean finishedPlaying) {
		this.finishedPlaying = finishedPlaying;
	}
	
	public int getRewindAmount() {
		return 650;
	}
	
}
