import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;

public class WaveFile {

	public static void main(String[] args) throws IOException {
		WaveFile waveFile = new WaveFile(new File("test.wav"));
		System.out.println(waveFile.getSampleRate());
	}

	public WaveFile(File file) throws IOException {
		setFile(file);
		readBytes();
		readSampleRate();
	}

	public int getSampleRate() {
		return mSampleRate;
	}

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

	private File mFile;
	private byte[] mBytes;
	private int mSampleRate;
}
