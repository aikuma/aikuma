/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.audio.record;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import org.lp20.aikuma.audio.Sampler;

/**
 * A writer that can handle PCM/WAV files.
 *
 * Process:
 *  1. It opens a file.
 *  2. It writes a header with length etc. information missing.
 *  3. When it is closed, it writes the necessary information into the header.
 *  4. It closes the file.
 *
 * Note: The file cannot be reopened.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class PCMWriter implements Sampler {

	/**
	 * The current sample, which represents where in the recording we are.
	 */
	private long currentSample;

	public long getCurrentSample(){
		return this.currentSample;
	}

	String fullFilename;
	/**
	 * @param	sampleRate		Eg. 1000
	 * @param	channelConfig	Eg. AudioFormat.CHANNEL_IN_MONO
	 * @param	audioFormat		Eg. AudioFormat.ENCODING_PCM_16BIT
	 *
	 * @return an instance of a PCMWriter.
	 */
	public static PCMWriter getInstance(int sampleRate, int channelConfig,
			int audioFormat) {
		return new PCMWriter(sampleRate, channelConfig, audioFormat);
	}

	/**
	 * The interval in which the recorded samples are output to the file.
	 *
	 *  Note: Used only in uncompressed mode.
	 */
	private static final int TIMER_INTERVAL = 120;

	/**
	 * File writer (only in uncompressed mode).
	 */
	private RandomAccessFile randomAccessWriter;

	/**
	 * Number of channels, sample rate, sample size(size in bits), buffer size,
	 * audio source, sample size (see AudioFormat).
	 */
	private short numberOfChannels;
	private int sampleRate;
	private short sampleSize;
	private int bufferSize;

	/**
	 * Number of frames written to file on each output (only in uncompressed
	 * mode)
	 */
	private int framePeriod;

	/**
	 * Number of bytes written to file after header (only in uncompressed mode)
	 * after stop() is called, this size is written to the header/data chunk in
	 * the wave file.
	 */
	private int payloadSize = 0;

	/**
	 * Write the given byte buffer to the file.
	 *
	 * Note: This method remembers the size of the buffer written so far.
	 *
	 * @param	buffer	The buffer containing the audio data to be written.
	 */
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
		}

		if (sampleSize == 16) {
			this.currentSample += buffer.length / 2;
		} else {
			//Assume sample size is 8.
			this.currentSample += buffer.length;
		}
	}

	/**
	 * Write the given short buffer to the file.
	 *
	 * @param	buffer	The buffer containing audio data to be written.
	 */
	public void write(short[] buffer) {
		byte[] byteBuffer = new byte[buffer.length * 2];

		for (int i = 0; i < buffer.length; i++) {
			short sample = buffer[i];

			// TODO Use Java helpers?
			byteBuffer[i * 2] = (byte) sample;
			byteBuffer[i * 2 + 1] = (byte) (sample >>> 8);
		}

		write(byteBuffer);
	}

	/**
	 * Default constructor.
	 *
	 *  @param sampleRate    Eg. 1000
	 *  @param channelConfig Eg. AudioFormat.CHANNEL_IN_MONO
	 *  @param audioFormat   Eg. AudioFormat.ENCODING_PCM_16BIT
	 */
	public PCMWriter(int sampleRate, int channelConfig, int audioFormat) {
		currentSample = 0;
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
		if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
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
	}
	
	/**
	 * Tries to create a RandomAccessFile.
	 *
	 * @param fullFilename The full path of the file to write.
	 */
	private void createRandomAccessFile(String fullFilename) {
		try {
			// Random access file.
			//
			File file = new File(this.fullFilename);
			file.getParentFile().mkdirs();
			randomAccessWriter = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			Log.e(PCMWriter.class.getName(),
					"Could not create RandomAccessFile: " + this.fullFilename);
		}
	}

	/** Prepares the writer for recording by writing the WAV file header.
	 *
	 * @param fullFilename The full path of the file to write.
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
			randomAccessWriter.writeShort(
					Short.reverseBytes(numberOfChannels));

			// Sample rate.
			//
			randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate));

			// Byte rate = SampleRate * NumberOfChannels * BitsPerSample / 8.
			//
			randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate
					* sampleSize * numberOfChannels / 8));

			// Block align = NumberOfChannels * BitsPerSample / 8.
			//
			randomAccessWriter.writeShort(Short .reverseBytes(
					(short) (numberOfChannels * sampleSize / 8)));

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
			// Note: Removed but here for inspiration.
			//
			// buffer = new byte[framePeriod * sampleSize / 8 *
			// numberOfChannels];
			//
		} catch (IOException e) {
			//If there is an issue here, throw a RuntimeException because
			//there's not much the app can do.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Finalizes the wave file.
	 *
	 *  1. Opens the file (if closed).
	 *  2. Writes the PCM sizes to the header.
	 *  3. Closes the file
	 */
	public void close() {
		// This is only necessary as the randomAccessWriter
		// might have been closed.
		//
		createRandomAccessFile(fullFilename);
		
		try {
			// Write size to RIFF header.
			//
			randomAccessWriter.seek(4);
			randomAccessWriter.writeInt(
					Integer.reverseBytes(36 + payloadSize));

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
