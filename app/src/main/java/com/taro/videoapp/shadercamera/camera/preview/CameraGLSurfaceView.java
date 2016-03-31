package com.taro.videoapp.shadercamera.camera.preview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.AttributeSet;
import android.util.Log;

import com.taro.videoapp.shadercamera.camera.CameraInterface;
import com.taro.videoapp.shadercamera.util.FileUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageColorBurnBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

@TargetApi(11)
public class CameraGLSurfaceView extends GLSurfaceView implements Renderer, SurfaceTexture.OnFrameAvailableListener {
	private static final String TAG = "yanzi";
	Context mContext;
	SurfaceTexture mSurface;
	int mTextureID = -1;
	DirectDrawer mDirectDrawer;
	public CameraGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onSurfaceCreated...");
		mTextureID = createTextureID();
		mSurface = new SurfaceTexture(mTextureID);
		mSurface.setOnFrameAvailableListener(this);
		mDirectDrawer = new DirectDrawer(mTextureID);
		CameraInterface.getInstance().doOpenCamera();

	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged... width " + width + " height " + height);
        GLES20.glViewport(0, 0, width, height);
        CameraInterface.getInstance().doStartPreview(mSurface, 1.33f);
        mDirectDrawer.initBuffer(CameraInterface.getInstance().getRatio());
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		mSurface.updateTexImage();
		float[] mtx = new float[16];
		mSurface.getTransformMatrix(mtx);
		mDirectDrawer.draw(mtx);
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        Log.d(TAG, "onPause");
		CameraInterface.getInstance().doStopCamera();
	}

	private int createTextureID()
	{
		int[] texture = new int[1];

		GLES20.glGenTextures(1, texture, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

		return texture[0];
	}

	public SurfaceTexture _getSurfaceTexture(){
		return mSurface;
	}
	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		this.requestRender();
	}

    public void changeCamera(){
        CameraInterface.getInstance().changeCamera();
        CameraInterface.getInstance().doStartPreview(mSurface, 1.33f);
        mDirectDrawer.changeUpDown();
        mDirectDrawer.initBuffer(CameraInterface.getInstance().getRatio());
    }

	public void doTakePicture(){
		CameraInterface.getInstance().doTakePicture(mJpegPictureCallback);
	}

    Camera.PictureCallback mJpegPictureCallback = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "myJpegCallback:onPictureTaken...");
//			Bitmap b = null;
//			if(null != data){
//				b = BitmapFactory.decodeByteArray(data, 0, data.length);
//				mCamera.stopPreview();
//				isPreviewing = false;
//			}
//			if(null != b)
//			{
//				Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
//				FileUtil.saveBitmap(rotaBitmap);
//			}

            // Write to SD Card
            if(null != data){
                byte[] imageData = data;

                Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
                // 向左旋转45度，参数为正则向右旋转
                Bitmap dstbmp = null;
                int wh =  b.getHeight() > b.getWidth() ? b.getWidth() : b.getHeight();
                int max =  b.getHeight() < b.getWidth() ? b.getWidth() : b.getHeight();
                int offset = (max - wh) / 2;
                Log.i(TAG, "doInBackground width:" + b.getWidth() + " height:" + b.getHeight());
                // 定义矩阵对象
                Matrix matrix = new Matrix();
                boolean isFrontCamera = CameraInterface.getInstance().getIsFrontCamera();
                if(b.getWidth() > b.getHeight()){
                    matrix.postRotate(90);
                    if(isFrontCamera){
                        matrix.postScale(1f, -1f);
                        b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                        dstbmp = Bitmap.createBitmap(b, 0, offset, wh, wh);
                    }else{
                        matrix.postScale(1f, 1f);
                        b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                        dstbmp = Bitmap.createBitmap(b, 0, offset, wh, wh, new Matrix(), true);
                    }
                }else{
                    if(isFrontCamera){
                        matrix.postScale(1f, -1f);
                        b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                        dstbmp = Bitmap.createBitmap(b, 0, offset, wh, wh);
                    }else{
                        matrix.postScale(1f, 1f);
                        dstbmp = Bitmap.createBitmap(b, 0, offset, wh, wh, new Matrix(), true);
                    }
                }
                if (!b.isRecycled()) {
                    b.recycle();
                }
                System.gc();

                final Bitmap finalDstbmp = dstbmp;
                ((Activity)getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GPUImage gpuImage = new GPUImage(getContext());
                        gpuImage.setImage(finalDstbmp);
                        GPUImageGaussianBlurFilter filter =  new GPUImageGaussianBlurFilter((float) 2.0);
                        gpuImage.setFilter(filter);
                        Bitmap blurBitMap = gpuImage.getBitmapWithFilterApplied();
                        FileUtil.saveBitmap(blurBitMap);
                        Log.i(TAG, "onPictureTaken - wrote bytes: " + finalDstbmp.getHeight() * finalDstbmp.getWidth() + " to ");
                        if (!finalDstbmp.isRecycled()) {
                            finalDstbmp.recycle();
                        }
                    }
                });

                CameraInterface.getInstance().doStartPreview();


            }
        }
    };
}
