package au.edu.melbuni.boldapp;

import java.io.BufferedWriter;
import java.io.FileWriter;

import android.text.format.Time;
import au.edu.melbuni.boldapp.persisters.Persister;

public class LogWriter {

	public static void log(String message) {
		writeToFile(message, "log.txt");
	}
	
	public static void reset() {
		// TODO
	}

	private static void writeToFile(String message, String filename) {
		try {
			BufferedWriter bos = new BufferedWriter(new FileWriter(Persister.getBasePath() + filename, true));
			Time currentTime = new Time();
			currentTime.setToNow();
			bos.write(currentTime.format2445() + ": ");
			bos.write(message);
			bos.newLine();
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}