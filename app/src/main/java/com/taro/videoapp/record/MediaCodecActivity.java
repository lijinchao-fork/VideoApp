package com.taro.videoapp.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.taro.media.audio.AudioRecorder;
import com.taro.media.camera.CameraRecordRenderer;
import com.taro.media.filter.FilterManager;
import com.taro.media.video.EncoderConfig;
import com.taro.media.widget.CameraSurfaceView;
import com.taro.videoapp.R;
import com.taro.videoapp.shadercamera.util.FileUtil;

import java.io.File;
import java.io.IOException;

public class MediaCodecActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraSurfaceView mCameraSurfaceView;
    private Button mRecordButton;
    private boolean mIsRecordEnabled;
    private AudioRecorder audioRecorder;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_codec);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera);
        mCameraSurfaceView.setAspectRatio(4, 4);

        findViewById(R.id.filter_normal).setOnClickListener(this);
        findViewById(R.id.filter_tone_curve).setOnClickListener(this);
        findViewById(R.id.filter_soft_light).setOnClickListener(this);

        mRecordButton = (Button) findViewById(R.id.record);
        mRecordButton.setOnClickListener(this);

        updateRecordButton();

//        MediaFormat audioFormat = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
//            audioFormat = new MediaFormat();
//            audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
//            audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
//            MediaCodec mAudioEncoder = null;
//            try {
//                mAudioEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
//                mAudioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//                mAudioEncoder.start();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }

    }

    @Override protected void onResume() {
        super.onResume();
        mCameraSurfaceView.onResume();
        updateRecordButton();
    }

    @Override protected void onPause() {
        mCameraSurfaceView.onPause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        mCameraSurfaceView.onDestroy();
        super.onDestroy();
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_normal:
                mCameraSurfaceView.changeFilter(FilterManager.FilterType.Normal);
                break;
            case R.id.filter_tone_curve:
                mCameraSurfaceView.changeFilter(FilterManager.FilterType.ToneCurve);
                break;
            case R.id.filter_soft_light:
                mCameraSurfaceView.changeFilter(FilterManager.FilterType.SoftLight);
                break;
            case R.id.record:
                if(audioRecorder == null){
                    audioRecorder = AudioRecorder.getInstance();
                }

                if (!mIsRecordEnabled) {
                    audioRecorder.startRecording();
                    mCameraSurfaceView.queueEvent(new Runnable() {
                        @Override public void run() {
                            CameraRecordRenderer renderer = mCameraSurfaceView.getRenderer();
                            renderer.setEncoderConfig(new EncoderConfig(new File(
                                    getCacheDirectory(MediaCodecActivity.this, true),
                                    "video-" + System.currentTimeMillis() + ".mp4"), 480, 480,
                                    1024 * 1024 /* 1 Mb/s */));
                        }
                    });
                }else{
                    audioRecorder.stopRecording();
                }
                mIsRecordEnabled = !mIsRecordEnabled;
                mCameraSurfaceView.queueEvent(new Runnable() {
                    @Override public void run() {
                        mCameraSurfaceView.getRenderer().setRecordingEnabled(mIsRecordEnabled);
                    }
                });
                updateRecordButton();
                break;
        }
    }

    public void updateRecordButton() {
        mRecordButton.setText(mIsRecordEnabled ? "stop" : "start");
    }

    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;

        if (preferExternal && Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState())) {
            appCacheDir = getExternalDirectory(context);
        }

        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }

        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            Log.d(FileUtil.class.getName(),
                    "Can't define system cache directory! use " + cacheDirPath);
            appCacheDir = new File(cacheDirPath);
        }

        return appCacheDir;
    }

    private static File getExternalDirectory(Context context) {

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                Log.d(FileUtil.class.getName(), "无法创建SDCard cache");
                return null;
            }

            //try {
            //    new File(cacheDir, ".nomedia").createNewFile();
            //} catch (IOException e) {
            //    Log.d(FileUtil.class.getName(), "无法创建 .nomedia 文件");
            //}
        }

        return cacheDir;
    }
}
