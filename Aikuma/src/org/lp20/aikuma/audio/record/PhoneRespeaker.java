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
public class PhoneRespeaker implements AudioListener, AudioHandler,
		MicrophoneListener {

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

	private void setUpPlayer(Recording original) throws IOException {
		this.player = new SimplePlayer(original, false);
	}

	public void listenNoPlay() {
		//mapper.markRespeaking(player, file);
		microphone.listen(this);
	}

	/** Callback for the microphone */
	public void onBufferFull(short[] buffer) {
		// This will call back the methods:
		//  * silenceTriggered
		//  * audioTriggered
		analyzer.analyze(this, buffer);
	}

	private void switchToRecord() {
		Log.i("sound", "switchToRecord()");
		player.pause();
		mapper.markRespeaking(player, file);
	}

	protected void switchToPlay() {
		Log.i("sound", "switchToPlay()");
		player.play();
	}

	/** Pause listening to the microphone. */
	public void pause() {
		try {
			microphone.stop();
		} catch (MicException e) {
			//Do Nothing. Could perhaps replace with a new Microphone.
		}
		player.pause();
		// Reset the analyzer to default values so it doesn't assume speech on
		// resuming.
		analyzer.reset();
	}

	public void stop() {
		try {
			microphone.stop();
		} catch (MicException e) {
			//Do nothing.
		}
		mapper.stop();
		file.close();
	}

	public void release() {
		if (player != null) {
			player.release();
		}
	}

	// The following two methods handle silences/speech discovered in the input
	// data.

	public void audioTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			switchToRecord();
		}
		file.write(buffer);
	}

	public void silenceTriggered(short[] buffer, boolean justChanged) {
		if (justChanged) {
			if (this.player.isFinishedPlaying()) {
				stop();
			} else {
				mapper.store(player, file);
				//player.rewind(getRewindAmount());
				switchToPlay();
			}
		}
	}

	public int getRewindAmount() {
		return 650;
	}

	/** Resume playing. */
	public void resume() {
		microphone.listen(this);
		//rewindToSegmentStart();
		mapper.markOriginal(player);
		switchToPlay();
	}
	
	//public void rewindToSegmentStart() {
	//	int msecs = player.sampleToMsec(mapper.getOriginalStartSample());
	//	msecs = msecs - getRewindAmount();
	//	player.seekTo(msecs >= 0 ? msecs : 0);
	//}
	public SimplePlayer getSimplePlayer() {
		return this.player;
	}

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

	/////////////////////////////////////////////////////////////////////
	

	
	///** Default constructor. */
	//public PhoneRespeaker(ThresholdSpeechAnalyzer analyzer, boolean
	//		shouldPlayThroughSpeaker) {
	//	this.analyzer = analyzer;
	//	
	//	microphone = new Microphone();
	//	player = new Player();
	//	file = PCMFile.getInstance(microphone);
	//	
	//	if (shouldPlayThroughSpeaker) {
	//		this.playThroughSpeaker();
	//	} else {
	//		this.playThroughEarpiece();
	//	}
	//}
	
	//public void setSensitivity(int threshold) {
	//	this.analyzer = new ThresholdSpeechAnalyzer(88, 3,
	//			new AverageRecognizer(threshold, threshold));
	//}
  
	///** Prepare the respeaker by setting a source file and a target file. */
	//public void prepare(String sourceFilename, String targetFilename,
	//		String mappingFilename) {
	//	player.prepare(sourceFilename);
	//	file.prepare(new File(targetFilename));
	//	mapper.prepare(mappingFilename);
	//}


	//public void setOnCompletionListener(OnCompletionListener ocl) {
	//	player.setOnCompletionListener(ocl);
	//}

	//public void playThroughEarpiece() {
	//	player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
	//}

	//public void playThroughSpeaker() {
	//	player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	//}

}
