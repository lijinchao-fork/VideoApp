package com.taro.media.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class AudioRecorder {
	private final static String TAG = "AudioRecorder";
	private static final int DEFAULT_SAMPLING_RATE = 22050;
	private static final int FRAME_COUNT = 160;
	/* Encoded bit rate. MP3 file will be encoded with bit rate 32kbps */
	private static final int BIT_RATE = 32;

    private static class SingletonHolder {
        private static final AudioRecorder INSTANCE = new AudioRecorder();
    }
    public static AudioRecorder getInstance(){
        return SingletonHolder.INSTANCE;
    }

	private AudioRecord audioRecord = null;

	private int bufferSize;
	private byte[] buffer;


    private RingBuffer ringBuffer;
    public RingBuffer getRingBuffer(){
        return ringBuffer;
    }

	private final int samplingRate;

	private final int channelConfig;

	private final PCMFormat audioFormat;

	private boolean isRecording = false;

    enum Status {
        ready,
        recording,
        readyStop,
        stop
	}
    private Status mStatus = Status.ready;

    public interface RecorderCallBack{
        // 0 - 120 db
        void onVolumeChange(double volume);
        void onFinished(String filePath, long millSeconds);
    }
    private RecorderCallBack recorderCallBack;
    public void setRecorderFinish(RecorderCallBack r){
        recorderCallBack = r;
    }


    class NotifyHandler extends Handler {
        public static final String FILE_PATH = "FILE_PATH";
        public static final String FILE_DURATION = "FILE_DURATION";
        public static final String VOLUME = "VOLUME";

        public static final int MSG_RECORD_FINISH = 100;
        public static final int MSG_RECORD_VOLUME = 101;

        public NotifyHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_RECORD_FINISH) {
                Bundle bundle = msg.getData();
                String filePath = bundle.getString(FILE_PATH);
                long duration = bundle.getLong(FILE_DURATION);
                if(recorderCallBack != null){
                    recorderCallBack.onFinished(filePath, duration);
                }
            }
            if(msg.what == MSG_RECORD_VOLUME){
                Bundle bundle = msg.getData();
                double volume = bundle.getDouble(VOLUME);
                if(recorderCallBack != null){
                    recorderCallBack.onVolumeChange(volume);
                }
            }
            super.handleMessage(msg);
        }
    }

	private final NotifyHandler mUIHandler = new NotifyHandler();

	@SuppressWarnings("SameParameterValue")
	private AudioRecorder(int samplingRate, int channelConfig, PCMFormat audioFormat) {
		this.samplingRate = samplingRate;
		this.channelConfig = channelConfig;
		this.audioFormat = audioFormat;
	}

	/**
	 * Default constructor. Setup recorder with default sampling rate 1 channel,
	 * 16 bits pcm
	 */
	public AudioRecorder() {
		this(DEFAULT_SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
                PCMFormat.PCM_16BIT);
	}

	/**
	 * Start recording. Create an encoding thread. Start record from this
	 * thread.
	 *
	 * @throws IOException
	 */
	public boolean startRecording() {
		if (isRecording) return false;
		Log.d(TAG, "Start recording");
        if(mStatus == Status.recording || mStatus == Status.readyStop){
            return false;
        }
		// Initialize audioRecord if it's null.
		if (audioRecord == null) {
			initAudioRecorder();
		}
		audioRecord.startRecording();
        final long recordStartTime = System.currentTimeMillis();

		new Thread() {

			@Override
			public void run() {
				isRecording = true;
                mStatus = Status.recording;
                long duration;
				while (isRecording) {
					int bytes = audioRecord.read(buffer, 0, bufferSize);
					if (bytes > 0) {

                        int mShortArrayLenght = bytes/2;
                        short[] short_buffer = new short[mShortArrayLenght];
                        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(short_buffer);
                        double v =  0;
                        // 将 buffer 内容取出，进行平方和运算
                        for (int i = 0; i < mShortArrayLenght; i++) {
                            v += Math.abs(short_buffer[i]) * Math.abs(short_buffer[i]);
                        }
                        // 平方和除以数据总长度，得到音量大小。
                        double mean = v / (double) mShortArrayLenght;
                        double volume = 10 * Math.log10(mean);
                        Log.d(TAG, "分贝值: " + volume);
                        Bundle mBundle = new Bundle();
                        mBundle.putDouble(NotifyHandler.VOLUME, volume);
                        Message Msg = new Message();
                        Msg.what = NotifyHandler.MSG_RECORD_VOLUME;
                        Msg.setData(mBundle);
                        mUIHandler.sendMessage(Msg);
                        Log.d(TAG, "write bytes:" + bytes);
                        ringBuffer.write(buffer, bytes);
					}
				}
                duration = System.currentTimeMillis() - recordStartTime;
                mStatus = Status.readyStop;
				// release and finalize audioRecord
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;

                Log.i(TAG, "done encoding thread");

                mStatus = Status.stop;

                Bundle mBundle = new Bundle();
                mBundle.putLong(NotifyHandler.FILE_DURATION, duration);
                Message Msg = new Message();
                Msg.what = NotifyHandler.MSG_RECORD_FINISH;
                Msg.setData(mBundle);
                mUIHandler.sendMessage(Msg);


			}
		}.start();
        return true;
	}

	/**
	 *
	 * @throws IOException
	 */
	public void stopRecording() {
		Log.d(TAG, "stop recording");
		isRecording = false;
	}

	/**
	 * Initialize audio recorder
	 */
	private void initAudioRecorder() {
		int bytesPerFrame = audioFormat.getBytesPerFrame();
		/* Get number of samples. Calculate the buffer size (round up to the
		   factor of given frame size) */
        //22050 1 1
        // 3840
		int frameSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat.getAudioFormat()) / bytesPerFrame;
		if (frameSize % FRAME_COUNT != 0) {
			frameSize = frameSize + (FRAME_COUNT - frameSize % FRAME_COUNT);
			Log.i(TAG, "Frame size: " + frameSize);
		}

		bufferSize = frameSize * bytesPerFrame;
        Log.i(TAG, "BufferSize = " + bufferSize);

		/* Setup audio recorder */
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, channelConfig, audioFormat.getAudioFormat(), bufferSize);
		// Setup RingBuffer. Currently is 10 times size of hardware buffer
		// Initialize buffer to hold data
		ringBuffer = new RingBuffer(100 * bufferSize);
		buffer = new byte[bufferSize];

		// Create and run thread used to encode data
		// The thread will
//		audioRecord.setRecordPositionUpdateListener(encodeThread, encodeThread.getHandler());
//		audioRecord.setPositionNotificationPeriod(FRAME_COUNT);
	}
}