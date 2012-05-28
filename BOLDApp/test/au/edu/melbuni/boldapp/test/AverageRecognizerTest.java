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

//	@Test
//	public void conceptionAboutBytesIsCorrect() {
//		assertEquals(0, 0);
//		assertEquals(1, 1);
//		assertEquals(127, 127);
//		assertEquals(-128, 128);
//		assertEquals(-1, 255);
//		assertEquals(0, 256);
//	}
	
//	@Test
//	public void conceptionAboutConversionIsCorrect() {
//		assertEquals(0, (short)(0 | (0 << 8)));
//		assertEquals(256, (short)(0 | (1 << 8)));
//	}
	
	@Test
	public void getAverageAmplitude1() {
		assertEquals(0, defaultRecognizer.getAverageAmplitude(new short[] { 0,
				0, 0, 0 }));
	}

	@Test
	public void getAverageAmplitude2() {
		assertEquals(0, defaultRecognizer.getAverageAmplitude(new short[] { 1,
				0, 0, 0 }));
	}

	@Test
	public void getAverageAmplitude3() {
		assertEquals(1, defaultRecognizer.getAverageAmplitude(new short[] { 1,
				1, 1, 1 }));
	}

	@Test
	public void getAverageAmplitude3b() {
		assertEquals(256, defaultRecognizer.getAverageAmplitude(new short[] { 0,
				512, 0, 512 }));
	}
	
	@Test
	public void getAverageAmplitude3c() {
		assertEquals(128, defaultRecognizer.getAverageAmplitude(new short[] { 0,
				0, 0, 512 }));
	}
	
	@Test
	public void getAverageAmplitude4() {
		assertEquals(8192, defaultRecognizer.getAverageAmplitude(new short[] { 0,
				16384, 0, 16384 }));
	}
	
	@Test
	public void getAverageAmplitude5() {
		assertEquals(2, defaultRecognizer.getAverageAmplitude(new short[] { 1,
				2, 3, 4 }));
	}

}
