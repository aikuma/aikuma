package au.edu.melbuni.boldapp;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

/*
 * A recorder that can handle PCM/WAV files.
 */
public class PCMRecorder {

	// Tries all sample rates.
	//
	public static PCMRecorder getInstance() {
		PCMRecorder result = null;
		int index = 0;
		do {
			result = getRecorder(index);
			index += 1;
		} while (result != null
				&& !(result.getState() == PCMRecorder.State.INITIALIZING));
		return result;
	}

	private final static int[] sampleRates = { 44100, 22050, 11025, 8000 };

	public static PCMRecorder getRecorder(int index) {
		if (index >= sampleRates.length) {
			return null;
		}
		return new PCMRecorder(AudioSource.MIC, sampleRates[index],
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
	}

	/**
	 * INITIALIZING: Recorder is initializing. READY: Recorder has been
	 * initialized, recorder not yet started. RECORDING: Recording. ERROR:
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

	// Recorder used for uncompressed recording
	//
	private AudioRecord audioRecorder = null;

	// Stores current amplitude (only in uncompressed mode)
	//
	private int currentAmplitude = 0;

	// Output file path
	//
	private String fullFilename = null;

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
	private int audioSource;
	private int audioFormat;

	// Number of frames written to file on each output (only in uncompressed
	// mode)
	//
	private int framePeriod;

	// Buffer for output (only in uncompressed mode).
	//
	private byte[] buffer;

	// Number of bytes written to file after header (only in uncompressed mode)
	// after stop() is called, this size is written to the header/data chunk in
	// the wave file.
	//
	private int payloadSize;

	/**
	 * 
	 * Returns the state of the recorder in a RehearsalAudioRecord.State typed
	 * object. Useful, as no exceptions are thrown.
	 * 
	 * @return recorder state
	 */
	public State getState() {
		return state;
	}

	public void record(byte[] buffer) {
		this.buffer = buffer;
		try {
			randomAccessWriter.write(buffer); // Write buffer to file
			payloadSize += buffer.length;

			// 16 Bit sample size.
			//
			if (sampleSize == 16) {
				for (int i = 0; i < buffer.length / 2; i++) {
					short currentSample = getShort(buffer[i * 2],
							buffer[i * 2 + 1]);

					// Check the amplitude.
					//
					if (currentSample > currentAmplitude) {
						currentAmplitude = currentSample;
					}
				}
			} else { // 8bit sample size.
				for (int i = 0; i < buffer.length; i++) {
					
					// Check the amplitude.
					//
					if (buffer[i] > currentAmplitude) {
						currentAmplitude = buffer[i];
					}
				}
			}
		} catch (IOException e) {
			Log.e(PCMRecorder.class.getName(),
					"Error occured in updateListener, recording is aborted");
			// stop();
		}
	}

//	/*
//	 * Method used for recording.
//	 * 
//	 * TODO Move around, rewrite!!!
//	 */
//	private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
//		public void onPeriodicNotification(AudioRecord recorder) {
//			audioRecorder.read(buffer, 0, buffer.length); // Fill buffer
//			try {
//				randomAccessWriter.write(buffer); // Write buffer to file
//				payloadSize += buffer.length;
//				if (sampleSize == 16) {
//
//					// 16 Bit sample size.
//					//
//					for (int i = 0; i < buffer.length / 2; i++) {
//						short currentSample = getShort(buffer[i * 2],
//								buffer[i * 2 + 1]);
//
//						// Check the amplitude.
//						//
//						if (currentSample > currentAmplitude) {
//							currentAmplitude = currentSample;
//						}
//					}
//				} else { // 8bit sample size
//					for (int i = 0; i < buffer.length; i++) {
//						// Check the amplitude.
//						//
//						if (buffer[i] > currentAmplitude) {
//							currentAmplitude = buffer[i];
//						}
//					}
//				}
//			} catch (IOException e) {
//				Log.e(ExtAudioRecorder.class.getName(),
//						"Error occured in updateListener, recording is aborted");
//				// stop();
//			}
//		}
//
//		// This callback is not used.
//		//
//		public void onMarkerReached(AudioRecord recorder) {
//
//		}
//	};

	/**
	 * Default constructor.
	 * 
	 * Instantiates a new recorder, in case of compressed recording the
	 * parameters can be left as 0. In case of errors, no exception is thrown,
	 * but the state is set to ERROR.
	 * 
	 */
	public PCMRecorder(int audioSource, int sampleRate, int channelConfig,
			int audioFormat) {
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
			this.audioSource = audioSource;
			this.sampleRate = sampleRate;
			this.audioFormat = audioFormat;

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

				Log.w(PCMRecorder.class.getName(),
						"Increasing buffer size to "
								+ Integer.toString(bufferSize));
			}

			audioRecorder = new AudioRecord(audioSource, sampleRate,
					channelConfig, audioFormat, bufferSize);

			if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
				throw new Exception("AudioRecord initialization failed");
//			audioRecorder.setRecordPositionUpdateListener(updateListener); // TODO!!!
			audioRecorder.setPositionNotificationPeriod(framePeriod);
			currentAmplitude = 0;
			fullFilename = null;
			state = State.INITIALIZING;
		} catch (Exception e) {
			if (e.getMessage() != null) {
				Log.e(PCMRecorder.class.getName(), e.getMessage());
			} else {
				Log.e(PCMRecorder.class.getName(),
						"Unknown error occured while initializing recording");
			}
			state = State.ERROR;
		}
	}

	/**
	 * Sets output file path, call directly after construction/reset.
	 * 
	 * @param output
	 *            file path
	 */
	public void setOutputFile(String fullFilename) {
		try {
			if (state == State.INITIALIZING) {
				this.fullFilename = fullFilename;
			}
		} catch (Exception e) {
			if (e.getMessage() != null) {
				Log.e(PCMRecorder.class.getName(), e.getMessage());
			} else {
				Log.e(PCMRecorder.class.getName(),
						"Unknown error occured while setting output path");
			}
			state = State.ERROR;
		}
	}

	/**
	 * 
	 * Returns the largest amplitude sampled since the last call to this method.
	 * 
	 * @return returns the largest amplitude since the last call, or 0 when not
	 *         in recording state.
	 * 
	 */
	public int getMaxAmplitude() {
		if (state == State.RECORDING) {
			int result = currentAmplitude;
			currentAmplitude = 0;
			return result;
		} else {
			return 0;
		}
	}

	/**
	 * 
	 * Prepares the recorder for recording, in case the recorder is not in the
	 * INITIALIZING state and the file path was not set the recorder is set to
	 * the ERROR state, which makes a reconstruction necessary. In case
	 * uncompressed recording is toggled, the header of the wave file is
	 * written. In case of an exception, the state is changed to ERROR
	 * 
	 */
	public void prepare() {
		try {
			if (state == State.INITIALIZING) {
				if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED)
						& (fullFilename != null)) {

					// Random access file.
					//
					randomAccessWriter = new RandomAccessFile(fullFilename,
							"rw");

					// Write the full WAV PCM file header.
					//

					// Set file length to 0, to prevent unexpected
					// behaviour in case the file already existed.
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
					randomAccessWriter
							.writeShort(Short.reverseBytes((short) 1));

					// Number of channels, 1 = mono, 2 = stereo.
					//
					randomAccessWriter.writeShort(Short
							.reverseBytes(numberOfChannels));

					// Sample rate.
					//
					randomAccessWriter.writeInt(Integer
							.reverseBytes(sampleRate));

					// Byte rate = SampleRate * NumberOfChannels * BitsPerSample
					// / 8.
					//
					randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate
							* sampleSize * numberOfChannels / 8));

					// Block align = NumberOfChannels * BitsPerSample / 8.
					//
					randomAccessWriter.writeShort(Short
							.reverseBytes((short) (numberOfChannels
									* sampleSize / 8)));

					// Bits per sample.
					//
					randomAccessWriter.writeShort(Short
							.reverseBytes(sampleSize));

					// "data" announcement.
					//
					randomAccessWriter.writeBytes("data");

					// Data chunk size, 0 = unknown.
					//
					randomAccessWriter.writeInt(0);

					// Clear the byte array.
					//
					buffer = new byte[framePeriod * sampleSize / 8
							* numberOfChannels];

					// Recorder is now ready.
					//
					state = State.READY;
				} else {
					Log.e(PCMRecorder.class.getName(),
							"prepare() method called on uninitialized recorder");
					state = State.ERROR;
				}
			} else {
				Log.e(PCMRecorder.class.getName(),
						"prepare() method called on illegal state");
				release();
				state = State.ERROR;
			}
		} catch (Exception e) {
			if (e.getMessage() != null) {
				Log.e(PCMRecorder.class.getName(), e.getMessage());
			} else {
				Log.e(PCMRecorder.class.getName(),
						"Unknown error occured in prepare()");
			}
			state = State.ERROR;
		}
	}

	/**
	 * Releases the resources associated with this class, and removes the
	 * unnecessary files, when necessary.
	 */
	public void release() {
		if (state == State.RECORDING) {
			stop();
		} else {
			if (state == State.READY) {
				try {
					randomAccessWriter.close(); // Remove prepared file
				} catch (IOException e) {
					Log.e(PCMRecorder.class.getName(),
							"I/O exception occured while closing output file");
				}
				(new File(fullFilename)).delete();
			}
		}

		if (audioRecorder != null) {
			audioRecorder.release();
		}
	}

	/**
	 * 
	 * 
	 * Resets the recorder to the INITIALIZING state, as if it was just created.
	 * In case the class was in RECORDING state, the recording is stopped. In
	 * case of exceptions the class is set to the ERROR state.
	 * 
	 */
	public void reset() {
		try {
			if (state != State.ERROR) {
				release();
				fullFilename = null; // Reset file path
				currentAmplitude = 0; // Reset amplitude
				audioRecorder = new AudioRecord(audioSource, sampleRate,
						numberOfChannels + 1, audioFormat, bufferSize);
				state = State.INITIALIZING;
			}
		} catch (Exception e) {
			Log.e(PCMRecorder.class.getName(), e.getMessage());
			state = State.ERROR;
		}
	}

	/**
	 * 
	 * 
	 * Starts the recording, and sets the state to RECORDING. Call after
	 * prepare().
	 * 
	 */
	public void start() {
		if (state == State.READY) {
			payloadSize = 0;
			audioRecorder.startRecording();
			audioRecorder.read(buffer, 0, buffer.length);
			state = State.RECORDING;
		} else {
			Log.e(PCMRecorder.class.getName(),
					"start() called on illegal state");
			state = State.ERROR;
		}
	}

	/**
	 * Stops the recording, and sets the state to STOPPED. In case of further
	 * usage, a reset is needed. Also finalizes the wave file in case of
	 * uncompressed recording.
	 */
	public void stop() {
		if (state == State.RECORDING) {
			audioRecorder.stop();

			// Finalize the WAV file.
			//
			try {
				// Write size to RIFF header.
				//
				randomAccessWriter.seek(4);
				randomAccessWriter.writeInt(Integer
						.reverseBytes(36 + payloadSize));

				// Write size to Sub-chunk size header.
				//
				randomAccessWriter.seek(40);
				randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize));

				randomAccessWriter.close();
			} catch (IOException e) {
				Log.e(PCMRecorder.class.getName(),
						"I/O exception occured while closing output file");
				state = State.ERROR;
			}
			state = State.STOPPED;
		} else {
			Log.e(PCMRecorder.class.getName(),
					"stop() called on illegal state");
			state = State.ERROR;
		}
	}

	/*
	 * 
	 * Converts a byte[2] to a short, in LITTLE_ENDIAN format
	 */
	private short getShort(byte argB1, byte argB2) {
		return (short) (argB1 | (argB2 << 8));
	}

}
