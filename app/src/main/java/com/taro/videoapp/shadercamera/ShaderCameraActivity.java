package com.taro.videoapp.shadercamera;


import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

import com.taro.videoapp.R;
import com.taro.videoapp.shadercamera.camera.CameraInterface;
import com.taro.videoapp.shadercamera.camera.preview.CameraGLSurfaceView;
import com.taro.videoapp.shadercamera.util.DisplayUtil;

import jp.co.cyberagent.android.gpuimage.GPUImage;

public class ShaderCameraActivity extends AppCompatActivity {
    private static final String TAG = "yanzi";
    CameraGLSurfaceView glSurfaceView = null;
    ImageButton shutterBtn;
    float previewRate = -1f;
    private GPUImage mGPUImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initUI();
        initViewParams();

        shutterBtn.setOnClickListener(new BtnListeners());


//        mGPUImage = new GPUImage(this);
//        mGPUImage.setGLSurfaceView((GLSurfaceView) findViewById(R.id.image_surface));
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.topics01);
//        mGPUImage.setImage(bitmap);
//        mGPUImage.setFilter(new GPUImageSepiaFilter());
    }



    private void initUI(){
        glSurfaceView = (CameraGLSurfaceView)findViewById(R.id.camera_textureview);
        shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
    }

    private void initViewParams(){
        LayoutParams params = glSurfaceView.getLayoutParams();
        Point p = DisplayUtil.getScreenMetrics(this);
        params.width = p.x;
        params.height = p.x;
        previewRate = 1;
        glSurfaceView.setLayoutParams(params);

        //�ֶ���������ImageButton�Ĵ�СΪ120dip��120dip,ԭͼƬ��С��64��64
        LayoutParams p2 = shutterBtn.getLayoutParams();
        p2.width = DisplayUtil.dip2px(this, 80);
        p2.height = DisplayUtil.dip2px(this, 80);;
        shutterBtn.setLayoutParams(p2);

    }

    private class BtnListeners implements OnClickListener{

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch(v.getId()){
                case R.id.btn_shutter:
                    CameraInterface.getInstance().doTakePicture();
                    break;
                default:break;
            }
        }

    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        glSurfaceView.bringToFront();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        glSurfaceView.onPause();
    }


}
