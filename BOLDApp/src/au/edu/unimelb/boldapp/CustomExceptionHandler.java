package au.edu.unimelb.boldapp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.io.File;

import android.os.Environment;

//import au.edu.melbuni.boldapp.persisters.Persister;

public class CustomExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler defaultUEH;

	/*
	 * if any of the parameters is null, the respective functionality will not
	 * be used
	 */
	public CustomExceptionHandler() {
		this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}

	public static String getBasePath() {
		File external = Environment.getExternalStorageDirectory();
		if (external != null) {
			return external.getAbsolutePath() + "/bold/";
		}
		return "/mnt/sdcard/bold/";
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		printWriter.close();
		String filename = "stacktrace.txt";

		writeToFile(stacktrace, filename);

		defaultUEH.uncaughtException(t, e);
	}

	public void writeToFile(String stacktrace, String filename) {
		try {
			BufferedWriter bos = new BufferedWriter(new FileWriter(getBasePath() + filename));
			bos.write(stacktrace);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
