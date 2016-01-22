package pmag.snapandsearch.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import pmag.snapandsearch.SnapAndSearchAbstractActivity;
import pmag.snapandsearch.SnapAndSearchInterface;
import pmag.snapandsearch.search.R;
import pmag.snapandsearch.search.SearchActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class SnapActivity extends SnapAndSearchAbstractActivity implements SnapAndSearchInterface {
	public Camera myCamera;
	private CameraPreview myPreview;

	private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		private SurfaceHolder mHolder;
		private Camera myCamera;

		public CameraPreview(Context context, Camera camera) {
			// TODO restart preview when screen is unlocked while app is active
			super(context);
			myCamera = camera;

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, now tell the camera where to draw
			// the
			// preview.
			try {
				myCamera.setPreviewDisplay(holder);
				myCamera.startPreview();
			} catch (IOException e) {
				Log.e(MY_APP_NAME, "Error setting camera preview: ", e);
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// empty. Take care of releasing the Camera preview in your
			// activity.

		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				myCamera.stopPreview();
			} catch (Exception e) {
				Log.e(MY_APP_NAME, "Error on surfaceChanged camera preview: ", e);
			}

			// setup of the camera
			// setupCamera();

			// start preview with new settings
			try {
				myCamera.setPreviewDisplay(mHolder);
				myCamera.startPreview();

			} catch (Exception e) {
				Log.e(MY_APP_NAME, "Error starting camera preview", e);
			}
		}
	}

	/** Create a File for saving an image */
	private File getOutputImageFile() {

		File mediaStorageDir = new File(getFilesDir(), MY_APP_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(MY_APP_NAME, "failed to create directory " + mediaStorageDir.getAbsolutePath());
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

		return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
	}

	/**
	 * Auto focus call back
	 * 
	 * @author FR067458
	 * 
	 */
	private class CameraAutoFocusCallback implements Camera.AutoFocusCallback {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			/* auto focus complete , let's take the picture */
			File outputFile = getOutputImageFile();
			if (outputFile == null) {
				Log.e(MY_APP_NAME, "Error creating media file");
			} else {
				JpegPictureCallback jpegPictureCallback = new JpegPictureCallback();
				jpegPictureCallback.setPictureFile(outputFile);
				myCamera.takePicture(null, null, jpegPictureCallback);
			}
		}
	}

	/*
	 * Callback to take a JPEG image
	 */
	private class JpegPictureCallback implements PictureCallback {

		private File pictureFile = null;

		public void setPictureFile(File pictureFile) {
			this.pictureFile = pictureFile;
		}

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Log.i(MY_APP_NAME, "JPEG file to be written is " + pictureFile.getAbsolutePath());
			if (deviceHasRotationBug()) {
				// rotate the image
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();
				// post rotate to 90
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				// Rotating Bitmap
				Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

				FileOutputStream fos;
				try {
					fos = new FileOutputStream(pictureFile);
					rotatedBitmap.compress(CompressFormat.JPEG, 100, fos);
				} catch (Exception e) {
					Log.e(MY_APP_NAME, "Unable to write image", e);
				}
			} else {
				// juste write the data to a file
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
				} catch (Exception e) {
					Log.e(MY_APP_NAME, "Unable to write image", e);
				}

			}
			// release the camera now
			myCamera.stopPreview();
			myCamera.release();

			// calling activity result
			Intent resultIntent = new Intent(getApplicationContext(), SearchActivity.class);
			resultIntent.putExtra(RESULT_FILE_NAME_PARAM, pictureFile.getAbsolutePath());
			Log.d(MY_APP_NAME, "Starting the result activity");
			startActivity(resultIntent);

		}

		/**
		 * For some devices, setRotation() doesn't work This method will check
		 * is the current device in the list of known devices having the
		 * problem.
		 * 
		 * @return
		 */
		private boolean deviceHasRotationBug() {

			// list of known devices that have the bug
			ArrayList<String> devices = new ArrayList<String>();
			devices.add("samsung/GT-S5830/GT-S5830");
			devices.add("samsung/cooper/cooper"); // CYANOGENMOD for GT-S5830
			devices.add("samsung/GT-S5660/GT-S5660");
			devices.add("samsung/GT-I9003/GT-I9003");

			return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/" + android.os.Build.DEVICE);

		}

	};

	/**
	 * Listener for the Snap button
	 */
	private class SnapButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// disable the button to prevent from clicking it again before focus
			// and snapshot is done
			v.setEnabled(false);
			myCamera.autoFocus(new CameraAutoFocusCallback());

		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.snap);

		// register the onclick listener to the button
		Button takePictureButton = (Button) findViewById(R.id.take_picture_bt_id);
		takePictureButton.setOnClickListener(new SnapButtonOnClickListener());

		// info on this device
		Log.i(MY_APP_NAME, "Current device : " + android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/" + android.os.Build.DEVICE);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myCamera.release();
	}

	@Override
	protected void onPause() {
		super.onPause();
		myPreview.getHolder().removeCallback(myPreview);
		myCamera.release();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// open or re-open camera
		myCamera = Camera.open();
		setupCamera(myCamera);

		// Create our Preview view and set it as the content of our activity.
		myPreview = new CameraPreview(this, myCamera);
		FrameLayout framePreview = (FrameLayout) findViewById(R.id.camera_preview_layout_id);
		framePreview.addView(myPreview);
		// enable the snap button
		Button snapButton = (Button) findViewById(R.id.take_picture_bt_id);
		snapButton.setEnabled(true);
	}

	private boolean cameraNaturalOrientationIsLandscape() {
		/*
		 * most of the camera have a landscape natural orientation a except
		 * devices listed here below
		 */
		ArrayList<String> devices = new ArrayList<String>();
		// devices.add("samsung/GT-I9003/GT-I9003");
		return !(devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/" + android.os.Build.DEVICE));

	}

	private void setDisplayOrientationPortrait(Camera camera, Camera.Parameters cameraParameters) {
		Method setDisplayOrientationMethod;
		try {
			setDisplayOrientationMethod = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
			if (setDisplayOrientationMethod != null) {
				setDisplayOrientationMethod.invoke(camera, new Object[] { 90 });
			} else { // let's try another way to set the display orientation in
						// portrait mode
				cameraParameters.set("orientation", "portrait");
			}
		} catch (Exception e) {
			Log.e(MY_APP_NAME, "Error while trying to set portrait display mode", e);
		}
	}

	private void setupCamera(Camera camera) {

		Camera.Parameters myCamParam = camera.getParameters();
		// most of the cameras are landscape oriented
		// since our activity is set to portrait, we may need to rotate preview
		// and
		// shot image

		if (cameraNaturalOrientationIsLandscape()) {
			setDisplayOrientationPortrait(camera, myCamParam);
			myCamParam.setRotation(90);// doesn't work on some devices
		}

		computePictureSize(myCamera, myCamParam);
		// myCamParam.setSceneMode(Camera.Parameters.SCENE_MODE_BARCODE);
		myCamParam.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
		myCamera.setParameters(myCamParam);
	}

	private void computePictureSize(Camera camera, Camera.Parameters myCamParam) {
		Camera.Size pictureSize = null;
		
		List<Camera.Size> supportedListSizes = myCamParam.getSupportedPictureSizes(); 
		// getSupportedPictureSizes returns null in some emulators so we test it
		if (supportedListSizes != null) {
			Iterator<Camera.Size> supportedSizes = supportedListSizes.iterator();
			while (supportedSizes.hasNext()) {
				Camera.Size supportedSize = (Camera.Size) supportedSizes.next();
				Log.i(MY_APP_NAME, "Available picture size for this camera : " + supportedSize.width + "x" + supportedSize.height);
				// we take the lowest available resolution
				if (pictureSize == null) {
					pictureSize = supportedSize;
				} else if (supportedSize.width < pictureSize.width) {
					pictureSize = supportedSize;
				}
			}
			myCamParam.setPictureSize(pictureSize.width, pictureSize.height);
			Log.i(MY_APP_NAME, "Picture size has been set to " + pictureSize.width + "x" + pictureSize.height);
		}
		else
		{
			Log.i(MY_APP_NAME, "Couldn't retrieve the list of supported picture sizes (probably running in an emulator). The picture size will be the default.");
		}
	}
}