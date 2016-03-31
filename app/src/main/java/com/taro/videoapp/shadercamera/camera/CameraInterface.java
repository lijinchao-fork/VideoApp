package com.taro.videoapp.shadercamera.camera;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import com.taro.videoapp.shadercamera.util.CamParaUtil;
import com.taro.videoapp.shadercamera.util.FileUtil;
import com.taro.videoapp.shadercamera.util.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;

@TargetApi(11)
public class CameraInterface {
	private static final String TAG = "yanzi";
	private Camera mCamera;
	private Camera.Parameters mParams;
	private boolean isPreviewing = false;
	private static CameraInterface mCameraInterface;
    private boolean isFrontCamera;
    private double ratio;


    private CameraInterface(){

	}
	public static synchronized CameraInterface getInstance(){
		if(mCameraInterface == null){
			mCameraInterface = new CameraInterface();
		}
		return mCameraInterface;
	}

    public float getRatio(){
        return (float) ratio;
    }

    public boolean getIsFrontCamera(){
        return isFrontCamera;
    }

    public void changeCamera(){
        isFrontCamera = !isFrontCamera;
        doOpenCamera();
    }

    public void doOpenCamera(){
        if(mCamera != null){
            doStopCamera();
        }
        if(isFrontCamera){
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                Log.d(TAG, "facing " + cameraInfo.facing);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCamera = Camera.open(i);
                    break;
                }
            }
        }else{
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                Log.d(TAG, "facing " + cameraInfo.facing);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mCamera = Camera.open(i);
                    break;
                }
            }
        }
        Log.i(TAG, "Camera open over....");
    }

    public void doStartPreview(){
        mCamera.startPreview();
        isPreviewing = true;
    }

	/**
	 * @param holder
	 * @param previewRate
	 */
	public void doStartPreview(SurfaceHolder holder, float previewRate){
		Log.i(TAG, "doStartPreview...");
		if(isPreviewing){
			mCamera.stopPreview();
			return;
		}
		if(mCamera != null){
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initCamera(previewRate);
		}


	}
	/**ʹ��TextureViewԤ��Camera
	 * @param surface
	 * @param previewRate
	 */
	public void doStartPreview(SurfaceTexture surface, float previewRate){
		Log.i(TAG, "doStartPreview...");
		if(isPreviewing){
			mCamera.stopPreview();
		}
		if(mCamera != null){
			try {
				mCamera.setPreviewTexture(surface);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initCamera(previewRate);
		}
		
	}

	/**
	 *
	 */
	public void doStopCamera(){
		if(null != mCamera)
		{
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview(); 
			isPreviewing = false;
			mCamera.release();
			mCamera = null;     
		}
	}
	/**
	 *
	 */
	public void doTakePicture(PictureCallback mJpegPictureCallback){
		if(isPreviewing && (mCamera != null)){
			mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
		}
	}
	public boolean isPreviewing(){
		return isPreviewing;
	}

    //实现自动对焦
    public void autoFocus() {
        Log.d(TAG, "autofocus");
        if (mCamera == null) {
            return;
        }
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
            }
        });
    }

	private void initCamera(float previewRate){
		if(mCamera != null){

			mParams = mCamera.getParameters();
			mParams.setPictureFormat(PixelFormat.JPEG);
			CamParaUtil.getInstance().printSupportPictureSize(mParams);
			CamParaUtil.getInstance().printSupportPreviewSize(mParams);

			Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(mParams.getSupportedPictureSizes(),previewRate, 800);
			mParams.setPictureSize(pictureSize.width, pictureSize.height);
            ratio = pictureSize.width * 1.0 / pictureSize.height;
			Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(mParams.getSupportedPreviewSizes(), previewRate, 800);
			mParams.setPreviewSize(previewSize.width, previewSize.height);

			mCamera.setDisplayOrientation(90);

			CamParaUtil.getInstance().printSupportFocusMode(mParams);
			List<String> focusModes = mParams.getSupportedFocusModes();
			if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                Log.d(TAG, "mCamera FOCUS_MODE_CONTINUOUS_PICTURE");
				mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			}
			mCamera.setParameters(mParams);	
			mCamera.startPreview();
            mCamera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
			isPreviewing = true;
			mParams = mCamera.getParameters();
			Log.i(TAG, "PreviewSize--With = " + mParams.getPreviewSize().width + "Height = " + mParams.getPreviewSize().height);
			Log.i(TAG, "PictureSize--With = " + mParams.getPictureSize().width + "Height = " + mParams.getPictureSize().height);
		}
	}




	ShutterCallback mShutterCallback = new ShutterCallback()
	{
		public void onShutter() {
			Log.i(TAG, "myShutterCallback:onShutter...");
		}
	};
	PictureCallback mRawCallback = new PictureCallback() 
	// �����δѹ��ԭ��ݵĻص�,����Ϊnull
	{

		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(TAG, "myRawCallback:onPictureTaken...");

		}
	};


}
