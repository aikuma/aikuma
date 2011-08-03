package au.edu.melbuni.boldapp;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
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
		public void onPictureTaken(byte[] imageData, Camera c) {
			if (imageData != null) {
				currentUser.putProfileImage(imageData);
				setResult(RESULT_OK, new Intent());
				finish();
			}
		}
	};

	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Note: stopPreview() will crash if preview is not running.
		//
		if (previewRunning) {
			camera.stopPreview();
		}

		Camera.Parameters p = camera.getParameters();
		p.setPreviewSize(w, h);
		camera.setParameters(p);
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera.startPreview();
		previewRunning = true;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		previewRunning = false;
		camera.release();
	}

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;

	public void onClick(View view) {
		camera.takePicture(null, pictureCallback, pictureCallback);
	}

}
