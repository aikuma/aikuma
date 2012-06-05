package au.edu.melbuni.boldapp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

/*
 * A writer that can handle PCM/WAV files.
 */
public class PCMWriter {

	String fullFilename;
	
	public static PCMWriter getInstance(int sampleRate, int channelConfig,
			int audioFormat) {
		return new PCMWriter(sampleRate, channelConfig, audioFormat);
	}

	// // Tries all sample rates.
	// //
	// public static PCMWriter getInstance() {
	// PCMWriter result = null;
	// int index = 0;
	// do {
	// result = getRecorder(index);
	// index += 1;
	// } while (result != null
	// && !(result.getState() == PCMWriter.State.INITIALIZING));
	// return result;
	// }
	//
	// private final static int[] sampleRates = { 44100, 22050, 11025, 8000 };
	//
	// public static PCMWriter getRecorder(int index) {
	// if (index >= sampleRates.length) {
	// return null;
	// }
	// return new PCMWriter(sampleRates[index],
	// AudioFormat.CHANNEL_CONFIGURATION_MONO,
	// AudioFormat.ENCODING_PCM_16BIT);
	// }

	/**
	 * INITIALIZING: Recorder is initializing. READY: Recorder has been
	 * initialized, writer not yet started. RECORDING: Recording. ERROR:
	 * Reconstruction needed. STOPPED: Reset needed.
	 */
	public enum State {
		INITIALIZING, READY, RECORDING, ERROR, STOPPED
	};

	// Recorder state; see State
	//
	private State state;

	// The interval in which the recorded samples are output to the file
	// Used only in uncompressed mode
	//
	private static final int TIMER_INTERVAL = 120;

	// File writer (only in uncompressed mode).
	//
	private RandomAccessFile randomAccessWriter;

	// Number of channels, sample rate, sample size(size in bits), buffer size,
	// audio source, sample size (see AudioFormat).
	//
	private short numberOfChannels;
	private int sampleRate;
	private short sampleSize;
	private int bufferSize;

	// Number of frames written to file on each output (only in uncompressed
	// mode)
	//
	private int framePeriod;

	// Number of bytes written to file after header (only in uncompressed mode)
	// after stop() is called, this size is written to the header/data chunk in
	// the wave file.
	//
	private int payloadSize = 0;

	/**
	 * 
	 * Returns the state of the writer in a RehearsalAudioRecord.State typed
	 * object. Useful, as no exceptions are thrown.
	 * 
	 * @return writer state
	 */
	public State getState() {
		return state;
	}

	// Write the given buffer to the file.
	//
	public void write(byte[] buffer) {
		try {
			// Write buffer to file.
			//
			randomAccessWriter.write(buffer);

			// Remember larger payload.
			//
			payloadSize += buffer.length;
		} catch (IOException e) {
			Log.e(PCMWriter.class.getName(),
					"Error occured in updateListener, recording is aborted");
			// stop();
		}
	}

	public void write(short[] buffer) {
		byte[] byteBuffer = new byte[buffer.length * 2];

		for (int i = 0; i < buffer.length; i++) {
			short sample = buffer[i];

			// TODO Use Java helpers?
			//
			byteBuffer[i * 2] = (byte) sample;
			byteBuffer[i * 2 + 1] = (byte) (sample >>> 8);
		}

		write(byteBuffer);
	}

	// /*
	// * Method used for recording.
	// *
	// * TODO Move around, rewrite!!!
	// */
	// private AudioRecord.OnRecordPositionUpdateListener updateListener = new
	// AudioRecord.OnRecordPositionUpdateListener() {
	// public void onPeriodicNotification(AudioRecord writer) {
	// audioRecorder.read(buffer, 0, buffer.length); // Fill buffer
	// try {
	// randomAccessWriter.write(buffer); // Write buffer to file
	// payloadSize += buffer.length;
	// if (sampleSize == 16) {
	//
	// // 16 Bit sample size.
	// //
	// for (int i = 0; i < buffer.length / 2; i++) {
	// short currentSample = getShort(buffer[i * 2],
	// buffer[i * 2 + 1]);
	//
	// // Check the amplitude.
	// //
	// if (currentSample > currentAmplitude) {
	// currentAmplitude = currentSample;
	// }
	// }
	// } else { // 8bit sample size
	// for (int i = 0; i < buffer.length; i++) {
	// // Check the amplitude.
	// //
	// if (buffer[i] > currentAmplitude) {
	// currentAmplitude = buffer[i];
	// }
	// }
	// }
	// } catch (IOException e) {
	// Log.e(ExtAudioRecorder.class.getName(),
	// "Error occured in updateListener, recording is aborted");
	// // stop();
	// }
	// }
	//
	// // This callback is not used.
	// //
	// public void onMarkerReached(AudioRecord writer) {
	//
	// }
	// };

	/**
	 * Default constructor.
	 * 
	 * Instantiates a new writer, in case of compressed recording the parameters
	 * can be left as 0. In case of errors, no exception is thrown, but the
	 * state is set to ERROR.
	 * 
	 */
	public PCMWriter(int sampleRate, int channelConfig, int audioFormat) {
		try {
			// Convert the Android attributes to internal attributes.
			//

			// Sample size.
			//
			if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
				sampleSize = 16;
			} else {
				sampleSize = 8;
			}

			// Channels.
			//
			if (channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO) {
				numberOfChannels = 1;
			} else {
				numberOfChannels = 2;
			}

			// These are needed to save the file correctly.
			//
			this.sampleRate = sampleRate;

			framePeriod = sampleRate * TIMER_INTERVAL / 1000;
			bufferSize = framePeriod * 2 * sampleSize * numberOfChannels / 8;

			// Check to make sure buffer size is not smaller than
			// the smallest allowed size.
			//
			if (bufferSize < AudioRecord.getMinBufferSize(sampleRate,
					channelConfig, audioFormat)) {
				bufferSize = AudioRecord.getMinBufferSize(sampleRate,
						channelConfig, audioFormat);

				// Set frame period and timer interval accordingly
				//
				framePeriod = bufferSize
						/ (2 * sampleSize * numberOfChannels / 8);

				Log.w(PCMWriter.class.getName(), "Increasing buffer size to "
						+ Integer.toString(bufferSize));
			}
		} catch (Exception e) {
			if (e.getMessage() != null) {
				Log.e(PCMWriter.class.getName(), e.getMessage());
			} else {
				Log.e(PCMWriter.class.getName(),
						"Unknown error occured while initializing recording");
			}
			state = State.ERROR;
		}
	}

	// /**
	// * Sets output file path, call directly after construction/reset.
	// *
	// * @param output
	// * file path
	// */
	// public void setOutputFile(String fullFilename) {
	// try {
	// if (state == State.INITIALIZING) {
	// this.fullFilename = fullFilename;
	// }
	// } catch (Exception e) {
	// if (e.getMessage() != null) {
	// Log.e(PCMWriter.class.getName(), e.getMessage());
	// } else {
	// Log.e(PCMWriter.class.getName(),
	// "Unknown error occured while setting output path");
	// }
	// state = State.ERROR;
	// }
	// }

	// /**
	// *
	// * Returns the largest amplitude sampled since the last call to this
	// method.
	// *
	// * @return returns the largest amplitude since the last call, or 0 when
	// not
	// * in recording state.
	// *
	// */
	// public int getMaxAmplitude() {
	// if (state == State.RECORDING) {
	// int result = currentAmplitude;
	// currentAmplitude = 0;
	// return result;
	// } else {
	// return 0;
	// }
	// }
	
	private void createRandomAccessFile(String fullFilename) {
		try {
			// Random access file.
			//
			randomAccessWriter = new RandomAccessFile(this.fullFilename, "rw");
		} catch (FileNotFoundException e) {
			
		}
	}

	/**
	 * Prepares the writer for recording by writing the WAV file header.
	 */
	public void prepare(String fullFilename) {
		this.fullFilename = fullFilename;
		
		try {
			createRandomAccessFile(fullFilename);

			// Write the full WAV PCM file header.
			//

			// Set file length to 0, to prevent unexpected
			// behaviour in case the file already existed.
			//
			randomAccessWriter.setLength(0);

			// "RIFF" announcement.
			//
			randomAccessWriter.writeBytes("RIFF");

			// File size, 0 = unknown.
			//
			randomAccessWriter.writeInt(0);

			// "WAVE fmt " = WAV format.
			//
			randomAccessWriter.writeBytes("WAVE");
			randomAccessWriter.writeBytes("fmt ");

			// Sub-chunk size, 16 = PCM.
			//
			randomAccessWriter.writeInt(Integer.reverseBytes(16));

			// AudioFormat, 1 = PCM.
			//
			randomAccessWriter.writeShort(Short.reverseBytes((short) 1));

			// Number of channels, 1 = mono, 2 = stereo.
			//
			randomAccessWriter.writeShort(Short.reverseBytes(numberOfChannels));

			// Sample rate.
			//
			randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate));

			// Byte rate = SampleRate * NumberOfChannels * BitsPerSample / 8.
			//
			randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate
					* sampleSize * numberOfChannels / 8));

			// Block align = NumberOfChannels * BitsPerSample / 8.
			//
			randomAccessWriter.writeShort(Short
					.reverseBytes((short) (numberOfChannels * sampleSize / 8)));

			// Bits per sample.
			//
			randomAccessWriter.writeShort(Short.reverseBytes(sampleSize));

			// "data" announcement.
			//
			randomAccessWriter.writeBytes("data");

			// Data chunk size, 0 = unknown.
			//
			randomAccessWriter.writeInt(0);

			// Clear the byte array.
			//
			// buffer = new byte[framePeriod * sampleSize / 8 *
			// numberOfChannels];
			//
		} catch (Exception e) {
			if (e.getMessage() != null) {
				Log.e(PCMWriter.class.getName(), e.getMessage());
			} else {
				Log.e(PCMWriter.class.getName(),
						"Unknown error occured in prepare()");
			}
			state = State.ERROR;
		}
	}

	/**
	 * Finalizes the wave file.
	 */
	public void close() {
		// This is only necessary as the randomAccessWriter might be closed.
		//
		createRandomAccessFile(fullFilename);
		
		try {
			// Write size to RIFF header.
			//
			randomAccessWriter.seek(4);
			randomAccessWriter.writeInt(Integer.reverseBytes(36 + payloadSize));

			// Write size to Sub-chunk size header.
			//
			randomAccessWriter.seek(40);
			randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize));

			randomAccessWriter.close();
		} catch (IOException e) {
			Log.e(PCMWriter.class.getName(),
					"I/O exception occured while closing output file");
		}
	}

}
