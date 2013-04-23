package au.edu.unimelb.aikuma.audio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import au.edu.unimelb.aikuma.audio.Respeaker;
import au.edu.unimelb.aikuma.audio.Player;
import au.edu.unimelb.aikuma.audio.analyzers.Analyzer;
import au.edu.unimelb.aikuma.audio.analyzers.ThresholdSpeechAnalyzer;
import au.edu.unimelb.aikuma.audio.recognizers.AverageRecognizer;

import au.edu.unimelb.aikuma.FileIO;

import au.edu.unimelb.aikuma.audio.NewSegments.Segment;

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

	public void listen() {
		mapper.markRespeaking(player, file);
		microphone.listen(this); // This object's onBufferFull() is called.
	}

	/**
	 * To listen to the final piece of annotation after playing of the original
	 * has been completed.
	 */
	public void listenAfterFinishedPlaying() {
		listen();
	}

	public void play() {
		mapper.markOriginal(player);
		player.seekTo(player.sampleToMsec(mapper.getOriginalStartSample()));
		player.play();
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

	/** Resume playing. */
	public void resume() {
		microphone.listen(this);
		rewindToSegmentStart();
		mapper.markOriginal(player);
		switchToPlay();
	}
	
	public void rewindToSegmentStart() {
		player.seekTo(player.sampleToMsec(mapper.getOriginalStartSample()));
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
				player.rewind(650);
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
	
}
