package com.taro.videoapp.shaderimage;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.taro.videoapp.R;

import java.util.Random;

/**
 * Created by joshuali on 16/3/22.
 */
public class ShaderImageActivity extends AppCompatActivity {

    private GLSurfaceView glView; // Use GLSurfaceView
    Random rnd = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_surface);

        glView = (GLSurfaceView) findViewById(R.id.image_surface);
        glView.setRenderer(new MyGLRenderer(this)); // Use a custom renderer

    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();

    }


}
