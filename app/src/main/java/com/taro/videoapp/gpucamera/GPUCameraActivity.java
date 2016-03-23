package com.taro.videoapp.gpucamera;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.taro.videoapp.R;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

/**
 * Created by joshuali on 16/3/22.
 */
public class GPUCameraActivity extends AppCompatActivity {

    private GLSurfaceView glView;
    private GPUImage mGPUImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_surface);

        glView = (GLSurfaceView) findViewById(R.id.image_surface);

        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView(glView);
        Camera camera = Camera.open();
        mGPUImage.setUpCamera(camera);
        mGPUImage.setFilter(new GPUImageSepiaFilter());
    }

}
