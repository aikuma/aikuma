/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.model;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * An "abstract" representation of a WAVE file entity, which offers methods for
 * probing information held in the header, as well as calculating the duration.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class WaveFile {

	/**
	 * Constructor.
	 *
	 * @param	file	The file to read from.
	 * @throws	IOException	In the event that there is an issue reading the
	 * file.
	 */
	public WaveFile(File file) throws IOException {
		setFile(file);
		//readBytes();
		readHeader();
		readNumChannels();
		readSampleRate();
		readBitsPerSample();
	}

	/**
	 * Gets the sample rate in kHz.
	 *
	 * @return	the sample rate in kHz.
	 */
	public int getSampleRate() {
		return mSampleRate;
	}

	/**
	 * Gets the number of bits per sample.
	 *
	 * @return	The number of bits per sample.
	 */
	public short getBitsPerSample() {
		return mBitsPerSample;
	}

	/**
	 * Gets the number of channels (Mono = 1, Stereo = 2).
	 *
	 * @return	The number of channels.
	 */
	public short getNumChannels() {
		return mNumChannels;
	}


	/**
	 * Gets the duration of the WAVE file in seconds.
	 *
	 * @return	The duration of the WAVE file in seconds.
	 */
	public double getDuration() {
		// Data length is the total number of bytes minus the header.
		long dataLength = mFile.length() - 44;
		// Here we assume that bits per sample will always be a multiple of 8.
		long numSamples = dataLength / ((getBitsPerSample() / 8) * mNumChannels);
		return (double) numSamples / mSampleRate;
	}

	///////////////////////////////////////////////////////////////////////////

	private void setFile(File file) {
		mFile = file;
	}

	// Reads the header from the wave file.
	private void readHeader() throws IOException {
		BufferedInputStream in = new
				BufferedInputStream(new FileInputStream(mFile), 44);
		byte[] buff = new byte[44];
		if (in.read(buff) == -1) {
			throw new IOException("End of input stream reached before filling the header.");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(buff);
		out.flush();
		mHeader = out.toByteArray();
	}

	// Reads the sample rate from the wave file
	private void readSampleRate() {
		byte[] sampleRateBytes = Arrays.copyOfRange(mHeader, 24, 28);
		ByteBuffer bb = ByteBuffer.wrap(sampleRateBytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		mSampleRate = bb.getInt();
	}

	// Reads the bits per sample from the wave file
	private void readBitsPerSample() {
		byte[] bpsBytes = Arrays.copyOfRange(mHeader, 34, 36);
		ByteBuffer bb = ByteBuffer.wrap(bpsBytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		mBitsPerSample = bb.getShort();
	}

	// Reads the number of channels from the wave file
	private void readNumChannels() {
		byte[] ncBytes = Arrays.copyOfRange(mHeader, 22, 24);
		ByteBuffer bb = ByteBuffer.wrap(ncBytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		mNumChannels = bb.getShort();
	}

	private File mFile;
	private byte[] mHeader;
	private int mSampleRate;
	private short mBitsPerSample;
	private short mNumChannels;
}
