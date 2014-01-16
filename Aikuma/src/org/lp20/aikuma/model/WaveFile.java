import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;

public class WaveFile {

	public static void main(String[] args) throws IOException {
		WaveFile waveFile = new WaveFile(new File("ok.wav"));
		System.out.println(waveFile.getSampleRate());
		System.out.println(waveFile.getBitsPerSample());
		System.out.println(waveFile.getDuration());
		System.out.println(waveFile.getNumChannels());
	}

	public WaveFile(File file) throws IOException {
		setFile(file);
		readBytes();
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

	private void readBytes() throws IOException {
		mBytes = FileUtils.readFileToByteArray(mFile);
	}

	private void readSampleRate() {
		byte[] sampleRateBytes = Arrays.copyOfRange(mBytes, 24, 28);
		ByteBuffer bb = ByteBuffer.wrap(sampleRateBytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		mSampleRate = bb.getInt();
	}

	private void readBitsPerSample() {
		byte[] bpsBytes = Arrays.copyOfRange(mBytes, 34, 36);
		ByteBuffer bb = ByteBuffer.wrap(bpsBytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		mBitsPerSample = bb.getShort();
	}

	private void readNumChannels() {
		byte[] ncBytes = Arrays.copyOfRange(mBytes, 22, 24);
		ByteBuffer bb = ByteBuffer.wrap(ncBytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		mNumChannels = bb.getShort();
	}

	private File mFile;
	private byte[] mBytes;
	private int mSampleRate;
	private short mBitsPerSample;
	private short mNumChannels;
}
