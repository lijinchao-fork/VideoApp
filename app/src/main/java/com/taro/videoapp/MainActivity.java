package com.taro.videoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.taro.videoapp.gpucamera.GPUCameraActivity;
import com.taro.videoapp.record.MediaCodecActivity;
import com.taro.videoapp.shadercamera.ShaderCameraActivity;
import com.taro.videoapp.shaderimage.ShaderImageActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.shader_image).setOnClickListener(this);
        findViewById(R.id.share_camera).setOnClickListener(this);
        findViewById(R.id.gpu_camera).setOnClickListener(this);
        findViewById(R.id.record_media).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.shader_image){
            Intent intent = new Intent(this, ShaderImageActivity.class);
            startActivity(intent);
        }

        if(v.getId() == R.id.share_camera){
            Intent intent = new Intent(this, ShaderCameraActivity.class);
            startActivity(intent);
        }

        if(v.getId() == R.id.gpu_camera){
            Intent intent = new Intent(this, GPUCameraActivity.class);
            startActivity(intent);
        }

        if(v.getId() == R.id.record_media){
            Intent intent = new Intent(this, MediaCodecActivity.class);
            startActivity(intent);
        }
    }
}
