package au.edu.melbuni.boldapp.models;

import java.io.IOException;
import java.util.List;

import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Camera implements SurfaceHolder.Callback {

	SurfaceView surfaceView;
	android.hardware.Camera camera;
	android.hardware.Camera.PictureCallback pictureCallback;

	boolean previewRunning = false;

	public Camera(SurfaceView surfaceView,
			android.hardware.Camera.PictureCallback pictureCallback) {
		this.surfaceView = surfaceView;
		this.pictureCallback = pictureCallback;

		// getWindow().setFormat(PixelFormat.TRANSLUCENT);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(
		// WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN
		// );
		// setContentView(R.layout.camera);
		// surfaceView = (SurfaceView)
		// findViewById(R.id.userPictureSurfaceView);
		// surfaceView.setOnClickListener(this);

		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);
	}

	// android.hardware.Camera.PictureCallback pictureCallback = new
	// android.hardware.Camera.PictureCallback() {
	// @Override
	// public void onPictureTaken(byte[] imageData, android.hardware.Camera c) {
	// if (imageData != null) {
	// currentUser.putProfileImage(imageData);
	// setResult(RESULT_OK, new Intent());
	// finish();
	// }
	// }
	// };

	public void takePicture() {
		camera.takePicture(null, pictureCallback, pictureCallback);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (camera != null) {
			// Note: stopPreview() will crash if preview is not running.
			//
			if (previewRunning) {
				camera.stopPreview();
			}

			android.hardware.Camera.Parameters parameters = camera
					.getParameters();
			
			// TODO Get the right size.
			//
			List<Size> previewSizes = parameters.getSupportedPreviewSizes();
			Size previewSize = previewSizes.get(0);
			parameters.setPreviewSize(previewSize.width, previewSize.height);
			
			// TODO Make dynamic!
			//
			List<Size> sizes = parameters.getSupportedPictureSizes();
			Size size = sizes.get(4);
			parameters.setPictureSize(size.width, size.height);
			
			camera.setParameters(parameters);
			
			try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			camera.startPreview();
			previewRunning = true;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		camera = android.hardware.Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		camera.stopPreview();
		previewRunning = false;
		camera.release();
	}

}
