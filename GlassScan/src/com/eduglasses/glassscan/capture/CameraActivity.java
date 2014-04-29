package com.eduglasses.glassscan.capture;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.eduglasses.glassscan.BaseGlassActivity;
import com.eduglasses.glassscan.LaunchActivity;
import com.eduglasses.glassscan.R;
import com.eduglasses.glassscan.image.ImageManager;

/**
 * A placeholder fragment containing a simple view.
 */
public class CameraActivity extends BaseGlassActivity implements
		SurfaceHolder.Callback {

	private static final String TAG = CameraActivity.class.getSimpleName();
	private static final String IMAGE_PREFIX = "GlassScan_";

	private Camera camera;
	private boolean mHasSurface;
	private ImageManager mImageManager;

	public static Intent newIntent(Context context) {
		Intent intent = new Intent(context, CameraActivity.class);
		return intent;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_camera);
		mImageManager = new ImageManager(this);

		mHasSurface = false;
		// uncomment to debug the application.
		// android.os.Debug.waitForDebugger();

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startCamera();
	}

	@Override
	protected void onPause() {
		if (!mHasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		camera.release();
		super.onPause();
	}

	private void startCamera() {
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (mHasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}

	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(surfaceHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera.startPreview();
	}

	@Override
	protected boolean onTap() {
		camera.takePicture(null, null, mPicture);
		return super.onTap();
	}

	PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] captureData, Camera camera) {

			Bitmap captureImage = null;
			if (captureData != null) {
				captureImage = getBitmapFromByteArray(captureData);
			}

			Uri imageUri = null;
			String imageName = IMAGE_PREFIX + System.currentTimeMillis()
					+ ".png";
			try {
				imageUri = mImageManager.saveImage(imageName, captureImage);
				Log.v(TAG, "Saving image as: " + imageName);
				finish();
				
				//Intent intent = new Intent(getApplicationContext(), GlassScanActivity.class);
	        //	Intent i = new Intent();
	        	//i.putExtra("DeviceId", imageUri);
	        	//startActivity(i);
	        	
			} catch (IOException e) {
				Log.e(TAG, "Failed to save image!", e);
			}
		}
	};

	protected Bitmap getBitmapFromByteArray(byte[] captureData) {
		Bitmap captureImage = null;
		captureImage = BitmapFactory.decodeByteArray(captureData, 0,
				captureData.length, null);
		// Mutable copy:
		captureImage = captureImage.copy(Bitmap.Config.ARGB_8888, true);
		return captureImage;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!mHasSurface) {
			mHasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHasSurface = false;
	}

}