package au.edu.melbuni.boldapp;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class CameraActivity extends Activity implements SurfaceHolder.Callback, OnClickListener {

	Camera camera;
	boolean previewRunning = false;
	User currentUser;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		// Explicit since it isn't a BoldActivity.
		//
		currentUser = Bundler.getCurrentUser(this);
		
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN
		);
		setContentView(R.layout.camera);
		surfaceView = (SurfaceView) findViewById(R.id.userPictureSurfaceView);
		surfaceView.setOnClickListener(this);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] imageData, Camera c) {
			if (imageData != null) {
				currentUser.putProfileImage(imageData);
				setResult(RESULT_OK, new Intent());
				finish();
			}
		}
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Note: stopPreview() will crash if preview is not running.
		//
		if (previewRunning) {
			camera.stopPreview();
		}

		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(w, h);
		List<Size> sizes = parameters.getSupportedPictureSizes();
		Size size = sizes.get(4); // TODO Make dynamic!
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

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		previewRunning = false;
		camera.release();
	}

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;

	@Override
	public void onClick(View view) {
		camera.takePicture(null, pictureCallback, pictureCallback);
	}

}
