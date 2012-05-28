package au.edu.melbuni.boldapp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AverageRecognizer extends Recognizer {

	protected int silenceThreshold;
	protected int speechThreshold;

	public AverageRecognizer() {
		this(6, 6);
	}

	public AverageRecognizer(int silenceDivisor, int speechDivisor) {
		// MediaRecorder.getAudioSourceMax();
		// TODO Make dynamic depending on phone.
		//
		int maxAmplitude = 32768;

		// Silence is less than
		// 1/n of max amplitude.
		//
		this.silenceThreshold = 0; // maxAmplitude / silenceDivisor;

		// Speech is more than 1/m of max amplitude.
		//
		this.speechThreshold = 0; // maxAmplitude / speechDivisor;
	}

	@Override
	public boolean isSilence(short[] buffer) {
		int reading = getAverageAmplitude(buffer);
		
		LogWriter.log("Average: " + reading);

		return reading < silenceThreshold;
	}

	@Override
	public boolean isSpeech(short[] buffer) {
		int reading = getAverageAmplitude(buffer);

		return reading > speechThreshold;
	}

	public int getAverageAmplitude(short[] buffer) {
		int sum = 0;

		for (int i = 0; i < buffer.length; i++) {
			sum += buffer[i];
		}

		return sum / buffer.length;
	}
	
//	// Convoluted version of
//	// argB1 | (argB2 << 8)
//	//
//	private int getInt(byte b1, byte b2) {
//		return (int) b1 | b2 << 8;
//		
////		ByteBuffer byteBuffer = ByteBuffer.allocate(2);
////		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
////		byteBuffer.put(0, b2);
////		byteBuffer.put(1, b1);
////		return (int) byteBuffer.getShort();
//		
////		int intB2 = (int) argB2;
////		intB2 = (intB2 << 8);
////		if (intB2 < 0) { intB2 += 65536; }
////		System.out.println(intB2);
////		
////		int intB1 = (int) argB1;
////		if (intB1 < 0) { intB1 += 512; }
////		System.out.println(intB1);
////		
////		int value = intB2 + intB1;
////		return value;
//	}

}
