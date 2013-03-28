package au.edu.unimelb.aikuma.audio.widgets;

import android.util.Log;

import android.app.Dialog;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import android.view.Window;

import android.widget.ImageView;

import au.edu.unimelb.aikuma.RespeakActivity;

import au.edu.unimelb.aikuma.audio.analyzers.BackgroundNoise;
import au.edu.unimelb.aikuma.audio.analyzers.BackgroundNoiseListener;
import au.edu.unimelb.aikuma.audio.thresholders.Noise;

/** 
 * Tries to extract the level of background noise.
 *
 * This is currently very specific on the RespeakActivity.
 *
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 */
public class NoiseLevel {
	
	private RespeakActivity activity;
	
	public NoiseLevel(RespeakActivity activity) {
		this.activity = activity;
	}
	
	public void find() {
		// Set up a dialog.
		//
		final Dialog dialog = new Dialog(activity) {
			public void onBackPressed() {
				setSensitivity(50);
				super.onBackPressed();
			}
		};
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		final ImageView imageView = new ImageView(activity);
		final Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		dialog.addContentView(imageView, new android.view.ViewGroup.LayoutParams(200, 200));
		dialog.show();
		
		final Paint paint = new Paint();
		
		new BackgroundNoise(50).getThreshold(new BackgroundNoiseListener() {
			public void noiseLevelQualityUpdated(final Noise.Information information) {
				activity.runOnUiThread(new Runnable() {
					public void run() {
						int size = 200;
						float quality = information.getQuality();
						double divisor = Math.log(-quality)+1;
						int minimum = information.getMinimum();
						int maximum = information.getMaximum();
						float factor = 0f;
						if (maximum > 0) { factor = 1f*minimum / maximum; }
						long padding = Math.round(1f*factor*(size/2));
						Log.i("getInformation", " " + ((int) Math.round(255/divisor)) + " " + minimum + " " + maximum + " " + factor + " " + (1.0-factor)*(size/2));
						
						// Draw a black background.
						//
						paint.setARGB(255, 0, 0, 0);
						canvas.drawRect(0, 0, size, size, paint);
						
						// Draw a square:
						//  * Green if the noise level is constant.
						//  * Red & going towards transparent if the noise is too high.
						//  * Large if the noise level is high.
						//  * Small if the noise level is low.
						//
						paint.setARGB((int) Math.round(255*divisor), (int) Math.round(200*divisor), (int) Math.round(255/divisor), 50);
						canvas.drawRect(padding, padding, size-padding, size-padding, paint);
						imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
						imageView.requestLayout();
					}
				});
			}
			public void noiseLevelFound(final Noise.Information information) {
				setSensitivity(information.getRecommendedRecordingLevel());
				activity.runOnUiThread(new Runnable() {
					public void run() {
						dialog.dismiss();
					}
				});
			}
		});
	}
	
	private void setSensitivity(int level) {
		activity.getSensitivitySlider().setMax(level*2);
		activity.getSensitivitySlider().setProgress(level);
		activity.getRespeaker().setSensitivity(level);
	}
	
}