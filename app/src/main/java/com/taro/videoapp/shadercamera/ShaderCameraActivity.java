package com.taro.videoapp.shadercamera;


import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.taro.videoapp.R;
import com.taro.videoapp.shadercamera.camera.CameraInterface;
import com.taro.videoapp.shadercamera.camera.preview.CameraGLSurfaceView;
import com.taro.videoapp.shadercamera.util.DisplayUtil;

public class ShaderCameraActivity extends AppCompatActivity {
    private static final String TAG = "yanzi";
    CameraGLSurfaceView glSurfaceView = null;
    TextView shutterBtn;
    TextView btn_change;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initUI();
        initViewParams();
        shutterBtn.setOnClickListener(new BtnListeners());
        btn_change.setOnClickListener(new BtnListeners());
    }



    private void initUI(){
        glSurfaceView = (CameraGLSurfaceView)findViewById(R.id.camera_textureview);
        shutterBtn = (TextView)findViewById(R.id.btn_shutter);
        btn_change = (TextView)findViewById(R.id.btn_change);
    }

    private void initViewParams(){
        LayoutParams params = glSurfaceView.getLayoutParams();
        Point p = DisplayUtil.getScreenMetrics(this);
        params.width = p.x;
        params.height = (int) (p.x * 1.0);
        glSurfaceView.setLayoutParams(params);

    }

    private class BtnListeners implements OnClickListener{

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_shutter:
                    glSurfaceView.doTakePicture();
                    break;
                case R.id.btn_change:
                    glSurfaceView.changeCamera();
                    break;
                default:break;
            }
        }

    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
//        glSurfaceView.bringToFront();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
//        glSurfaceView.onPause();
    }


}
