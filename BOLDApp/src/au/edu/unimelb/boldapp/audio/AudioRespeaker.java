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
		microphone.listen(this);
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
		// if (respeakingStartOfSegment != null) {
		// 	Segment originalSegment = new Segment(
		// 		originalStartOfSegment,
		// 		originalEndOfSegment);
		// 	Segment respeakingSegment = new Segment(
		// 			respeakingStartOfSegment,
		// 			file.getCurrentSample());
		// 	segments.put(originalSegment, respeakingSegment);
		// }
		mapper.stop();
		// try {
		// 	segments.write(new File(mappingFilename));
		// } catch (IOException e) {
		// }
		file.close();
		// try {
		// 	writer.close();
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }
	}

	/** Pause listening to the microphone. */
	public void pause() {
		// super.pause();
		microphone.stop();
		player.pause();
		// Reset the analyzer to default values so it doesn't assume speech on
		// resuming.
		analyzer.reset();
	}

	/** Resume playing. */
	public void resume() {
		// super.listen();
		microphone.listen(this);
		// originalStartOfSegment = player.getCurrentSample();
		mapper.markOriginal(player);
		switchToPlay();
	}
  
	/*
	 * Switches the mode to play mode.
	 */
	protected void switchToPlay() {
		mapper.store(player, file);
		
		player.rewind(650);
		
		// respeakingEndOfSegment = file.getCurrentSample();
		// Segment originalSegment = new Segment(
		// 		originalStartOfSegment, originalEndOfSegment);
		// Segment respeakingSegment = new Segment(
		// 		respeakingStartOfSegment, respeakingEndOfSegment);
		// segments.put(originalSegment, respeakingSegment);
		// respeakingStartOfSegment = null;
		// respeakingEndOfSegment = null;
		// originalStartOfSegment = player.getCurrentSample();
		
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
			// long currentSample = file.getCurrentSample();
			// long originalCurrentSample = player.getCurrentSample();
			// mapper.markRespeaking(player, file);
			// try {
			// 	writer.write(originalCurrentSample + ",");
			// 	respeakingStartOfSegment = currentSample;
			// } catch (Exception e) {
			// 	e.printStackTrace();
			// }
			switchToRecord();
		}
		file.write(buffer);
	}

	public void silenceTriggered(short[] buffer, boolean justChanged) {
		// long currentSample = file.getCurrentSample();
		// long originalCurrentSample = player.getCurrentSample();
		if (justChanged) {
			//If the recording has finished playing and we're just annotating
			//at the end, then we're finished and can stop the respeaking.
			// try {
			// 	Log.i("issue37mapping", "respeaking: " + currentSample);
			// 	writer.write(currentSample + "\n");
			// 
			// } catch (Exception e) {
			// 	e.printStackTrace();
			// }
			if (getFinishedPlaying()) {
				// respeakingEndOfSegment = file.getCurrentSample();
				// Segment originalSegment = new Segment(
				// 		originalStartOfSegment, originalEndOfSegment);
				// Segment respeakingSegment = new Segment(
				// 		respeakingStartOfSegment, respeakingEndOfSegment);
				// segments.put(originalSegment, respeakingSegment);
				stop();
			} else {
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
