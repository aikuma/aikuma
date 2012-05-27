//package au.edu.melbuni.boldapp;
//
//import java.util.Arrays;
//
//import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
//
//public class Transcriber extends SpeechController {
//
//	Player player;
//
//	protected boolean okTriggered = true;
//	protected boolean waitTriggered = false;
//	protected boolean goBackTriggered = false;
//
//	int[] history = new int[3];
//
//	public Transcriber() {
//		player = Bundler.getPlayer();
//	}
//
//	public void listen(String fileName, OnCompletionListener completionListener) {
//		super.listen(fileName, completionListener);
//		player.startPlaying(fileName, completionListener);
//	}
//
//	public void stop() {
//		player.stopPlaying();
//		super.stop();
//	}
//
//	protected void appendShiftHistory(int value) {
//		for (int i = 1; i < history.length; i++) {
//			history[i - 1] = history[i];
//		}
//		history[history.length - 1] = value;
//	}
//
//	public void onBufferFull(short[] buffer) {
//		int reading = getMaxAmplitude(buffer);
//
//		if (isSilence(reading)) {
//			appendShiftHistory(0);
//		} else if (isSpeech(reading)) {
//			appendShiftHistory(1);
//		} else {
//			appendShiftHistory(2);
//		}
//		
//		LogWriter.log("[" + history[0] + ", " + history[1] + ", " + history[2] + "]");
//
//		if (waitTriggered) {
//			if (isGoBack()) {
//				goBack();
//			}
//		}
//		if (isOk()) {
//			if (okTriggered) {
//				return;
//			}
//			okGo();
//			okTriggered = true;
//			waitTriggered = false;
////			goBackTriggered = false;
//		} else {
//			if (isWait()) {
//				if (waitTriggered) {
//					return;
//				}
//				doWait();
//				waitTriggered = true;
//				okTriggered = false;
////				goBackTriggered = false;
//			}
////			else {
////				if (isGoBack()) {
////					if (goBackTriggered) {
////						return;
////					}
////					goBack();
////					goBackTriggered = true;
////					okTriggered = false;
////					waitTriggered = false;
////				}
////			}
//		}
//	}
//
//	private void okGo() {
//		player.resume();
//	}
//
//	private void doWait() {
//		player.pause();
//		player.rewind(200);
//	}
//
//	private void goBack() {
//		player.pause();
//		player.rewind(1200);
//		player.resume();
//		try {
//			Thread.sleep(200);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		player.pause();
//	}
//
//	int[] goBack = { 0, 1, 0 };
//
//	private boolean isGoBack() {
//		return Arrays.equals(history, goBack);
//	}
//
//	int[] wait = { 0, 1, 0 };
//
//	private boolean isWait() {
//		return Arrays.equals(history, wait);
//	}
//
//	int[] ok = { 1, 1, 0 };
//
//	private boolean isOk() {
//		return Arrays.equals(history, ok);
//	}
//
//}
