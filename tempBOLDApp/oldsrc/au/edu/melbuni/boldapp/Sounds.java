package au.edu.melbuni.boldapp;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class Sounds {

	static boolean prepared = false;
	private static SoundPool sounds;

	private static int beep;
	private static int beepbeep;

	public static void prepare(Context context) {
		sounds = new SoundPool(2, AudioManager.STREAM_NOTIFICATION, 0);

		beep = sounds.load(context, R.raw.beep, 1);
		beep = sounds.load(context, R.raw.beepbeep, 1);

		prepared = true;
	}

	public static void beep() {
		if (!prepared) {
			LogWriter.log("Sounds not prepared for beep!");
			return;
		}
		sounds.play(beep, 1f, 1f, 1, 0, 1f);
	}

	public static void beepbeep() {
		if (!prepared) {
			LogWriter.log("Sounds not prepared for beepbeep!");
			return;
		}
		sounds.play(beepbeep, 1f, 1f, 10, 0, 1f);
	}
}
