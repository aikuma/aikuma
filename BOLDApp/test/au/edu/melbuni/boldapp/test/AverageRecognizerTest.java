package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.AverageRecognizer;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AverageRecognizerTest {

	AverageRecognizer defaultRecognizer;
	AverageRecognizer specificRecognizer;

	@Before
	public void setUp() {
		this.defaultRecognizer = new AverageRecognizer();
		this.specificRecognizer = new AverageRecognizer(1, 1);
	}

	@Test
	public void conceptionAboutBytesIsCorrect() {
		assertEquals(0, (byte) 0);
		assertEquals(1, (byte) 1);
		assertEquals(127, (byte) 127);
		assertEquals(-128, (byte) 128);
		assertEquals(-1, (byte) 255);
		assertEquals(0, (byte) 256);
	}
	
	@Test
	public void conceptionAboutConversionIsCorrect() {
		assertEquals(0, (short)((byte) 0 | ((byte) 0 << 8)));
		assertEquals(256, (short)((byte) 0 | ((byte) 1 << 8)));
		
		
	}
	
	@Test
	public void getAverageAmplitude1() {
		assertEquals(0, defaultRecognizer.getAverageAmplitude(new byte[] { 0,
				0, 0, 0 }));
	}

	@Test
	public void getAverageAmplitude2() {
		assertEquals(0, defaultRecognizer.getAverageAmplitude(new byte[] { 1,
				0, 0, 0 }));
	}

	@Test
	public void getAverageAmplitude3() {
		assertEquals(1, defaultRecognizer.getAverageAmplitude(new byte[] { 1,
				0, 1, 0 }));
	}

	@Test
	public void getAverageAmplitude3b() {
		assertEquals(256, defaultRecognizer.getAverageAmplitude(new byte[] { (byte) 0,
				(byte) 1, (byte) 0, (byte) 1 }));
	}
	
	@Test
	public void getAverageAmplitude3c() {
		assertEquals(128, defaultRecognizer.getAverageAmplitude(new byte[] { (byte) 0,
				(byte) 0, (byte) 0, (byte) 1 }));
	}
	
	@Test
	public void getAverageAmplitude4() {
		assertEquals(32768, defaultRecognizer.getAverageAmplitude(new byte[] { (byte) 0,
				(byte) 128, (byte) 0, (byte) 128 }));
	}
	
	@Test
	public void getAverageAmplitude5() {
		assertEquals(65280, defaultRecognizer.getAverageAmplitude(new byte[] { (byte) 0,
				(byte) 255, (byte) 0, (byte) 255 }));
	}
	
	@Test
	public void getAverageAmplitude6() {
		assertEquals(32640, defaultRecognizer.getAverageAmplitude(new byte[] { (byte) 0,
				(byte) 0, (byte) 0, (byte) 255 }));
	}

}
